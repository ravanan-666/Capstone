package com.djmart.dao;

import com.djmart.config.DatabaseConfig;
import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.model.Product;
import com.djmart.util.DatabaseUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductDaoTest {

    private static ProductDAO productDAO;

    @BeforeAll
    static void setUpAll() throws Exception {
        DatabaseConfig testConfig = new DatabaseConfig("test-config.properties");
        DatabaseUtil.initDataSource(testConfig);
        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseUtil.applyMigrations(conn);
            DatabaseUtil.applySeedDataIfEmpty(conn);
        }
        productDAO = new ProductDAOImpl();
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseUtil.closeDataSource();
    }

    @Test
    @Order(1)
    @DisplayName("Product CRUD lifecycle")
    void testProductCrud() {
        // Create
        Product product = new Product();
        product.setSellerId(2L);
        product.setName("Gaming Mouse Wireless Pro");
        product.setDescription("Ultra-low latency 2.4GHz gaming mouse with 20K DPI sensor.");
        product.setPrice(new BigDecimal("79.99"));
        product.setStockQty(45);
        product.setCategory("Electronics");
        product.setImageUrl("https://images.example.com/mouse.jpg");

        Product created = productDAO.create(product);
        assertNotNull(created.getId());
        assertEquals("Gaming Mouse Wireless Pro", created.getName());
        assertEquals(new BigDecimal("79.99"), created.getPrice());

        // Read by ID
        Optional<Product> found = productDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(created.getName(), found.get().getName());

        // Update
        found.get().setPrice(new BigDecimal("69.99"));
        found.get().setStockQty(40);
        boolean updated = productDAO.update(found.get());
        assertTrue(updated);

        Optional<Product> reloaded = productDAO.findById(created.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(new BigDecimal("69.99"), reloaded.get().getPrice());
        assertEquals(40, reloaded.get().getStockQty());

        // Delete
        boolean deleted = productDAO.delete(created.getId());
        assertTrue(deleted);
        assertFalse(productDAO.findById(created.getId()).isPresent());
    }

    @Test
    @Order(2)
    @DisplayName("Search products by keyword and category")
    void testProductSearchAndFiltering() {
        // Search keyword "Noise"
        List<Product> results = productDAO.search("Noise", null, null, null, "price", "ASC", 0, 10);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getName().contains("Headphones")));

        // Filter by category "Fashion"
        List<Product> fashion = productDAO.search(null, "Fashion", null, null, "created_at", "DESC", 0, 10);
        assertFalse(fashion.isEmpty());
        assertTrue(fashion.stream().allMatch(p -> "Fashion".equals(p.getCategory())));

        // Filter by price range ($50 to $100)
        List<Product> midPrice = productDAO.search(null, null, new BigDecimal("50.00"), new BigDecimal("100.00"),
                "price", "ASC", 0, 10);
        assertFalse(midPrice.isEmpty());
        for (Product p : midPrice) {
            assertTrue(p.getPrice().compareTo(new BigDecimal("50.00")) >= 0);
            assertTrue(p.getPrice().compareTo(new BigDecimal("100.00")) <= 0);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Sorting products by price ascending and descending")
    void testProductSorting() {
        List<Product> asc = productDAO.search(null, null, null, null, "price", "ASC", 0, 10);
        List<Product> desc = productDAO.search(null, null, null, null, "price", "DESC", 0, 10);

        assertFalse(asc.isEmpty());
        assertFalse(desc.isEmpty());

        for (int i = 0; i < asc.size() - 1; i++) {
            assertTrue(asc.get(i).getPrice().compareTo(asc.get(i + 1).getPrice()) <= 0);
        }

        for (int i = 0; i < desc.size() - 1; i++) {
            assertTrue(desc.get(i).getPrice().compareTo(desc.get(i + 1).getPrice()) >= 0);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Product search pagination and count calculation")
    void testProductPagination() {
        long total = productDAO.countSearch(null, null, null, null);
        assertTrue(total >= 8);

        // Page 1 with size 3
        List<Product> page1 = productDAO.search(null, null, null, null, "created_at", "DESC", 0, 3);
        assertEquals(3, page1.size());

        // Page 2 with size 3
        List<Product> page2 = productDAO.search(null, null, null, null, "created_at", "DESC", 3, 3);
        assertEquals(3, page2.size());

        // Ensure page 1 and page 2 have no overlapping items
        Long page1FirstId = page1.get(0).getId();
        assertFalse(page2.stream().anyMatch(p -> p.getId().equals(page1FirstId)));
    }

    @Test
    @Order(5)
    @DisplayName("SQL-Safe parameter handling (Anti-SQL Injection)")
    void testAntiSqlInjection() {
        // SQL injection payload in search keyword
        String payload = "' OR '1'='1' -- ";
        List<Product> results = productDAO.search(payload, null, null, null, "created_at", "DESC", 0, 10);
        // The query safely treated the payload as literal search text and found no rows
        assertTrue(results.isEmpty());

        // SQL injection attempt in category
        String catPayload = "Electronics' OR 1=1 --";
        List<Product> catResults = productDAO.search(null, catPayload, null, null, "created_at", "DESC", 0, 10);
        assertTrue(catResults.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("Invalid IDs and empty results handling")
    void testInvalidIdsAndEmptyResults() {
        assertFalse(productDAO.findById(null).isPresent());
        assertFalse(productDAO.findById(-999L).isPresent());
        assertFalse(productDAO.findById(999999L).isPresent());

        assertFalse(productDAO.delete(null));
        assertFalse(productDAO.delete(999999L));

        List<Product> emptyCategory = productDAO.findByCategory("NonExistentCategory404", 0, 10);
        assertTrue(emptyCategory.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Stock inventory decrement and update")
    void testStockManagement() throws Exception {
        Product product = productDAO.findById(1L).orElseThrow();
        int initialStock = product.getStockQty();

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Decrement by 2
            boolean decremented = productDAO.decrementStock(conn, 1L, 2);
            assertTrue(decremented);
        }

        Product afterDec = productDAO.findById(1L).orElseThrow();
        assertEquals(initialStock - 2, afterDec.getStockQty());

        // Attempting to decrement more than available stock fails safely
        try (Connection conn = DatabaseUtil.getConnection()) {
            boolean failedDec = productDAO.decrementStock(conn, 1L, initialStock + 500);
            assertFalse(failedDec);
        }
    }
}
