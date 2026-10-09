package com.djmart.controller;

import com.djmart.dao.jdbc.CartDAOImpl;
import com.djmart.dao.jdbc.OrderDAOImpl;
import com.djmart.dao.jdbc.OrderItemDAOImpl;
import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.dto.CartResponse;
import com.djmart.dto.CheckoutRequest;
import com.djmart.dto.OrderResponse;
import com.djmart.dto.PageResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.ValidationException;
import com.djmart.model.OrderStatus;
import com.djmart.model.Role;
import com.djmart.service.CartService;
import com.djmart.service.OrderService;
import com.djmart.service.impl.CartServiceImpl;
import com.djmart.service.impl.OrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller orchestrating checkout flows, order history, order details,
 * order cancellations, and seller/admin status transitions.
 */
@WebServlet(name = "OrderServlet", urlPatterns = {
        "/orders",
        "/orders/*",
        "/api/orders/*",
        "/checkout",
        "/checkout/*",
        "/api/checkout",
        "/api/checkout/*",
        "/api/v1/orders/*",
        "/api/v1/checkout",
        "/api/v1/checkout/*"
})
public class OrderServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServlet.class);

    private final OrderService orderService;
    private final CartService cartService;

    public OrderServlet() {
        this(new OrderServiceImpl(new OrderDAOImpl(), new OrderItemDAOImpl(), new ProductDAOImpl(), new CartDAOImpl()),
             new CartServiceImpl(new CartDAOImpl(), new ProductDAOImpl()));
    }

    public OrderServlet(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);
        boolean isApi = isJsonRequest(request);

        try {
            UserResponse user = requireAuthenticatedUser(request);

            // Checkout page
            if (isCheckoutPath(path)) {
                handleCheckoutView(request, response, user, isApi);
                return;
            }

            // Single order details
            Long orderId = extractOrderId(path);
            if (orderId != null) {
                OrderResponse order = orderService.getOrderById(orderId, user.getId(), user.getRole());
                if (isApi) {
                    sendSuccess(response, HttpServletResponse.SC_OK, order);
                } else {
                    request.setAttribute("order", order);
                    forwardToJsp(request, response, "buyer/order-details");
                }
                return;
            }

            // Order history list
            handleOrderHistory(request, response, user, isApi);

        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);
        boolean isApi = isJsonRequest(request);

        try {
            UserResponse user = requireAuthenticatedUser(request);

            // 1. Checkout execution
            if (isCheckoutPath(path) || path.equals("/api/orders") || path.equals("/api/v1/orders")) {
                handleCheckoutSubmit(request, response, user, isApi);
                return;
            }

            // 2. Cancellation
            if (path.endsWith("/cancel")) {
                handleCancelOrder(request, response, user, isApi);
                return;
            }

            // 3. Status transition (for seller or admin)
            if (path.endsWith("/status")) {
                handleUpdateStatus(request, response, user);
                return;
            }

            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Order action not found", "NOT_FOUND");
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private void handleCheckoutView(HttpServletRequest request, HttpServletResponse response,
                                    UserResponse user, boolean isApi)
            throws ServletException, IOException {
        CartResponse cart = cartService.getCart(user.getId());
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            if (isApi) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Shopping cart is empty", "EMPTY_CART");
            } else {
                redirect(request, response, "/cart?empty=true");
            }
            return;
        }

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_OK, cart);
        } else {
            request.setAttribute("cart", cart);
            request.setAttribute("user", user);
            forwardToJsp(request, response, "buyer/checkout");
        }
    }

    private void handleCheckoutSubmit(HttpServletRequest request, HttpServletResponse response,
                                      UserResponse user, boolean isApi)
            throws IOException {
        CheckoutRequest checkoutReq;
        if (isApi && request.getContentType() != null && request.getContentType().contains("application/json")) {
            checkoutReq = parseRequestBody(request, CheckoutRequest.class);
        } else {
            String shippingAddress = getStringParam(request, "shippingAddress");
            boolean paymentConfirmed = "true".equalsIgnoreCase(request.getParameter("paymentConfirmed")) ||
                    request.getParameter("paymentMethod") != null;
            checkoutReq = new CheckoutRequest(shippingAddress, paymentConfirmed);
        }

        if (checkoutReq == null || checkoutReq.getShippingAddress() == null || checkoutReq.getShippingAddress().trim().length() < 5) {
            throw new ValidationException("A valid shipping address of at least 5 characters is required");
        }

        OrderResponse placedOrder = orderService.checkout(user.getId(), checkoutReq);
        LOGGER.info("Order placed successfully. Order ID: {} for user: {}", placedOrder.getId(), user.getId());

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_CREATED, placedOrder, "Order placed successfully");
        } else {
            redirect(request, response, "/orders/" + placedOrder.getId() + "?success=true");
        }
    }

    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response,
                                   UserResponse user, boolean isApi)
            throws IOException {
        String path = getPath(request);
        Long orderId = extractOrderId(path);
        if (orderId == null) {
            orderId = getLongParam(request, "orderId");
        }

        if (orderId == null) {
            throw new ValidationException("Order ID is required for cancellation");
        }

        boolean cancelled = orderService.cancelOrder(orderId, user.getId(), user.getRole());
        LOGGER.info("Order {} cancellation result: {} by user {}", orderId, cancelled, user.getId());

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_OK, null, "Order cancelled successfully");
        } else {
            redirect(request, response, "/orders/" + orderId + "?cancelled=true");
        }
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response,
                                    UserResponse user)
            throws IOException {
        requireRole(request, Role.SELLER, Role.ADMIN);
        String path = getPath(request);
        Long orderId = extractOrderId(path);
        if (orderId == null) {
            orderId = getLongParam(request, "orderId");
        }

        String statusStr = getStringParam(request, "status");
        if (statusStr == null && request.getContentType() != null && request.getContentType().contains("application/json")) {
            StatusUpdateRequest updateReq = parseRequestBody(request, StatusUpdateRequest.class);
            if (updateReq != null) {
                statusStr = updateReq.status;
            }
        }

        if (orderId == null || statusStr == null) {
            throw new ValidationException("Order ID and target status are required");
        }

        OrderStatus newStatus = OrderStatus.fromString(statusStr);
        OrderResponse updated = orderService.updateOrderStatus(orderId, newStatus, user.getId(), user.getRole());
        sendSuccess(response, HttpServletResponse.SC_OK, updated, "Order status updated successfully");
    }

    private void handleOrderHistory(HttpServletRequest request, HttpServletResponse response,
                                    UserResponse user, boolean isApi)
            throws ServletException, IOException {
        int page = getIntParam(request, "page", 1);
        int size = getIntParam(request, "size", 10);

        PageResponse<OrderResponse> ordersPage;
        if (user.getRole() == Role.ADMIN) {
            ordersPage = orderService.getAllOrders(page, size);
        } else if (user.getRole() == Role.SELLER) {
            ordersPage = orderService.getOrdersBySeller(user.getId(), page, size);
        } else {
            ordersPage = orderService.getOrdersByBuyer(user.getId(), page, size);
        }

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_OK, ordersPage);
        } else {
            request.setAttribute("ordersPage", ordersPage);
            forwardToJsp(request, response, "buyer/orders");
        }
    }

    private boolean isCheckoutPath(String path) {
        return path.equals("/checkout") || path.equals("/checkout/") ||
               path.equals("/api/checkout") || path.equals("/api/checkout/") ||
               path.equals("/api/v1/checkout") || path.equals("/api/v1/checkout/");
    }

    private Long extractOrderId(String path) {
        if (path == null) return null;
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            if ("orders".equals(segments[i]) && i + 1 < segments.length) {
                String potentialId = segments[i + 1];
                if (!potentialId.isEmpty() && !"cancel".equalsIgnoreCase(potentialId) && !"status".equalsIgnoreCase(potentialId)) {
                    try {
                        return Long.parseLong(potentialId);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return null;
    }

    private static class StatusUpdateRequest {
        private String status;
    }
}
