package com.djmart;

import com.djmart.config.DatabaseConfig;
import com.djmart.dao.*;
import com.djmart.dao.jdbc.*;
import com.djmart.dto.*;
import com.djmart.model.OrderStatus;
import com.djmart.model.Role;
import com.djmart.service.*;
import com.djmart.service.impl.*;
import com.djmart.util.DatabaseUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End integration test suite auditing complete Buyer, Seller, and Admin flows
 * on top of the real H2 in-memory database and HikariCP connection pool.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndFlowIntegrationTest {

    private static UserDAO userDAO;
    private static ProductDAO productDAO;
    private static CartDAO cartDAO;
    private static OrderDAO orderDAO;
    private static OrderItemDAO orderItemDAO;
    private static ReviewDAO reviewDAO;

    private static AuthService authService;
    private static UserService userService;
    private static ProductService productService;
    private static CartService cartService;
    private static OrderService orderService;
    private static ReviewService reviewService;

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

        authService = new AuthServiceImpl(userDAO);
        userService = new UserServiceImpl(userDAO);
        reviewService = new ReviewServiceImpl(reviewDAO, productDAO, orderDAO, orderItemDAO);
        productService = new ProductServiceImpl(productDAO, reviewDAO);
        cartService = new CartServiceImpl(cartDAO, productDAO);
        orderService = new OrderServiceImpl(orderDAO, orderItemDAO, productDAO, cartDAO);
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseUtil.closeDataSource();
    }

    @Test
    @Order(1)
    @DisplayName("BUYER FLOW: Registration -> Login -> Browse -> Search -> Filter -> Sort -> Details -> Cart -> Update -> Checkout -> History -> Review")
    void testCompleteBuyerFlow() {
        // 1. Registration
        String buyerEmail = "testbuyer" + System.currentTimeMillis() + "@example.com";
        RegisterRequest regReq = new RegisterRequest("Test Buyer", buyerEmail, "StrongP@ss1", "StrongP@ss1", "BUYER");
        UserResponse registered = authService.register(regReq);
        assertNotNull(registered.getId());
        assertEquals(Role.BUYER, registered.getRole());

        // 2. Login
        LoginRequest loginReq = new LoginRequest(buyerEmail, "StrongP@ss1");
        UserResponse loggedIn = authService.login(loginReq);
        assertEquals(registered.getId(), loggedIn.getId());

        // 3. Browse catalog
        PageResponse<ProductResponse> allProducts = productService.searchProducts(
                null, null, null, null, "id", "desc", 1, 10);
        assertFalse(allProducts.getContent().isEmpty(), "Catalog should contain seed products");

        // 4. Search
        PageResponse<ProductResponse> searchResult = productService.searchProducts(
                "Watch", null, null, null, "id", "desc", 1, 10);
        assertNotNull(searchResult);

        // 5. Filter by category
        PageResponse<ProductResponse> filterResult = productService.searchProducts(
                null, "Electronics", null, null, "id", "desc", 1, 10);
        assertNotNull(filterResult);

        // 6. Sort
        PageResponse<ProductResponse> sortResult = productService.searchProducts(
                null, null, null, null, "price", "asc", 1, 10);
        assertNotNull(sortResult);

        // 7. Product details
        ProductResponse chosenProduct = allProducts.getContent().get(0);
        ProductResponse details = productService.getProductById(chosenProduct.getId());
        assertEquals(chosenProduct.getId(), details.getId());
        int initialStock = details.getStockQty();
        assertTrue(initialStock >= 2, "Seed product should have at least 2 in stock");

        // 8. Add to cart
        cartService.addToCart(loggedIn.getId(), chosenProduct.getId(), 1);
        CartResponse cart = cartService.getCart(loggedIn.getId());
        assertEquals(1, cart.getItemCount());
        assertEquals(1, cart.getItems().get(0).getQuantity());

        // 9. Update quantity
        Long cartItemId = cart.getItems().get(0).getId();
        cartService.updateQuantity(loggedIn.getId(), cartItemId, 2);
        CartResponse updatedCart = cartService.getCart(loggedIn.getId());
        assertEquals(2, updatedCart.getItemCount());
        assertEquals(2, updatedCart.getItems().get(0).getQuantity());

        // 10. Checkout (Server-authoritative atomic transaction)
        CheckoutRequest checkoutReq = new CheckoutRequest("42 Royal Enclave, Chennai 600025", true);
        OrderResponse order = orderService.checkout(loggedIn.getId(), checkoutReq);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(2, order.getItems().get(0).getQuantity());

        // Stock must have been reduced by 2
        ProductResponse productAfterOrder = productService.getProductById(chosenProduct.getId());
        assertEquals(initialStock - 2, productAfterOrder.getStockQty());

        // Cart must be emptied
        CartResponse emptyCart = cartService.getCart(loggedIn.getId());
        assertEquals(0, emptyCart.getItemCount());

        // 11. Order history
        PageResponse<OrderResponse> buyerOrders = orderService.getOrdersByBuyer(loggedIn.getId(), 1, 10);
        assertEquals(1, buyerOrders.getTotalElements());
        assertEquals(order.getId(), buyerOrders.getContent().get(0).getId());

        // 12. Review eligibility before delivery (should not be eligible)
        boolean eligibleBeforeDelivery = reviewService.isEligibleToReview(loggedIn.getId(), chosenProduct.getId());
        assertFalse(eligibleBeforeDelivery, "Buyer cannot review an undelivered order");

        // Advance order to DELIVERED (by admin or seller)
        orderService.updateOrderStatus(order.getId(), OrderStatus.SHIPPED, 1L, Role.ADMIN);
        orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED, 1L, Role.ADMIN);

        // Now buyer should be eligible to review
        boolean eligibleAfterDelivery = reviewService.isEligibleToReview(loggedIn.getId(), chosenProduct.getId());
        assertTrue(eligibleAfterDelivery, "Buyer should be eligible to review after order is delivered");

        // 13. Submit review
        ReviewRequest revReq = new ReviewRequest(chosenProduct.getId(), 5, "Remarkable craftsmanship and swift delivery.");
        ReviewResponse review = reviewService.addReview(loggedIn.getId(), revReq);
        assertNotNull(review.getId());
        assertEquals(5, review.getRating());

        // Duplicate review should be prevented
        assertFalse(reviewService.isEligibleToReview(loggedIn.getId(), chosenProduct.getId()));
    }

    @Test
    @Order(2)
    @DisplayName("SELLER FLOW: Login -> Dashboard Stats -> Add Product -> Edit Product -> Stock Update -> Relevant Orders -> Order Status Transition")
    void testCompleteSellerFlow() {
        // 1. Login as Seller (seed user seller.tech@djmart.com)
        LoginRequest sellerLogin = new LoginRequest("seller.tech@djmart.com", "Password@123");
        UserResponse seller = authService.login(sellerLogin);
        assertEquals(Role.SELLER, seller.getRole());

        // 2. Seller dashboard metrics
        long initialProductCount = productDAO.countBySeller(seller.getId());
        long orderCount = orderDAO.countRelevantSellerOrders(seller.getId());
        long lowStockCount = productDAO.countLowStockBySeller(seller.getId(), 5);
        BigDecimal sellerRevenue = orderItemDAO.calculateSellerRevenue(seller.getId());

        assertTrue(initialProductCount >= 0);
        assertTrue(orderCount >= 0);
        assertTrue(lowStockCount >= 0);
        assertNotNull(sellerRevenue);

        // 3. Add product
        ProductRequest newProd = new ProductRequest("Handcrafted Brass Lamp",
                "Solid cast brass archival table lighting",
                new BigDecimal("4800.00"), 8, "Living", "https://images.unsplash.com/photo-lamp");
        ProductResponse createdProd = productService.createProduct(seller.getId(), newProd);
        assertNotNull(createdProd.getId());
        assertEquals("Handcrafted Brass Lamp", createdProd.getName());
        assertEquals(initialProductCount + 1, productDAO.countBySeller(seller.getId()));

        // 4. Edit product & Stock update
        ProductRequest editReq = new ProductRequest("Handcrafted Brass Lamp Mk II",
                "Updated solid cast brass table lighting",
                new BigDecimal("5200.00"), 12, "Living", "https://images.unsplash.com/photo-lamp2");
        ProductResponse updatedProd = productService.updateProduct(seller.getId(), createdProd.getId(), editReq, false);
        assertEquals("Handcrafted Brass Lamp Mk II", updatedProd.getName());
        assertEquals(12, updatedProd.getStockQty());
        assertEquals(new BigDecimal("5200.00"), updatedProd.getPrice());

        // 5. Relevant order viewing
        PageResponse<OrderResponse> sellerOrders = orderService.getOrdersBySeller(seller.getId(), 1, 10);
        assertNotNull(sellerOrders);

        // 6. Delete product
        boolean deleted = productService.deleteProduct(seller.getId(), createdProd.getId(), false);
        assertTrue(deleted);
        assertEquals(initialProductCount, productDAO.countBySeller(seller.getId()));
    }

    @Test
    @Order(3)
    @DisplayName("ADMIN FLOW: Login -> Admin Dashboard -> User Management -> Orders Oversight -> Platform Statistics")
    void testCompleteAdminFlow() {
        // 1. Login as Admin (seed user admin@djmart.com)
        LoginRequest adminLogin = new LoginRequest("admin@djmart.com", "Password@123");
        UserResponse admin = authService.login(adminLogin);
        assertEquals(Role.ADMIN, admin.getRole());

        // 2. User management
        PageResponse<UserResponse> users = userService.findAllUsers(1, 10);
        assertTrue(users.getTotalElements() >= 3, "Should have admin, seller, buyer seed users");

        // 3. Orders oversight
        PageResponse<OrderResponse> allOrders = orderService.getAllOrders(1, 10);
        assertNotNull(allOrders);

        // 4. Platform statistics
        long totalUsers = userDAO.count();
        long buyerCount = userDAO.countByRole(Role.BUYER);
        long sellerCount = userDAO.countByRole(Role.SELLER);
        long totalProducts = productDAO.countAll();
        long totalOrders = orderDAO.countAll();
        BigDecimal gmv = orderDAO.calculateTotalRevenue();

        assertTrue(totalUsers >= 3);
        assertTrue(buyerCount >= 1);
        assertTrue(sellerCount >= 1);
        assertTrue(totalProducts >= 1);
        assertNotNull(gmv);
    }
}
