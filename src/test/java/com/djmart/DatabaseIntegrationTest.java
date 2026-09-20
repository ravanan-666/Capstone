package com.djmart;

import com.djmart.config.DatabaseConfig;
import com.djmart.model.Role;
import com.djmart.util.DatabaseUtil;
import com.djmart.util.PasswordUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseIntegrationTest {

    @BeforeAll
    static void setUpAll() throws Exception {
        // Initialize HikariCP connection pool with in-memory test database
        DatabaseConfig testConfig = new DatabaseConfig("test-config.properties");
        DatabaseUtil.initDataSource(testConfig);

        // Apply migrations and seed data
        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseUtil.applyMigrations(conn);
            DatabaseUtil.applySeedDataIfEmpty(conn);
        }
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseUtil.closeDataSource();
    }

    @Test
    @Order(1)
    @DisplayName("Verify connection acquisition, validity, and leak-free release")
    void testConnectionAcquisitionAndRelease() throws SQLException {
        assertTrue(DatabaseUtil.isPoolInitialized(), "HikariCP pool should be initialized");

        try (Connection conn = DatabaseUtil.getConnection()) {
            assertNotNull(conn, "Obtained connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
            assertTrue(conn.isValid(2), "Connection should be valid");
        }
        // Once out of try-with-resources, connection is automatically returned to pool
    }

    @Test
    @Order(2)
    @DisplayName("Verify all 6 core tables exist in schema metadata")
    void testSchemaTablesExist() throws SQLException {
        Set<String> expectedTables = Set.of(
                "USERS",
                "PRODUCTS",
                "ORDERS",
                "ORDER_ITEMS",
                "CART_ITEMS",
                "REVIEWS"
        );

        Set<String> actualTables = new HashSet<>();
        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actualTables.add(rs.getString("TABLE_NAME").toUpperCase());
                }
            }
        }

        for (String expected : expectedTables) {
            assertTrue(actualTables.contains(expected), "Table " + expected + " should exist in database");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Verify all mandatory foreign-key and operational indexes exist")
    void testIndexesExist() throws SQLException {
        Set<String> expectedIndexes = Set.of(
                "IDX_PRODUCTS_SELLER_ID",
                "IDX_ORDERS_BUYER_ID",
                "IDX_ORDER_ITEMS_ORDER_ID",
                "IDX_ORDER_ITEMS_PRODUCT_ID",
                "IDX_CART_ITEMS_USER_ID",
                "IDX_CART_ITEMS_PRODUCT_ID",
                "IDX_REVIEWS_PRODUCT_ID",
                "IDX_REVIEWS_USER_ID",
                "IDX_PRODUCTS_CATEGORY",
                "IDX_PRODUCTS_NAME",
                "IDX_PRODUCTS_PRICE",
                "IDX_ORDERS_STATUS",
                "IDX_ORDERS_CREATED_AT",
                "IDX_REVIEWS_RATING"
        );

        Set<String> actualIndexes = new HashSet<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_SCHEMA = 'PUBLIC'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("INDEX_NAME");
                if (name != null) {
                    actualIndexes.add(name.toUpperCase());
                }
            }
        }

        for (String expected : expectedIndexes) {
            assertTrue(actualIndexes.contains(expected), "Index " + expected + " should exist in database");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Verify seed data: Admin, Sellers, Buyers, Passwords, and Products")
    void testSeedData() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 1. Verify Admin User
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {
                ps.setString(1, "admin@djmart.com");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Admin user should exist");
                    assertEquals("ADMIN", rs.getString("role"));
                    String hash = rs.getString("password_hash");
                    assertNotNull(hash);
                    assertTrue(PasswordUtil.checkPassword("Password@123", hash), "Admin password should match Password@123");
                }
            }

            // 2. Verify Sellers and Buyers count
            try (PreparedStatement ps = conn.prepareStatement("SELECT role, COUNT(*) FROM users GROUP BY role");
                 ResultSet rs = ps.executeQuery()) {
                int sellerCount = 0;
                int buyerCount = 0;
                int adminCount = 0;
                while (rs.next()) {
                    String role = rs.getString(1);
                    int count = rs.getInt(2);
                    if ("SELLER".equals(role)) sellerCount = count;
                    if ("BUYER".equals(role)) buyerCount = count;
                    if ("ADMIN".equals(role)) adminCount = count;
                }
                assertEquals(1, adminCount, "Should have 1 seed admin");
                assertTrue(sellerCount >= 2, "Should have at least 2 seed sellers");
                assertTrue(buyerCount >= 2, "Should have at least 2 seed buyers");
            }

            // 3. Verify Product Categories & Pricing
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT category), COUNT(*) FROM products");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                int categoriesCount = rs.getInt(1);
                int totalProducts = rs.getInt(2);
                assertTrue(categoriesCount >= 3, "Should have multiple categories");
                assertTrue(totalProducts >= 8, "Should have at least 8 seed products");
            }

            // 4. Verify Sample Orders, Order Items, Cart Items, Reviews
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM orders");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) > 0, "Orders table should contain seed orders");
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM reviews");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) > 0, "Reviews table should contain seed reviews");
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("Verify Unique constraint on email")
    void testUniqueEmailConstraint() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "Duplicate Admin");
            ps.setString(2, "admin@djmart.com"); // Duplicate email
            ps.setString(3, "$2a$10$xyz");
            ps.setString(4, Role.BUYER.name());

            assertThrows(SQLException.class, ps::executeUpdate, "Inserting duplicate email should fail");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Verify Foreign Key constraint on product seller")
    void testForeignKeySellerConstraint() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (?, ?, ?, ?, ?)")) {
            ps.setLong(1, 999999L); // Non-existent user id
            ps.setString(2, "Ghost Product");
            ps.setBigDecimal(3, new BigDecimal("19.99"));
            ps.setInt(4, 10);
            ps.setString(5, "Electronics");

            assertThrows(SQLException.class, ps::executeUpdate, "Inserting product with invalid seller_id should fail");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Verify Check constraint on review rating (1 to 5 only)")
    void testReviewRatingConstraint() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, 1L);
            ps.setLong(2, 4L);
            ps.setInt(3, 7); // Invalid rating > 5
            ps.setString(4, "Invalid rating test");

            assertThrows(SQLException.class, ps::executeUpdate, "Inserting review with rating 7 should violate CHECK constraint");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Verify DECIMAL precision without floating point inaccuracies")
    void testMonetaryDecimalPrecision() throws SQLException {
        BigDecimal exactPrice = new BigDecimal("149.99");
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT price FROM products WHERE id = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                BigDecimal dbPrice = rs.getBigDecimal("price");
                assertEquals(exactPrice, dbPrice, "Monetary amount should match exact BigDecimal value without float rounding");
            }
        }
    }
}
