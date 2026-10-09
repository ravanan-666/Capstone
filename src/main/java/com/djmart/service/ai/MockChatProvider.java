package com.djmart.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Mock implementation of ChatProvider responding to domain-specific e-commerce FAQ queries.
 * Operates offline with zero external network dependencies.
 */
public class MockChatProvider implements ChatProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockChatProvider.class);

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Greetings from DJ Mart Concierge. How may I assist your shopping journey today?";
        }

        String msg = userMessage.trim().toLowerCase(Locale.ROOT);

        if (msg.contains("order") && (msg.contains("track") || msg.contains("status") || msg.contains("where"))) {
            return "You can view the real-time fulfillment status of all your purchases by navigating to 'My Orders' in the top header. Orders progress from Confirmed to Shipped to Delivered.";
        }

        if (msg.contains("cancel") || msg.contains("cancellation")) {
            return "Orders in 'Pending' or 'Confirmed' status can be cancelled directly from your Order Details page. Once an order is dispatched for transit (Shipped), cancellation is no longer possible.";
        }

        if (msg.contains("return") || msg.contains("refund") || msg.contains("policy")) {
            return "DJ Mart upholds a 14-day archival return policy on unblemished merchandise. Simply contact concierge support or submit a return request through your verified order details.";
        }

        if (msg.contains("delivery") || msg.contains("shipping") || msg.contains("time") || msg.contains("courier")) {
            return "All verified orders receive complimentary express courier dispatch. Standard delivery window is 2 to 4 business days across India.";
        }

        if (msg.contains("payment") || msg.contains("pay") || msg.contains("upi") || msg.contains("card")) {
            return "DJ Mart accepts Credit/Debit cards (Visa, MasterCard, RuPay), Instant UPI / QR transfers (GPay, PhonePe, Paytm), Net Banking, and Cash on Delivery (COD).";
        }

        if (msg.contains("seller") || msg.contains("sell") || msg.contains("vendor") || msg.contains("merchant")) {
            return "Independent artisans and verified sellers can join DJ Mart by registering a Seller account. Once registered, the Seller Studio dashboard allows you to publish and manage product listings.";
        }

        if (msg.contains("authentic") || msg.contains("guarantee") || msg.contains("quality")) {
            return "Every article listed on DJ Mart undergoes provenance inspection in strict accordance with Anna University Capstone standards to guarantee authenticity.";
        }

        if (msg.contains("review") || msg.contains("rating") || msg.contains("feedback")) {
            return "To protect marketplace integrity, client reviews and 1–5 star ratings can only be submitted for verified orders once their delivery has been confirmed.";
        }

        if (msg.contains("cart") || msg.contains("bag") || msg.contains("quantity")) {
            return "You can manage items, update quantities, and inspect running authoritative totals at any time from your Shopping Bag.";
        }

        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "Hello! I am your DJ Mart AI Assistant. Feel free to ask about our curated collections, shipping, payment options, or order tracking.";
        }

        return "Thank you for inquiring with DJ Mart Concierge. For specific assistance regarding our curated catalog, delivery status, or seller onboarding, feel free to ask or contact support@DJ Mart.com.";
    }
}
