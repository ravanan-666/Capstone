package com.djmart.controller;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dao.jdbc.*;
import com.djmart.dto.OrderResponse;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductRequest;
import com.djmart.dto.ProductResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.ValidationException;
import com.djmart.model.Role;
import com.djmart.service.OrderService;
import com.djmart.service.ProductService;
import com.djmart.service.impl.OrderServiceImpl;
import com.djmart.service.impl.ProductServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller orchestrating seller dashboard analytics, product catalog management (create, edit, delete),
 * and seller-specific order fulfillment.
 */
@WebServlet(name = "SellerServlet", urlPatterns = {
        "/seller",
        "/seller/*",
        "/api/seller/*",
        "/api/v1/seller/*"
})
public class SellerServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SellerServlet.class);

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;

    public SellerServlet() {
        this(new ProductServiceImpl(new ProductDAOImpl(), new ReviewDAOImpl()),
             new OrderServiceImpl(new OrderDAOImpl(), new OrderItemDAOImpl(), new ProductDAOImpl(), new CartDAOImpl()),
             new ProductDAOImpl(),
             new OrderDAOImpl(),
             new OrderItemDAOImpl());
    }

    public SellerServlet(ProductService productService, OrderService orderService,
                         ProductDAO productDAO, OrderDAO orderDAO, OrderItemDAO orderItemDAO) {
        this.productService = productService;
        this.orderService = orderService;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);
            String path = getPath(request);

            if (path.contains("/stats")) {
                Map<String, Object> stats = computeSellerStats(user.getId());
                sendSuccess(response, HttpServletResponse.SC_OK, stats);
                return;
            }

            if (path.contains("/orders")) {
                int page = getIntParam(request, "page", 1);
                int size = getIntParam(request, "size", 10);
                PageResponse<OrderResponse> orders = orderService.getOrdersBySeller(user.getId(), page, size);
                sendSuccess(response, HttpServletResponse.SC_OK, orders);
                return;
            }

            if (path.contains("/products")) {
                int page = getIntParam(request, "page", 1);
                int size = getIntParam(request, "size", 10);
                PageResponse<ProductResponse> products = productService.getProductsBySeller(user.getId(), page, size);
                sendSuccess(response, HttpServletResponse.SC_OK, products);
                return;
            }

            // Default: Seller Dashboard JSP
            renderDashboard(request, response, user);

        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);
            String path = getPath(request);
            boolean isApi = isJsonRequest(request);

            if (path.endsWith("/products/delete")) {
                handleDeleteProduct(request, response, user, isApi);
            } else if (path.endsWith("/products/update")) {
                handleUpdateProduct(request, response, user, isApi);
            } else {
                handleCreateProduct(request, response, user, isApi);
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);
            handleUpdateProduct(request, response, user, true);
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);
            handleDeleteProduct(request, response, user, true);
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private void renderDashboard(HttpServletRequest request, HttpServletResponse response, UserResponse user)
            throws ServletException, IOException {
        int page = getIntParam(request, "page", 1);
        int size = getIntParam(request, "size", 10);

        Map<String, Object> stats = computeSellerStats(user.getId());
        PageResponse<ProductResponse> products = productService.getProductsBySeller(user.getId(), page, size);
        PageResponse<OrderResponse> recentOrders = orderService.getOrdersBySeller(user.getId(), 1, 5);
        List<String> categories = productService.getCategories();

        request.setAttribute("stats", stats);
        request.setAttribute("products", products);
        request.setAttribute("recentOrders", recentOrders.getContent());
        request.setAttribute("categories", categories);

        forwardToJsp(request, response, "seller/dashboard");
    }

    private Map<String, Object> computeSellerStats(Long sellerId) {
        Map<String, Object> stats = new HashMap<>();
        long totalProducts = productDAO.countBySeller(sellerId);
        long totalOrders = orderDAO.countRelevantSellerOrders(sellerId);
        long lowStockCount = productDAO.countLowStockBySeller(sellerId, 5);
        BigDecimal revenue = orderItemDAO.calculateSellerRevenue(sellerId);

        stats.put("totalProducts", totalProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("lowStockCount", lowStockCount);
        stats.put("revenue", revenue);
        return stats;
    }

    private void handleCreateProduct(HttpServletRequest request, HttpServletResponse response,
                                     UserResponse user, boolean isApi) throws IOException {
        ProductRequest productReq;
        if (isApi && request.getContentType() != null && request.getContentType().contains("application/json")) {
            productReq = parseRequestBody(request, ProductRequest.class);
        } else {
            String name = getStringParam(request, "name");
            String description = getStringParam(request, "description");
            BigDecimal price = getBigDecimalParam(request, "price");
            Integer stockQty = getIntParam(request, "stockQty", 0);
            String category = getStringParam(request, "category");
            String imageUrl = getStringParam(request, "imageUrl");
            productReq = new ProductRequest(name, description, price, stockQty, category, imageUrl);
        }

        ProductResponse created = productService.createProduct(user.getId(), productReq);
        LOGGER.info("Seller {} created product {}", user.getId(), created.getId());

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_CREATED, created, "Product listing created successfully");
        } else {
            redirect(request, response, "/seller/dashboard?created=true");
        }
    }

    private void handleUpdateProduct(HttpServletRequest request, HttpServletResponse response,
                                     UserResponse user, boolean isApi) throws IOException {
        Long productId = getLongParam(request, "productId");
        if (productId == null) {
            productId = extractIdFromPath(getPath(request));
        }

        if (productId == null) {
            throw new ValidationException("Product ID is required for update");
        }

        ProductRequest productReq;
        if (isApi && request.getContentType() != null && request.getContentType().contains("application/json")) {
            productReq = parseRequestBody(request, ProductRequest.class);
        } else {
            String name = getStringParam(request, "name");
            String description = getStringParam(request, "description");
            BigDecimal price = getBigDecimalParam(request, "price");
            Integer stockQty = getIntParam(request, "stockQty", 0);
            String category = getStringParam(request, "category");
            String imageUrl = getStringParam(request, "imageUrl");
            productReq = new ProductRequest(name, description, price, stockQty, category, imageUrl);
        }

        boolean isAdmin = user.getRole() == Role.ADMIN;
        ProductResponse updated = productService.updateProduct(user.getId(), productId, productReq, isAdmin);
        LOGGER.info("Seller {} updated product {}", user.getId(), productId);

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_OK, updated, "Product updated successfully");
        } else {
            redirect(request, response, "/seller/dashboard?updated=true");
        }
    }

    private void handleDeleteProduct(HttpServletRequest request, HttpServletResponse response,
                                     UserResponse user, boolean isApi) throws IOException {
        Long productId = getLongParam(request, "productId");
        if (productId == null) {
            productId = extractIdFromPath(getPath(request));
        }

        if (productId == null) {
            throw new ValidationException("Product ID is required for deletion");
        }

        boolean isAdmin = user.getRole() == Role.ADMIN;
        productService.deleteProduct(user.getId(), productId, isAdmin);
        LOGGER.info("Seller {} deleted product {}", user.getId(), productId);

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_OK, null, "Product deleted successfully");
        } else {
            redirect(request, response, "/seller/dashboard?deleted=true");
        }
    }

    private Long extractIdFromPath(String path) {
        if (path == null) return null;
        String[] segments = path.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isEmpty() && !"delete".equalsIgnoreCase(segments[i]) && !"update".equalsIgnoreCase(segments[i])) {
                try {
                    return Long.parseLong(segments[i]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
