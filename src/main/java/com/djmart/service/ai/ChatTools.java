package com.djmart.service.ai;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.model.Order;
import com.djmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Approved backend tools service for the DJ Mart AI Chatbot.
 * Provides backend-controlled database lookups and domain knowledge queries.
 * Models and conversational engines are restricted to calling these approved methods,
 * completely preventing arbitrary SQL execution or invented catalog data.
 */
public class ChatTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatTools.class);

    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;

    public ChatTools(ProductDAO productDAO, OrderDAO orderDAO) {
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
    }

    /**
     * Searches database for products with price at or below given budget.
     */
    public List<Product> getProductsUnderBudget(BigDecimal maxPrice, String category) {
        if (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) <= 0) {
            maxPrice = new BigDecimal("5000.00");
        }
        return productDAO.search(null, category, BigDecimal.ZERO, maxPrice, "price", "ASC", 0, 5);
    }

    /**
     * Searches database by product name, brand, or specification keyword.
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return productDAO.search(keyword.trim(), null, null, null, "price", "ASC", 0, 5);
    }

    /**
     * Finds top products in a specific category.
     */
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return productDAO.findByCategory(category.trim(), 0, 5);
    }

    /**
     * Looks up specific product details by name or keyword match.
     */
    public Optional<Product> findProductByTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return Optional.empty();
        }
        List<Product> matches = productDAO.search(term.trim(), null, null, null, "created_at", "DESC", 0, 1);
        if (!matches.isEmpty()) {
            return Optional.of(matches.get(0));
        }
        return Optional.empty();
    }

    /**
     * Compares two products by query terms.
     */
    public Map<String, Product> compareTwoProducts(String term1, String term2) {
        Map<String, Product> result = new LinkedHashMap<>();
        findProductByTerm(term1).ifPresent(p -> result.put("product1", p));
        findProductByTerm(term2).ifPresent(p -> result.put("product2", p));
        return result;
    }

    /**
     * Checks order status for a given user or order ID.
     */
    public Optional<Order> getOrderStatus(Long orderId, Long userId) {
        if (orderId == null) {
            return Optional.empty();
        }
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // If authenticated, ensure the order belongs to this buyer
            if (userId != null && !order.getBuyerId().equals(userId)) {
                LOGGER.warn("User {} attempted to inspect order {} belonging to user {}", userId, orderId, order.getBuyerId());
                return Optional.empty();
            }
            return Optional.of(order);
        }
        return Optional.empty();
    }

    /**
     * Extracts numerical budget amount from a user message (e.g. "under ₹1,000", "below 5000", "under 10k").
     */
    public static BigDecimal extractBudget(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();

        // Check for "k" notation e.g. "10k", "5k"
        Matcher kMatcher = Pattern.compile("(?:under|below|less than|budget of|within)?\\s*(?:₹|rs\\.?|inr)?\\s*(\\d+(?:\\.\\d+)?)\\s*k\\b").matcher(lower);
        if (kMatcher.find()) {
            try {
                double val = Double.parseDouble(kMatcher.group(1)) * 1000.0;
                return BigDecimal.valueOf(val);
            } catch (Exception ignored) {}
        }

        // Check for standard numbers e.g. "under 1000", "under ₹1,000", "below 5000"
        Matcher numMatcher = Pattern.compile("(?:under|below|less than|within|budget of)\\s*(?:₹|rs\\.?|inr)?\\s*(\\d[\\d,]*(?:\\.\\d+)?)").matcher(lower);
        if (numMatcher.find()) {
            try {
                String clean = numMatcher.group(1).replace(",", "");
                return new BigDecimal(clean);
            } catch (Exception ignored) {}
        }

        // Check for general rupee mention
        Matcher generalRupee = Pattern.compile("(?:₹|rs\\.?|inr)\\s*(\\d[\\d,]*(?:\\.\\d+)?)").matcher(lower);
        if (generalRupee.find()) {
            try {
                String clean = generalRupee.group(1).replace(",", "");
                return new BigDecimal(clean);
            } catch (Exception ignored) {}
        }

        return null;
    }
}
