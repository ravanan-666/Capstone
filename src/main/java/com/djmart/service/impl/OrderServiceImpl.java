package com.djmart.service.impl;

import com.djmart.dao.CartDAO;
import com.djmart.dao.OrderDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dto.CheckoutRequest;
import com.djmart.dto.OrderItemResponse;
import com.djmart.dto.OrderResponse;
import com.djmart.dto.PageResponse;
import com.djmart.exception.*;
import com.djmart.model.*;
import com.djmart.service.OrderService;
import com.djmart.util.DatabaseUtil;
import com.djmart.util.ValidationErrors;
import com.djmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business service implementation for orders and atomic checkout transactions.
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;

    public OrderServiceImpl(OrderDAO orderDAO, OrderItemDAO orderItemDAO,
                            ProductDAO productDAO, CartDAO cartDAO) {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }

    @Override
    public OrderResponse checkout(Long buyerId, CheckoutRequest request) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(buyerId, "buyerId", errors);
        if (request == null) {
            errors.addError("request", "Checkout request cannot be null");
        } else {
            ValidationUtil.validateRequired(request.getShippingAddress(), "shippingAddress", errors);
            if (request.getShippingAddress() != null && request.getShippingAddress().trim().length() < 5) {
                errors.addError("shippingAddress", "Shipping address must be at least 5 characters long");
            }
        }
        errors.throwIfHasErrors("Checkout validation failed");

        // 1. Load active cart items
        List<CartItem> cartItems = cartDAO.findByUser(buyerId);

        // 2. Validate cart is not empty
        if (cartItems.isEmpty()) {
            LOGGER.warn("Checkout rejected: Shopping cart is empty for buyer ID {}", buyerId);
            throw new ValidationException("Shopping cart is empty. Please add items before checking out.");
        }

        // 3. Reload current product prices and validate stock
        BigDecimal serverTotal = BigDecimal.ZERO;
        List<Product> productsToOrder = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productDAO.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (item.getQuantity() > product.getStockQty()) {
                LOGGER.warn("Checkout rejected: Product {} insufficient stock. Available: {}, Requested: {}",
                        product.getId(), product.getStockQty(), item.getQuantity());
                throw new InsufficientStockException(product.getId(), item.getQuantity(), product.getStockQty());
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            serverTotal = serverTotal.add(itemSubtotal);
            productsToOrder.add(product);
        }

        Order savedOrder;
        // Execute multi-table transaction atomically
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 6. Create Order record
                Order order = new Order();
                order.setBuyerId(buyerId);
                order.setStatus(OrderStatus.CONFIRMED);
                order.setTotalAmount(serverTotal);
                order.setShippingAddress(request.getShippingAddress().trim());

                savedOrder = orderDAO.create(conn, order);

                // 7. Create Order Items & 8. Reduce Stock
                List<OrderItem> itemsToPersist = new ArrayList<>();
                for (int i = 0; i < cartItems.size(); i++) {
                    CartItem cartItem = cartItems.get(i);
                    Product product = productsToOrder.get(i);

                    boolean decremented = productDAO.decrementStock(conn, product.getId(), cartItem.getQuantity());
                    if (!decremented) {
                        throw new InsufficientStockException(product.getId(), cartItem.getQuantity(), product.getStockQty());
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(savedOrder.getId());
                    orderItem.setProductId(product.getId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(product.getPrice()); // Authoritative current price
                    itemsToPersist.add(orderItem);
                }
                orderItemDAO.createBatch(conn, itemsToPersist);

                // 9. Clear Buyer Cart
                cartDAO.clear(conn, buyerId);

                // 10. Commit Transaction
                conn.commit();
                LOGGER.info("Checkout transaction committed successfully. Order ID: {}", savedOrder.getId());
            } catch (Exception e) {
                LOGGER.error("Checkout transaction failed; rolling back all operations: {}", e.getMessage(), e);
                conn.rollback();
                if (e instanceof AppException) {
                    throw (AppException) e;
                }
                throw new DatabaseException("Checkout failed during transaction execution", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Database connection error during checkout: {}", e.getMessage(), e);
            throw new DatabaseException("Database transaction error", e);
        }

        return enrichOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long requesterId, Role requesterRole) {
        if (orderId == null) {
            throw new ResourceNotFoundException("Order ID cannot be null");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Enforce RBAC access control
        if (requesterRole == Role.BUYER && !order.getBuyerId().equals(requesterId)) {
            throw new AuthorizationException("You are not authorized to view this order");
        }

        if (requesterRole == Role.SELLER) {
            List<OrderItemResponse> sellerItems = orderItemDAO.findByOrderIdAndSellerId(orderId, requesterId);
            if (sellerItems.isEmpty()) {
                throw new AuthorizationException("You are not authorized to view this order");
            }
        }

        return enrichOrderResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByBuyer(Long buyerId, int page, int size) {
        if (buyerId == null) {
            throw new IllegalArgumentException("Buyer ID cannot be null");
        }

        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        List<Order> orders = orderDAO.findByBuyer(buyerId, offset, safeSize);
        long total = orderDAO.countByBuyer(buyerId);

        List<OrderResponse> dtos = orders.stream()
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, safePage, safeSize, total);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersBySeller(Long sellerId, int page, int size) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }

        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        List<Order> orders = orderDAO.findRelevantSellerOrders(sellerId, offset, safeSize);
        long total = orderDAO.countRelevantSellerOrders(sellerId);

        List<OrderResponse> dtos = orders.stream()
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, safePage, safeSize, total);
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        List<Order> orders = orderDAO.findAll(offset, safeSize);
        long total = orderDAO.countAll();

        List<OrderResponse> dtos = orders.stream()
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, safePage, safeSize, total);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long updaterId, Role updaterRole) {
        if (orderId == null || newStatus == null) {
            throw new IllegalArgumentException("Order ID and new status cannot be null");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Authorization check: Only Seller of products in the order or Admin can change status
        if (updaterRole == Role.BUYER) {
            throw new AuthorizationException("Buyers cannot change order status directly; use cancelOrder instead");
        }

        if (updaterRole == Role.SELLER) {
            List<OrderItemResponse> sellerItems = orderItemDAO.findByOrderIdAndSellerId(orderId, updaterId);
            if (sellerItems.isEmpty()) {
                throw new AuthorizationException("You are not authorized to update this order");
            }
        }

        // If cancelling, route through cancellation to safely restore stock
        if (newStatus == OrderStatus.CANCELLED) {
            cancelOrder(orderId, updaterId, updaterRole);
            return enrichOrderResponse(orderDAO.findById(orderId).orElseThrow());
        }

        // Validate workflow transition
        validateStatusTransition(order.getStatus(), newStatus);

        orderDAO.updateStatus(orderId, newStatus);
        LOGGER.info("Order ID {} status transitioned from {} to {} by user ID {}",
                orderId, order.getStatus(), newStatus, updaterId);

        order.setStatus(newStatus);
        return enrichOrderResponse(order);
    }

    @Override
    public boolean cancelOrder(Long orderId, Long requesterId, Role requesterRole) {
        if (orderId == null) {
            return false;
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Ownership validation
        if (requesterRole == Role.BUYER && !order.getBuyerId().equals(requesterId)) {
            throw new AuthorizationException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderException("Cannot cancel order that has already been shipped or delivered");
        }

        // Atomic inventory restoration upon cancellation
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                orderDAO.updateStatus(conn, orderId, OrderStatus.CANCELLED);

                // Restore stock for all items
                List<OrderItemResponse> items = orderItemDAO.findByOrderId(orderId);
                for (OrderItemResponse item : items) {
                    Product product = productDAO.findById(item.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                    productDAO.updateStock(product.getId(), product.getStockQty() + item.getQuantity());
                }

                conn.commit();
                LOGGER.info("Order ID {} successfully cancelled; inventory restored", orderId);
                return true;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.error("Failed to cancel order ID {}: {}", orderId, e.getMessage(), e);
                throw new DatabaseException("Failed to cancel order", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during order cancellation", e);
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new OrderException("Order in " + current + " state cannot undergo further transitions");
        }

        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            default -> false;
        };

        if (!valid) {
            throw new OrderException("Illegal order status transition from " + current + " to " + next);
        }
    }

    @Override
    public List<java.util.Map<String, Object>> getBestSellingProducts(int limit) {
        return orderDAO.getBestSellingProducts(limit);
    }

    @Override
    public List<java.util.Map<String, Object>> getDailySales(int days) {
        return orderDAO.getDailySales(days);
    }

    @Override
    public java.util.Map<String, Object> getSalesAnalytics() {
        return orderDAO.getSalesSummary();
    }

    private OrderResponse enrichOrderResponse(Order order) {
        OrderResponse dto = OrderResponse.fromOrder(order);
        if (dto != null) {
            List<OrderItemResponse> items = orderItemDAO.findByOrderId(order.getId());
            dto.setItems(items);
        }
        return dto;
    }
}
