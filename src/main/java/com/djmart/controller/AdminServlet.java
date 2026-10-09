package com.djmart.controller;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dao.UserDAO;
import com.djmart.dao.jdbc.*;
import com.djmart.dto.OrderResponse;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.ValidationException;
import com.djmart.model.OrderStatus;
import com.djmart.model.Product;
import com.djmart.model.Role;
import com.djmart.service.OrderService;
import com.djmart.service.ProductService;
import com.djmart.service.UserService;
import com.djmart.service.impl.OrderServiceImpl;
import com.djmart.service.impl.ProductServiceImpl;
import com.djmart.service.impl.UserServiceImpl;
import com.djmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * Controller orchestrating platform administration, marketplace KPIs, user moderation,
 * product/inventory controls, category management, order oversight, and analytics.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
        "/admin",
        "/admin/*",
        "/api/admin/*",
        "/api/v1/admin/*"
})
public class AdminServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServlet.class);

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public AdminServlet() {
        this(new UserServiceImpl(new UserDAOImpl()),
             new OrderServiceImpl(new OrderDAOImpl(), new OrderItemDAOImpl(), new ProductDAOImpl(), new CartDAOImpl()),
             new ProductServiceImpl(new ProductDAOImpl(), new ReviewDAOImpl()),
             new UserDAOImpl(),
             new OrderDAOImpl(),
             new ProductDAOImpl());
    }

    public AdminServlet(UserService userService, OrderService orderService, ProductService productService,
                        UserDAO userDAO, OrderDAO orderDAO, ProductDAO productDAO) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.ADMIN);
            String path = getPath(request);

            if (path.contains("/stats")) {
                Map<String, Object> stats = computePlatformStats();
                sendSuccess(response, HttpServletResponse.SC_OK, stats);
                return;
            }

            if (path.contains("/analytics")) {
                renderAnalytics(request, response);
                return;
            }

            if (path.contains("/users")) {
                int page = getIntParam(request, "page", 1);
                int size = getIntParam(request, "size", 15);
                PageResponse<UserResponse> users = userService.findAllUsers(page, size);
                sendSuccess(response, HttpServletResponse.SC_OK, users);
                return;
            }

            if (path.contains("/orders")) {
                int page = getIntParam(request, "page", 1);
                int size = getIntParam(request, "size", 15);
                PageResponse<OrderResponse> orders = orderService.getAllOrders(page, size);
                sendSuccess(response, HttpServletResponse.SC_OK, orders);
                return;
            }

            if (path.contains("/products")) {
                int page = getIntParam(request, "page", 1);
                int size = getIntParam(request, "size", 25);
                PageResponse<ProductResponse> products = productService.getAllProducts(page, size);
                sendSuccess(response, HttpServletResponse.SC_OK, products);
                return;
            }

            // Default: Admin Dashboard JSP view
            renderDashboard(request, response);

        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.ADMIN);
            UserResponse admin = getAuthenticatedUser(request);
            String path = getPath(request);
            boolean isApi = isJsonRequest(request);

            // 1. Order Status Transition by Admin
            if (path.contains("/order/status") || path.contains("/orders/status")) {
                Long orderId = getLongParam(request, "orderId");
                String statusStr = getStringParam(request, "status");

                if (orderId == null || statusStr == null) {
                    throw new ValidationException("Order ID and new status are required");
                }

                OrderStatus newStatus = OrderStatus.fromString(statusStr);
                OrderResponse updated = orderService.updateOrderStatus(orderId, newStatus, admin.getId(), Role.ADMIN);
                LOGGER.info("Admin {} updated order ID {} to status {}", admin.getId(), orderId, newStatus);

                if (isApi) {
                    sendSuccess(response, HttpServletResponse.SC_OK, updated, "Order status updated successfully");
                } else {
                    redirect(request, response, "/admin/dashboard?statusUpdated=true");
                }
                return;
            }

            // 2. Product Price and Stock Update by Admin
            if (path.contains("/product/update") || path.contains("/products/update")) {
                Long productId = getLongParam(request, "productId");
                BigDecimal price = getBigDecimalParam(request, "price");
                Integer stock = getIntParam(request, "stock", -1);
                String imageUrl = getStringParam(request, "imageUrl");

                if (productId == null) {
                    throw new ValidationException("Product ID is required");
                }

                Optional<Product> prodOpt = productDAO.findById(productId);
                if (prodOpt.isEmpty()) {
                    throw new ValidationException("Product not found with ID " + productId);
                }

                Product prod = prodOpt.get();
                if (price != null && price.compareTo(BigDecimal.ZERO) >= 0) {
                    prod.setPrice(price);
                    prod.setPriceVerifiedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                if (stock >= 0) {
                    prod.setStockQty(stock);
                }
                if (imageUrl != null && !imageUrl.isBlank()) {
                    prod.setImageUrl(imageUrl.trim());
                }

                boolean updated = productDAO.update(prod);
                LOGGER.info("Admin {} updated product ID {}: success={}", admin.getId(), productId, updated);

                if (isApi) {
                    sendSuccess(response, HttpServletResponse.SC_OK, null, "Product updated successfully");
                } else {
                    redirect(request, response, "/admin/dashboard?productUpdated=true");
                }
                return;
            }

            // 3. Category Creation by Admin
            if (path.contains("/category/create") || path.contains("/categories/create")) {
                String catName = getStringParam(request, "name");
                String catSlug = getStringParam(request, "slug");
                String catDesc = getStringParam(request, "description");

                if (catName == null || catName.trim().isEmpty()) {
                    throw new ValidationException("Category name is required");
                }
                if (catSlug == null || catSlug.trim().isEmpty()) {
                    catSlug = catName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                }

                createCategory(catName.trim(), catSlug.trim(), catDesc);
                LOGGER.info("Admin {} created category: {}", admin.getId(), catName);

                if (isApi) {
                    sendSuccess(response, HttpServletResponse.SC_CREATED, null, "Category created successfully");
                } else {
                    redirect(request, response, "/admin/dashboard?categoryCreated=true");
                }
                return;
            }

            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Admin action not found", "NOT_FOUND");

        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private void renderDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userPage = getIntParam(request, "page", 1);
        int userSize = getIntParam(request, "size", 15);

        Map<String, Object> stats = computePlatformStats();
        PageResponse<UserResponse> users = userService.findAllUsers(userPage, userSize);
        PageResponse<OrderResponse> recentOrders = orderService.getAllOrders(1, 10);
        List<Map<String, Object>> bestSellers = orderService.getBestSellingProducts(5);
        List<ProductResponse> lowStockProducts = productService.getLowStockAlerts(20);
        PageResponse<ProductResponse> allProducts = productService.getAllProducts(1, 25);
        List<String> categories = productService.getCategories();

        request.setAttribute("stats", stats != null ? stats : Collections.emptyMap());
        request.setAttribute("usersPage", users);
        request.setAttribute("recentOrders", recentOrders != null ? recentOrders.getContent() : Collections.emptyList());
        request.setAttribute("bestSellers", bestSellers != null ? bestSellers : Collections.emptyList());
        request.setAttribute("lowStockProducts", lowStockProducts != null ? lowStockProducts : Collections.emptyList());
        request.setAttribute("allProducts", allProducts != null ? allProducts.getContent() : Collections.emptyList());
        request.setAttribute("categories", categories != null ? categories : Collections.emptyList());

        forwardToJsp(request, response, "admin/dashboard");
    }

    private void renderAnalytics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> stats = computePlatformStats();
        Map<String, Object> salesSummary = orderService.getSalesAnalytics();
        List<Map<String, Object>> dailySales = orderService.getDailySales(14);
        List<Map<String, Object>> bestSellers = orderService.getBestSellingProducts(10);
        List<ProductResponse> lowStock = productService.getLowStockAlerts(15);
        List<String> categories = productService.getCategories();

        request.setAttribute("stats", stats != null ? stats : Collections.emptyMap());
        request.setAttribute("salesSummary", salesSummary != null ? salesSummary : Collections.emptyMap());
        request.setAttribute("dailySales", dailySales != null ? dailySales : Collections.emptyList());
        request.setAttribute("bestSellers", bestSellers != null ? bestSellers : Collections.emptyList());
        request.setAttribute("lowStock", lowStock != null ? lowStock : Collections.emptyList());
        request.setAttribute("categories", categories != null ? categories : Collections.emptyList());

        forwardToJsp(request, response, "admin/analytics");
    }

    private Map<String, Object> computePlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userDAO.count();
        long totalBuyers = userDAO.countByRole(Role.BUYER);
        long totalSellers = userDAO.countByRole(Role.SELLER);
        long totalProducts = productDAO.countAll();
        long totalOrders = orderDAO.countAll();
        BigDecimal totalRevenue = orderDAO.calculateTotalRevenue();

        stats.put("totalUsers", totalUsers);
        stats.put("totalBuyers", totalBuyers);
        stats.put("totalSellers", totalSellers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        return stats;
    }

    private void createCategory(String name, String slug, String description) {
        String checkSql = "SELECT COUNT(*) FROM categories WHERE name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setString(1, name);
            try (java.sql.ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    String updateSql = "UPDATE categories SET slug = ?, description = ? WHERE name = ?";
                    try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                        psUp.setString(1, slug);
                        psUp.setString(2, description);
                        psUp.setString(3, name);
                        psUp.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO categories (name, slug, description, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setString(1, name);
                        psIns.setString(2, slug);
                        psIns.setString(3, description);
                        psIns.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create category {}: {}", name, e.getMessage(), e);
            throw new ValidationException("Failed to create category: " + e.getMessage());
        }
    }
}
