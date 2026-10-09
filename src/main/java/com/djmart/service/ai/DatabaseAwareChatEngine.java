package com.djmart.service.ai;

import com.djmart.model.Order;
import com.djmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent Database-Connected Conversational Engine for DJ Mart.
 * Understands customer intents, queries the live PostgreSQL / H2 database via approved tools,
 * maintains conversation context and history, and produces contextually accurate responses.
 * Operates with full database fidelity both offline and as the tool execution layer for LLMs.
 */
public class DatabaseAwareChatEngine implements ChatProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAwareChatEngine.class);
    private static final Locale INDIA_LOCALE = new Locale("en", "IN");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(INDIA_LOCALE);

    private final ChatTools chatTools;
    private final Map<String, List<ChatMessageRecord>> conversationHistories = new HashMap<>();
    private final Map<String, Long> lastReferencedProducts = new HashMap<>();

    public DatabaseAwareChatEngine(ChatTools chatTools) {
        this.chatTools = chatTools;
    }

    @Override
    public String getReply(String userMessage, String context) {
        return getReply("default", userMessage, null, context);
    }

    public String getReply(String sessionId, String userMessage, Long authenticatedUserId, String context) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Greetings from **DJ Mart Concierge**. How may I assist your shopping journey today? You can ask about our catalog, verified prices, live stock, or order status.";
        }

        String safeSessionId = sessionId != null ? sessionId : "default";
        String msg = userMessage.trim();
        String lower = msg.toLowerCase(Locale.ROOT);

        // Record user turn in conversation history
        recordMessage(safeSessionId, "user", msg);

        String reply;

        // 1. GREETINGS
        if (isGreeting(lower)) {
            reply = "Hello! Welcome to **DJ Mart**. I am your live AI shopping assistant. " +
                    "I can help you search products, find items within your budget, check live stock and verified prices, " +
                    "or track your orders. What are you looking for today?";
        }
        // 2. BUDGET SEARCH (e.g. "under ₹1,000", "products below 5000", "under 10k")
        else if (isBudgetInquiry(lower)) {
            BigDecimal budget = ChatTools.extractBudget(lower);
            if (budget == null) {
                budget = new BigDecimal("2000.00");
            }
            String category = extractCategory(lower);
            List<Product> matches = chatTools.getProductsUnderBudget(budget, category);
            reply = formatBudgetResults(matches, budget, safeSessionId);
        }
        // 3. PRODUCT PRICE CHECK (e.g. "What is the price of Sony headphones?", "How much is Keychron?")
        else if (isPriceInquiry(lower)) {
            reply = handlePriceInquiry(lower, safeSessionId);
        }
        // 4. STOCK AVAILABILITY CHECK (e.g. "Is this item available?", "Is Dell monitor in stock?")
        else if (isStockInquiry(lower)) {
            reply = handleStockInquiry(lower, safeSessionId);
        }
        // 5. PRODUCT COMPARISON (e.g. "Compare Sony headphones and Apple AirPods Pro")
        else if (isComparisonInquiry(lower)) {
            reply = handleComparison(lower, safeSessionId);
        }
        // 6. CATEGORY DISCOVERY (e.g. "Show me electronics", "Show me fashion products")
        else if (isCategoryInquiry(lower)) {
            String category = extractCategory(lower);
            if (category == null) category = "Electronics";
            List<Product> products = chatTools.getProductsByCategory(category);
            reply = formatCategoryResults(products, category, safeSessionId);
        }
        // 7. ORDER STATUS & TRACKING (e.g. "Track my order", "Where is my order #1?")
        else if (isOrderTracking(lower)) {
            reply = handleOrderTracking(lower, authenticatedUserId);
        }
        // 8. SHIPPING & DELIVERY POLICIES
        else if (isShippingInquiry(lower)) {
            reply = "🚚 **DJ Mart Shipping & Delivery Information:**\n\n" +
                    "• **Complimentary Shipping:** Free express shipping on all orders across India.\n" +
                    "• **Transit Time:** Delivery within **2 to 4 business days** via top courier partners.\n" +
                    "• **Real-Time Tracking:** Once your order is dispatched, track its milestone status directly in the **My Orders** section.";
        }
        // 9. RETURN & REFUND POLICIES
        else if (isReturnInquiry(lower)) {
            reply = "📦 **DJ Mart Return & Refund Policy:**\n\n" +
                    "• **14-Day Guarantee:** We uphold a **14-day hassle-free return window** on all unblemished items in original packaging.\n" +
                    "• **Direct Request:** You can initiate a return or cancellation directly from your **Order Details** page.\n" +
                    "• **Swift Refunds:** Approved refunds are processed to your original payment method or UPI account within 3–5 business days.";
        }
        // 10. PAYMENT METHODS
        else if (isPaymentInquiry(lower)) {
            reply = "💳 **Accepted Payment Methods on DJ Mart:**\n\n" +
                    "• **Instant UPI & QR:** Google Pay, PhonePe, Paytm, BHIM\n" +
                    "• **Cards:** Credit and Debit cards (Visa, MasterCard, RuPay, Maestro)\n" +
                    "• **Net Banking:** Supported across all major Indian banks\n" +
                    "• **Cash on Delivery (COD):** Available for verified domestic delivery addresses\n\n" +
                    "All payments are encrypted and validated server-side.";
        }
        // 11. GENERAL PRODUCT SEARCH
        else {
            reply = handleGeneralSearch(lower, safeSessionId);
        }

        // Record bot response in conversation history
        recordMessage(safeSessionId, "bot", reply);

        return reply;
    }

    private boolean isGreeting(String lower) {
        return lower.matches("^(hi|hello|hey|greetings|good\\s*(morning|afternoon|evening)|namaste|start|help)(\\s+.*|!|\\.)?$");
    }

    private boolean isBudgetInquiry(String lower) {
        return lower.contains("under") || lower.contains("below") || lower.contains("budget") ||
                lower.contains("less than") || lower.contains("cheapest") || lower.contains("affordable");
    }

    private boolean isPriceInquiry(String lower) {
        return lower.contains("price") || lower.contains("cost") || lower.contains("how much") ||
                lower.contains("rate") || lower.contains("mrp");
    }

    private boolean isStockInquiry(String lower) {
        return lower.contains("available") || lower.contains("in stock") || lower.contains("stock") ||
                lower.contains("left") || lower.contains("sold out") || lower.contains("units");
    }

    private boolean isComparisonInquiry(String lower) {
        return lower.contains("compare") || lower.contains("difference between") ||
                lower.contains("vs") || lower.contains("which is better");
    }

    private boolean isCategoryInquiry(String lower) {
        return lower.contains("category") || lower.contains("electronics") || lower.contains("fashion") ||
                lower.contains("home") || lower.contains("stationery") || lower.contains("wellness") ||
                lower.contains("show me") || lower.contains("browse");
    }

    private boolean isOrderTracking(String lower) {
        return lower.contains("order") && (lower.contains("track") || lower.contains("status") ||
                lower.contains("where") || lower.contains("history") || lower.contains("check"));
    }

    private boolean isShippingInquiry(String lower) {
        return lower.contains("shipping") || lower.contains("delivery") || lower.contains("courier") ||
                lower.contains("dispatch") || lower.contains("when will i receive");
    }

    private boolean isReturnInquiry(String lower) {
        return lower.contains("return") || lower.contains("refund") || lower.contains("cancel") ||
                lower.contains("cancellation") || lower.contains("exchange");
    }

    private boolean isPaymentInquiry(String lower) {
        return lower.contains("payment") || lower.contains("pay") || lower.contains("upi") ||
                lower.contains("card") || lower.contains("cod") || lower.contains("cash on delivery");
    }

    private String extractCategory(String lower) {
        if (lower.contains("electronic") || lower.contains("audio") || lower.contains("gadget")) return "Electronics";
        if (lower.contains("fashion") || lower.contains("cloth") || lower.contains("wear") || lower.contains("apparel")) return "Fashion";
        if (lower.contains("home") || lower.contains("kitchen") || lower.contains("cook")) return "Home & Kitchen";
        if (lower.contains("book") || lower.contains("stationery") || lower.contains("pen") || lower.contains("journal")) return "Books & Stationery";
        if (lower.contains("wellness") || lower.contains("fitness") || lower.contains("yoga") || lower.contains("gym")) return "Wellness & Fitness";
        return null;
    }

    private String formatPrice(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return "₹" + String.format("%,.2f", amount);
    }

    private String formatBudgetResults(List<Product> products, BigDecimal budget, String sessionId) {
        if (products.isEmpty()) {
            return "I searched our live catalog, but couldn't find products under " + formatPrice(budget) +
                    ". Our entry products start from ₹375.00 (e.g. Parker Jotter Ballpoint Pen) and ₹999.00 (Milton Thermosteel Flask). Would you like to explore those?";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Here are our top available products under **").append(formatPrice(budget)).append("** directly from our verified database:\n\n");
        for (Product p : products) {
            lastReferencedProducts.put(sessionId, p.getId());
            sb.append("• **[").append(p.getName()).append("](/products/").append(p.getId()).append(")** — ")
              .append(formatPrice(p.getPrice()))
              .append(" (").append(p.getStockQty() > 0 ? "In Stock: " + p.getStockQty() + " units" : "Out of Stock").append(")\n")
              .append("  *Category: ").append(p.getCategory()).append(" | Brand: ").append(p.getBrand() != null ? p.getBrand() : "Artisan").append("*\n\n");
        }
        sb.append("Would you like more details on any of these items, or should I refine by category?");
        return sb.toString();
    }

    private String handlePriceInquiry(String lower, String sessionId) {
        // Look up by term in message
        String cleanTerm = extractSearchTerm(lower, "price of", "how much is", "cost of", "price for", "what is");
        Optional<Product> productOpt = Optional.empty();

        if (!cleanTerm.isEmpty()) {
            productOpt = chatTools.findProductByTerm(cleanTerm);
        }

        // Contextual follow-up: If no product term was specified, use last referenced product
        if (productOpt.isEmpty() && lastReferencedProducts.containsKey(sessionId)) {
            Long lastId = lastReferencedProducts.get(sessionId);
            productOpt = chatTools.findProductByTerm(String.valueOf(lastId));
        }

        if (productOpt.isPresent()) {
            Product p = productOpt.get();
            lastReferencedProducts.put(sessionId, p.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("The current verified market price of **[").append(p.getName()).append("](/products/").append(p.getId()).append(")** is **")
              .append(formatPrice(p.getPrice())).append("**.");
            if (p.getOriginalPrice() != null && p.getOriginalPrice().compareTo(p.getPrice()) > 0) {
                sb.append(" *(Original MRP: ").append(formatPrice(p.getOriginalPrice())).append(")*");
            }
            sb.append("\n\n• **Stock Status:** ").append(p.getStockQty() > 0 ? "Available (" + p.getStockQty() + " units in stock)" : "Sold out")
              .append("\n• **Brand:** ").append(p.getBrand() != null ? p.getBrand() : "Curated Partner")
              .append("\n• **SKU:** ").append(p.getSku() != null ? p.getSku() : "DJM-PROD-" + p.getId())
              .append("\n• **Verification:** Verified by DJ Mart Catalog Administration in INR.")
              .append("\n\nWould you like to add this to your cart?");
            return sb.toString();
        }

        return "Could you specify which product's price you'd like to check? For example, you can ask *'What is the price of Sony headphones?'* or *'How much is Keychron keyboard?'*";
    }

    private String handleStockInquiry(String lower, String sessionId) {
        String cleanTerm = extractSearchTerm(lower, "is", "available", "in stock", "stock of", "how many");
        Optional<Product> productOpt = Optional.empty();

        if (!cleanTerm.isEmpty()) {
            productOpt = chatTools.findProductByTerm(cleanTerm);
        }

        if (productOpt.isEmpty() && lastReferencedProducts.containsKey(sessionId)) {
            Long lastId = lastReferencedProducts.get(sessionId);
            productOpt = chatTools.findProductByTerm(String.valueOf(lastId));
        }

        if (productOpt.isPresent()) {
            Product p = productOpt.get();
            lastReferencedProducts.put(sessionId, p.getId());
            if (p.getStockQty() > 0) {
                return "Yes! **[" + p.getName() + "](/products/" + p.getId() + ")** is currently **in stock** with **" +
                        p.getStockQty() + " units available** at **" + formatPrice(p.getPrice()) + "**. You can order now for complimentary 2-4 day express dispatch.";
            } else {
                return "Currently, **[" + p.getName() + "](/products/" + p.getId() + ")** is **out of stock**. Our merchants replenish stock regularly. Would you like me to recommend similar products in " + p.getCategory() + "?";
            }
        }

        return "Please tell me which product you are inquiring about, e.g., *'Is the 4K monitor in stock?'* or *'Is Keychron keyboard available?'*";
    }

    private String handleComparison(String lower, String sessionId) {
        // Attempt to extract two product terms
        String[] parts = lower.split("(?:and|vs|versus|with)");
        if (parts.length >= 2) {
            String term1 = parts[0].replaceAll("^(compare|difference between|whats the difference between)", "").trim();
            String term2 = parts[1].replaceAll("(which is better|which one is better|please)", "").trim();

            Optional<Product> p1Opt = chatTools.findProductByTerm(term1);
            Optional<Product> p2Opt = chatTools.findProductByTerm(term2);

            if (p1Opt.isPresent() && p2Opt.isPresent()) {
                Product p1 = p1Opt.get();
                Product p2 = p2Opt.get();
                lastReferencedProducts.put(sessionId, p1.getId());

                return "⚖️ **Product Comparison:**\n\n" +
                        "1. **[" + p1.getName() + "](/products/" + p1.getId() + ")**\n" +
                        "   • Price: **" + formatPrice(p1.getPrice()) + "**\n" +
                        "   • Category: " + p1.getCategory() + " | Brand: " + (p1.getBrand() != null ? p1.getBrand() : "Curated") + "\n" +
                        "   • Availability: " + (p1.getStockQty() > 0 ? "In Stock (" + p1.getStockQty() + " left)" : "Out of Stock") + "\n\n" +
                        "2. **[" + p2.getName() + "](/products/" + p2.getId() + ")**\n" +
                        "   • Price: **" + formatPrice(p2.getPrice()) + "**\n" +
                        "   • Category: " + p2.getCategory() + " | Brand: " + (p2.getBrand() != null ? p2.getBrand() : "Curated") + "\n" +
                        "   • Availability: " + (p2.getStockQty() > 0 ? "In Stock (" + p2.getStockQty() + " left)" : "Out of Stock") + "\n\n" +
                        "**Verdict:** If you are seeking lower cost, [" + (p1.getPrice().compareTo(p2.getPrice()) <= 0 ? p1.getName() : p2.getName()) +
                        "] is the more budget-friendly selection at " + formatPrice(p1.getPrice().min(p2.getPrice())) + ".";
            }
        }
        return "To compare two products, please ask like: *'Compare Sony headphones and Apple AirPods Pro'* or *'Keychron keyboard vs Logitech mouse'*";
    }

    private String formatCategoryResults(List<Product> products, String category, String sessionId) {
        if (products.isEmpty()) {
            return "Our " + category + " collection is currently being refreshed. Feel free to explore our other categories such as Electronics, Fashion, or Home & Kitchen!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Here are our featured items in **").append(category).append("**:\n\n");
        for (Product p : products) {
            lastReferencedProducts.put(sessionId, p.getId());
            sb.append("• **[").append(p.getName()).append("](/products/").append(p.getId()).append(")** — ")
              .append(formatPrice(p.getPrice()))
              .append(" (").append(p.getStockQty() > 0 ? "In Stock" : "Sold out").append(")\n");
        }
        sb.append("\nClick on any product to inspect detailed specifications and reviews.");
        return sb.toString();
    }

    private String handleOrderTracking(String lower, Long userId) {
        Matcher idMatcher = Pattern.compile("(?:#|order\\s*(?:id|number)?\\s*[:#]?\\s*)(\\d+)").matcher(lower);
        Long orderId = null;
        if (idMatcher.find()) {
            try {
                orderId = Long.parseLong(idMatcher.group(1));
            } catch (Exception ignored) {}
        }

        if (orderId != null) {
            Optional<Order> orderOpt = chatTools.getOrderStatus(orderId, userId);
            if (orderOpt.isPresent()) {
                Order o = orderOpt.get();
                return "📦 **Order Status for Order #" + o.getId() + ":**\n\n" +
                        "• **Fulfillment Status:** `" + o.getStatus() + "`\n" +
                        "• **Total Amount:** " + formatPrice(o.getTotalAmount()) + "\n" +
                        "• **Delivery Address:** " + o.getShippingAddress() + "\n" +
                        "• **Order Date:** " + o.getCreatedAt() + "\n\n" +
                        "You can review complete tracking milestones on your [Order Details](/orders/" + o.getId() + ") page.";
            } else {
                return "I couldn't locate order #" + orderId + ". Please double check your order number or visit the **My Orders** section in the header to view all purchases.";
            }
        }

        return "To track an order, please provide your Order ID (for example: *'Where is my order #1?'*), or head to the **My Orders** tab in your navigation header.";
    }

    private String handleGeneralSearch(String lower, String sessionId) {
        String clean = extractSearchTerm(lower, "show me", "looking for", "find", "search for", "do you have");
        if (clean.isEmpty()) {
            clean = lower;
        }

        List<Product> matches = chatTools.searchProducts(clean);
        if (!matches.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("I found matching products in our database for **\"").append(clean).append("\"**:\n\n");
            for (Product p : matches) {
                lastReferencedProducts.put(sessionId, p.getId());
                sb.append("• **[").append(p.getName()).append("](/products/").append(p.getId()).append(")** — ")
                  .append(formatPrice(p.getPrice())).append(" (").append(p.getCategory()).append(")\n");
            }
            sb.append("\nWould you like more details, pricing, or stock info on any of these?");
            return sb.toString();
        }

        return "I am here to assist! You can ask me:\n\n" +
                "• *\"Show me products under ₹1,000\"*\n" +
                "• *\"What is the price of Sony headphones?\"*\n" +
                "• *\"Is Keychron keyboard in stock?\"*\n" +
                "• *\"Compare Sony headphones and AirPods Pro\"*\n" +
                "• *\"What is your return and shipping policy?\"*\n\n" +
                "What would you like to explore?";
    }

    private String extractSearchTerm(String lower, String... prefixes) {
        String term = lower;
        for (String p : prefixes) {
            if (term.contains(p)) {
                int idx = term.indexOf(p) + p.length();
                term = term.substring(idx).trim();
            }
        }
        term = term.replaceAll("[?!.,]", "").trim();
        return term;
    }

    private void recordMessage(String sessionId, String sender, String text) {
        List<ChatMessageRecord> history = conversationHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(new ChatMessageRecord(sender, text, System.currentTimeMillis()));
        if (history.size() > 30) {
            history.remove(0);
        }
    }

    public static class ChatMessageRecord {
        private final String sender;
        private final String message;
        private final long timestamp;

        public ChatMessageRecord(String sender, String message, long timestamp) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getSender() { return sender; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}
