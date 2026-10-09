package com.djmart.service;

import com.djmart.dao.jdbc.OrderDAOImpl;
import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.model.Product;
import com.djmart.service.ai.ChatTools;
import com.djmart.service.ai.DatabaseAwareChatEngine;
import com.djmart.util.DatabaseUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatbotQueriesTest {

    private static DatabaseAwareChatEngine chatEngine;
    private static ChatTools chatTools;

    @BeforeAll
    static void setUp() throws Exception {
        System.setProperty("djmart.env", "test");
        Connection conn = DatabaseUtil.getConnection();
        DatabaseUtil.applyMigrations(conn);
        DatabaseUtil.applySeedDataIfEmpty(conn);

        // Ensure products 25 and 26 are merged into the database
        ProductDAOImpl productDAO = new ProductDAOImpl();
        if (productDAO.findById(25L).isEmpty()) {
            Product laptop = new Product();
            laptop.setId(25L);
            laptop.setSellerId(2L);
            laptop.setName("ASUS ROG Zephyrus G16 (2024) Gaming Laptop");
            laptop.setDescription("Intel Core Ultra 9 185H, NVIDIA GeForce RTX 4070 8GB, 16\" 2.5K 240Hz OLED Display, 32GB LPDDR5X RAM, 1TB PCIe 4.0 SSD, Eclipse Gray.");
            laptop.setPrice(new BigDecimal("179990.00"));
            laptop.setOriginalPrice(new BigDecimal("199990.00"));
            laptop.setStockQty(10);
            laptop.setCategory("Electronics");
            laptop.setBrand("ASUS");
            laptop.setSku("ASUS-ROG-G16-OLED");
            laptop.setIsActive(true);
            productDAO.create(conn, laptop);
        }

        if (productDAO.findById(26L).isEmpty()) {
            Product camera = new Product();
            camera.setId(26L);
            camera.setSellerId(2L);
            camera.setName("Sony Alpha 7 IV Full-Frame Mirrorless Camera");
            camera.setDescription("33MP Full-Frame Exmor R CMOS Sensor, 4K 60p 10-bit video, 759-point Fast Hybrid AF, with 28-70mm Lens Kit and 5-axis Optical Stabilization.");
            camera.setPrice(new BigDecimal("242490.00"));
            camera.setOriginalPrice(new BigDecimal("262490.00"));
            camera.setStockQty(8);
            camera.setCategory("Electronics");
            camera.setBrand("Sony");
            camera.setSku("SONY-A7M4-LENSKIT");
            camera.setIsActive(true);
            productDAO.create(conn, camera);
        }

        chatTools = new ChatTools(productDAO, new OrderDAOImpl());
        chatEngine = new DatabaseAwareChatEngine(chatTools);
    }

    @AfterAll
    static void tearDown() {
        DatabaseUtil.closeDataSource();
    }

    @Test
    void test15ChatbotQueriesInSequence() {
        String sessionId = "test-session-15";

        // 1. Greeting
        String r1 = chatEngine.getReply(sessionId, "Hello there! What can you help me with?", null, "");
        assertNotNull(r1);
        assertTrue(r1.contains("Welcome to **DJ Mart**"), "Should give welcome greeting");

        // 2. Search for wireless headphones
        String r2 = chatEngine.getReply(sessionId, "Do you sell any wireless headphones?", null, "");
        assertNotNull(r2);
        assertTrue(r2.toLowerCase().contains("sony") || r2.toLowerCase().contains("boat") || r2.toLowerCase().contains("airpods"), "Should find headphones: " + r2);

        // 3. Exact price of Sony WH-1000XM5
        String r3 = chatEngine.getReply(sessionId, "What is the exact price of the Sony WH-1000XM5?", null, "");
        assertNotNull(r3);
        assertTrue(r3.contains("29,990") || r3.contains("Sony WH-1000XM5"), "Should return Sony WH-1000XM5 price: " + r3);

        // 4. Stock of Keychron K2 keyboard
        String r4 = chatEngine.getReply(sessionId, "Is the Keychron K2 keyboard currently in stock?", null, "");
        assertNotNull(r4);
        assertTrue(r4.toLowerCase().contains("in stock") && r4.contains("Keychron"), "Should verify Keychron stock: " + r4);

        // 5. Compare Sony headphones with boAt earbuds
        String r5 = chatEngine.getReply(sessionId, "Compare the Sony headphones with boAt earbuds", null, "");
        assertNotNull(r5);
        assertTrue(r5.contains("Product Comparison") && r5.toLowerCase().contains("sony") && r5.toLowerCase().contains("boat"), "Should compare Sony and boAt: " + r5);

        // 6. Products under 2000 rupees
        String r6 = chatEngine.getReply(sessionId, "What products do you have under 2000 rupees?", null, "");
        assertNotNull(r6);
        assertTrue(r6.contains("under") || r6.toLowerCase().contains("in stock"), "Should list items under 2000: " + r6);

        // 7. Books & Stationery category
        String r7 = chatEngine.getReply(sessionId, "Show me items in Books & Stationery", null, "");
        assertNotNull(r7);
        assertTrue(r7.contains("Books & Stationery"), "Should return Books & Stationery: " + r7);

        // 8. Top laptop and camera options over 1 lakh
        String r8 = chatEngine.getReply(sessionId, "What are your top laptop and camera options over 1 lakh?", null, "");
        assertNotNull(r8);
        assertTrue(r8.toLowerCase().contains("laptop") || r8.toLowerCase().contains("asus") || r8.toLowerCase().contains("camera") || r8.toLowerCase().contains("sony"), "Should return laptop and camera over 1 lakh: " + r8);

        // 9. Contextual follow-up: tell me more about the first item
        String r9 = chatEngine.getReply(sessionId, "Can you tell me more about the first item you mentioned?", null, "");
        assertNotNull(r9);
        assertTrue(r9.contains("Current Verified Price") || r9.contains("Specifications") || r9.contains("Description"), "Should return detailed specs for first item: " + r9);

        // 10. Return and cancellation policy
        String r10 = chatEngine.getReply(sessionId, "What is your return and cancellation policy?", null, "");
        assertNotNull(r10);
        assertTrue(r10.contains("Return & Refund Policy") && r10.contains("14-Day"), "Should give return policy: " + r10);

        // 11. Shipping and order delivery
        String r11 = chatEngine.getReply(sessionId, "How does shipping and order delivery work?", null, "");
        assertNotNull(r11);
        assertTrue(r11.contains("Shipping & Delivery Information") && r11.contains("Complimentary"), "Should give shipping info: " + r11);

        // 12. Non-shopping inquiry: weather in Paris
        String r12 = chatEngine.getReply(sessionId, "What is the weather in Paris today?", null, "");
        assertNotNull(r12);
        assertTrue(r12.contains("DJ Mart Concierge") && r12.toLowerCase().contains("weather"), "Should courteously clarify shopping focus: " + r12);

        // 13. How do I track my order?
        String r13 = chatEngine.getReply(sessionId, "How do I track my order?", null, "");
        assertNotNull(r13);
        assertTrue(r13.toLowerCase().contains("order id") || r13.toLowerCase().contains("my orders"), "Should give order tracking instructions: " + r13);

        // 14. Empty message handled by processMessage or getReply
        String r14 = chatEngine.getReply(sessionId, "", null, "");
        assertNotNull(r14);
        assertTrue(r14.contains("DJ Mart"), "Empty prompt handled gracefully: " + r14);

        // 15. Show me everything from Nike
        String r15 = chatEngine.getReply(sessionId, "Show me everything from Nike", null, "");
        assertNotNull(r15);
        assertTrue(r15.toUpperCase().contains("NIKE") && r15.toLowerCase().contains("air force"), "Should list Nike items: " + r15);
    }
}
