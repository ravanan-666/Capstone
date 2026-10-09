package com.djmart.dao;

import com.djmart.config.DatabaseConfig;
import com.djmart.dao.jdbc.*;
import com.djmart.dto.OrderItemResponse;
import com.djmart.dto.ReviewResponse;
import com.djmart.exception.DatabaseException;
import com.djmart.model.CartItem;
import com.djmart.model.Order;
import com.djmart.model.OrderItem;
import com.djmart.model.OrderStatus;
import com.djmart.model.Product;
import com.djmart.model.Review;
import com.djmart.model.Role;
import com.djmart.model.User;
import com.djmart.util.DatabaseUtil;
import com.djmart.util.PasswordUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MarketplaceDaoIntegrationTest {

    private static UserDAO userDAO;
    private static ProductDAO productDAO;
    private static CartDAO cartDAO;
    private static OrderDAO orderDAO;
    private static OrderItemDAO orderItemDAO;
    private static ReviewDAO reviewDAO;

    @BeforeAll
    static void setUpAll() throws Exception {
        DatabaseConfig testConfig = new DatabaseConfig("test-config.properties");
        DatabaseUtil.initDataSource(testConfig);
        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseUtil.applyMigrations(conn);
            DatabaseUtil.applySeedDataIfEmpty(conn);
        }

        userDAO = new UserDAOImpl();
        productDAO = new ProductDAOImpl();
        cartDAO = new CartDAOImpl();
        orderDAO = new OrderDAOImpl();
        orderItemDAO = new OrderItemDAOImpl();
        reviewDAO = new ReviewDAOImpl();
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseUtil.closeDataSource();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("UserDAO CRUD, email lookup, and constraint validation")
    void testUserDao() {
        // 1. Find existing seed user
        Optional<User> admin = userDAO.findByEmail("admin@djmart.com");
        assertTrue(admin.isPresent());
        assertEquals("System Administrator", admin.get().getName());
        assertEquals(Role.ADMIN, admin.get().getRole());

        // 2. Create new user
        User newUser = new User();
        newUser.setName("Alex Newbuyer");
        newUser.setEmail("alex.newbuyer@example.com");
        newUser.setPasswordHash(PasswordUtil.hashPassword("Password@123"));
        newUser.setRole(Role.BUYER);

        User created = userDAO.create(newUser);
        assertNotNull(created.getId());
        assertEquals("Alex Newbuyer", created.getName());

        // 3. Update profile
        created.setName("Alex Updated");
        assertTrue(userDAO.update(created));
        assertEquals("Alex Updated", userDAO.findById(created.getId()).orElseThrow().getName());

        // 4. Update password
        String newHash = PasswordUtil.hashPassword("NewSecurePassword#99");
        assertTrue(userDAO.updatePassword(created.getId(), newHash));
        assertTrue(PasswordUtil.checkPassword("NewSecurePassword#99",
                userDAO.findById(created.getId()).orElseThrow().getPasswordHash()));

        // 5. Test duplicate email rejection
        User duplicate = new User();
        duplicate.setName("Duplicate User");
        duplicate.setEmail("alex.newbuyer@example.com"); // already exists
        duplicate.setPasswordHash("somehash");
        duplicate.setRole(Role.BUYER);
        assertThrows(DatabaseException.class, () -> userDAO.create(duplicate));

        // 6. Test invalid ID
        assertFalse(userDAO.findById(-1L).isPresent());
        assertFalse(userDAO.findById(null).isPresent());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("CartDAO operations with joined product loading")
    void testCartDao() {
        Long buyerId = 4L; // John Doe
        Long productId = 2L; // Mechanical Keyboard

        // 1. Add item to cart
        cartDAO.addOrUpdateItem(buyerId, productId, 1);

        // 2. Find by user (verifies JOIN loads product details with zero N+1 queries)
        List<CartItem> cartItems = cartDAO.findByUser(buyerId);
        assertFalse(cartItems.isEmpty());

        CartItem item = cartItems.stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst()
                .orElseThrow();
        assertNotNull(item.getProduct());
        assertEquals("Keychron K2 V2 Wireless Mechanical Keyboard", item.getProduct().getName());
        assertEquals(new BigDecimal("7499.00"), item.getProduct().getPrice());

        // 3. Update quantity
        assertTrue(cartDAO.updateQuantity(item.getId(), 3));
        assertEquals(3, cartDAO.findById(item.getId()).orElseThrow().getQuantity());

        // 4. Remove item
        assertTrue(cartDAO.remove(item.getId()));
        assertFalse(cartDAO.findByUserAndProduct(buyerId, productId).isPresent());

        // 5. Clear cart
        cartDAO.addOrUpdateItem(buyerId, 5L, 2);
        assertTrue(cartDAO.clear(buyerId));
        assertTrue(cartDAO.findByUser(buyerId).isEmpty());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Transactional checkout order placement and stock decrement")
    void testTransactionalOrderPlacement() throws Exception {
        Long buyerId = 4L; // John Doe
        Long productId = 5L; // Classic Oxford Cotton Shirt ($45.00)
        int orderQty = 2;

        Product productBefore = productDAO.findById(productId).orElseThrow();
        int stockBefore = productBefore.getStockQty();
        BigDecimal unitPrice = productBefore.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(orderQty));

        // Setup buyer cart
        cartDAO.addOrUpdateItem(buyerId, productId, orderQty);

        Order placedOrder;
        // Atomic Transaction: Order creation + OrderItem insertion + Stock decrement + Cart clear
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Decrement stock
                boolean stockReduced = productDAO.decrementStock(conn, productId, orderQty);
                assertTrue(stockReduced, "Stock decrement must succeed");

                // 2. Create order
                Order order = new Order();
                order.setBuyerId(buyerId);
                order.setStatus(OrderStatus.CONFIRMED);
                order.setTotalAmount(totalAmount);
                order.setShippingAddress("77 Commerce Ave, Metro City");
                placedOrder = orderDAO.create(conn, order);
                assertNotNull(placedOrder.getId());

                // 3. Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(placedOrder.getId());
                orderItem.setProductId(productId);
                orderItem.setQuantity(orderQty);
                orderItem.setUnitPrice(unitPrice);
                orderItemDAO.create(conn, orderItem);

                // 4. Clear cart
                cartDAO.clear(conn, buyerId);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        // Verify state after successful commit
        Product productAfter = productDAO.findById(productId).orElseThrow();
        assertEquals(stockBefore - orderQty, productAfter.getStockQty(), "Product stock must be decremented by order quantity");

        List<CartItem> remainingCart = cartDAO.findByUser(buyerId);
        assertTrue(remainingCart.isEmpty(), "Cart must be cleared after checkout");

        Optional<Order> reloadedOrder = orderDAO.findById(placedOrder.getId());
        assertTrue(reloadedOrder.isPresent());
        assertEquals(OrderStatus.CONFIRMED, reloadedOrder.get().getStatus());
        assertEquals(totalAmount, reloadedOrder.get().getTotalAmount());

        // Verify order items joined query
        List<OrderItemResponse> items = orderItemDAO.findByOrderId(placedOrder.getId());
        assertEquals(1, items.size());
        assertEquals("Classic Oxford Cotton Shirt", items.get(0).getProductName());
        assertEquals(unitPrice, items.get(0).getUnitPrice());
        assertEquals(totalAmount, items.get(0).getSubtotal());

        // Test Order Status Update & Cancellation
        assertTrue(orderDAO.updateStatus(placedOrder.getId(), OrderStatus.SHIPPED));
        assertEquals(OrderStatus.SHIPPED, orderDAO.findById(placedOrder.getId()).orElseThrow().getStatus());

        assertTrue(orderDAO.cancel(placedOrder.getId()));
        assertEquals(OrderStatus.CANCELLED, orderDAO.findById(placedOrder.getId()).orElseThrow().getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("Transaction Rollback: Order fails safely when inventory is insufficient")
    void testTransactionRollbackOnFailure() throws Exception {
        Long buyerId = 4L;
        Long productId = 6L; // Vintage Denim Jacket
        Product product = productDAO.findById(productId).orElseThrow();
        int initialStock = product.getStockQty();
        long initialOrderCount = orderDAO.countAll();

        assertThrows(Exception.class, () -> {
            try (Connection conn = DatabaseUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Attempting to buy more than available stock
                    boolean stockReduced = productDAO.decrementStock(conn, productId, initialStock + 50);
                    if (!stockReduced) {
                        throw new IllegalStateException("Insufficient stock available");
                    }

                    Order order = new Order();
                    order.setBuyerId(buyerId);
                    order.setStatus(OrderStatus.PENDING);
                    order.setTotalAmount(new BigDecimal("999.00"));
                    orderDAO.create(conn, order);

                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        });

        // Verify rollback maintained data integrity
        Product productAfter = productDAO.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfter.getStockQty(), "Stock must remain unchanged after rollback");
        assertEquals(initialOrderCount, orderDAO.countAll(), "No order should be created after rollback");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("Seller Order Visibility: Find incoming orders for products sold by seller")
    void testSellerIncomingOrders() {
        Long sellerId = 2L; // Tech Trends Official
        List<Order> sellerOrders = orderDAO.findRelevantSellerOrders(sellerId, 0, 10);
        assertFalse(sellerOrders.isEmpty(), "Seller must see incoming orders for their products");
        assertTrue(orderDAO.countRelevantSellerOrders(sellerId) > 0);
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("ReviewDAO: Create, joined find by product, average rating, and duplicate rejection")
    void testReviewDao() {
        Long productId = 3L; // Ultra HD 4K IPS Monitor 27"
        Long buyerId = 4L; // John Doe

        // 1. Verify no review yet
        assertFalse(reviewDAO.existsByUserAndProduct(buyerId, productId));

        // 2. Submit review
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(buyerId);
        review.setRating(5);
        review.setComment("Incredible 4K clarity and true color accuracy! Highly recommended.");

        Review created = reviewDAO.create(review);
        assertNotNull(created.getId());
        assertEquals(5, created.getRating());

        // 3. Verify exists check
        assertTrue(reviewDAO.existsByUserAndProduct(buyerId, productId));

        // 4. Calculate average rating
        double avg = reviewDAO.calculateAverageRating(productId);
        assertEquals(5.0, avg, 0.01);

        // 5. Find by product with joined user name (zero N+1 queries)
        List<ReviewResponse> reviews = reviewDAO.findByProduct(productId, 0, 10);
        assertFalse(reviews.isEmpty());
        assertEquals("John Doe", reviews.get(0).getUserName());

        // 6. Test unique constraint: user cannot submit duplicate review for same product
        Review duplicate = new Review();
        duplicate.setProductId(productId);
        duplicate.setUserId(buyerId);
        duplicate.setRating(4);
        duplicate.setComment("Second review attempt");
        assertThrows(DatabaseException.class, () -> reviewDAO.create(duplicate));

        // 7. Delete review
        assertTrue(reviewDAO.delete(created.getId()));
        assertFalse(reviewDAO.existsByUserAndProduct(buyerId, productId));
    }
}
