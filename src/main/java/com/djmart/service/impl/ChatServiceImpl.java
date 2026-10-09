package com.djmart.service.impl;

import com.djmart.dao.jdbc.OrderDAOImpl;
import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.exception.ValidationException;
import com.djmart.service.ChatService;
import com.djmart.service.ai.*;
import com.djmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation managing customer assistance queries, session guardrails,
 * rate limiting (20 msg/min), input caps, database-connected AI responses,
 * and database-backed conversation history.
 */
public class ChatServiceImpl implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final int MAX_INPUT_LENGTH = 1000;
    private static final int MAX_MESSAGES_PER_MINUTE = 20;
    private static final String DOMAIN_CONTEXT =
            "DJ Mart is an authentic e-commerce marketplace in India offering curated electronics, fashion, home & kitchen, books & stationery, and wellness gear. " +
            "All pricing is in Indian Rupees (INR / ₹). Complimentary express shipping on all orders in 2-4 business days. 14-day return policy. " +
            "Accepted payments: RuPay, Visa, MasterCard, Instant UPI (GPay, PhonePe, Paytm), and Cash on Delivery (COD).";

    private final ChatProvider chatProvider;
    private final DatabaseAwareChatEngine dbEngine;
    private final Map<String, RateTracker> rateLimits = new ConcurrentHashMap<>();

    public ChatServiceImpl() {
        ChatTools chatTools = new ChatTools(new ProductDAOImpl(), new OrderDAOImpl());
        this.dbEngine = new DatabaseAwareChatEngine(chatTools);

        String openAiKey = System.getProperty("openai.api.key", System.getenv("OPENAI_API_KEY"));
        if (openAiKey == null || openAiKey.isBlank()) {
            openAiKey = System.getenv("AI_API_KEY");
        }

        String geminiApiKey = System.getProperty("gemini.api.key", System.getenv("GEMINI_API_KEY"));

        if (openAiKey != null && !openAiKey.isBlank() && !"mock".equalsIgnoreCase(openAiKey)) {
            String baseUrl = System.getenv("OPENAI_BASE_URL");
            String model = System.getenv("OPENAI_MODEL");
            this.chatProvider = new OpenAIChatProvider(openAiKey, baseUrl, model, dbEngine);
            LOGGER.info("ChatService initialized with OpenAIChatProvider (model: {})", model != null ? model : "gpt-4o-mini");
        } else if (geminiApiKey != null && !geminiApiKey.isBlank() && !"mock".equalsIgnoreCase(geminiApiKey)) {
            this.chatProvider = new GeminiChatProvider(geminiApiKey, dbEngine);
            LOGGER.info("ChatService initialized with GeminiChatProvider");
        } else {
            this.chatProvider = dbEngine;
            LOGGER.info("ChatService initialized with DatabaseAwareChatEngine (live database tool mode)");
        }
    }

    public ChatServiceImpl(ChatProvider chatProvider) {
        this.chatProvider = chatProvider;
        this.dbEngine = (chatProvider instanceof DatabaseAwareChatEngine) ?
                (DatabaseAwareChatEngine) chatProvider :
                new DatabaseAwareChatEngine(new ChatTools(new ProductDAOImpl(), new OrderDAOImpl()));
    }

    @Override
    public String processMessage(String sessionId, String userMessage) {
        return processMessage(sessionId, userMessage, null, null);
    }

    @Override
    public String processMessage(String sessionId, String userMessage, Long userId, String userRole) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new ValidationException("Message cannot be empty");
        }

        String trimmed = userMessage.trim();
        if (trimmed.length() > MAX_INPUT_LENGTH) {
            throw new ValidationException("Message exceeds the maximum limit of " + MAX_INPUT_LENGTH + " characters");
        }

        String safeSessionId = sessionId != null ? sessionId : "anonymous";

        // 1. Enforce per-session rate limit
        enforceRateLimit(safeSessionId);

        // 2. Persist incoming user message in database
        persistChatMessage(safeSessionId, userId, "USER", trimmed);

        // 3. Obtain response from provider
        String reply;
        if (chatProvider == dbEngine) {
            reply = dbEngine.getReply(safeSessionId, trimmed, userId, DOMAIN_CONTEXT);
        } else {
            try {
                reply = chatProvider.getReply(trimmed, DOMAIN_CONTEXT);
                if (reply == null || reply.isBlank()) {
                    reply = dbEngine.getReply(safeSessionId, trimmed, userId, DOMAIN_CONTEXT);
                }
            } catch (Exception ex) {
                LOGGER.warn("External chat provider failed, falling back to database engine: {}", ex.getMessage());
                reply = dbEngine.getReply(safeSessionId, trimmed, userId, DOMAIN_CONTEXT);
            }
        }

        // 4. Persist bot reply in database
        persistChatMessage(safeSessionId, userId, "BOT", reply);

        return reply;
    }

    private void enforceRateLimit(String sessionId) {
        long now = System.currentTimeMillis();
        RateTracker tracker = rateLimits.computeIfAbsent(sessionId, k -> new RateTracker());

        synchronized (tracker) {
            if (now - tracker.windowStart > 60000) {
                tracker.windowStart = now;
                tracker.count = 1;
            } else {
                tracker.count++;
                if (tracker.count > MAX_MESSAGES_PER_MINUTE) {
                    LOGGER.warn("Rate limit exceeded for chat session {}", sessionId);
                    throw new ValidationException("Message rate limit reached (maximum " + MAX_MESSAGES_PER_MINUTE + " messages per minute). Please pause briefly.");
                }
            }
        }
    }

    private void persistChatMessage(String sessionId, Long userId, String senderType, String message) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Upsert conversation
            try (PreparedStatement ps = conn.prepareStatement(
                    "MERGE INTO chat_conversations (id, user_id, updated_at) KEY (id) VALUES (?, ?, CURRENT_TIMESTAMP)")) {
                ps.setString(1, sessionId);
                if (userId != null) {
                    ps.setLong(2, userId);
                } else {
                    ps.setNull(2, java.sql.Types.BIGINT);
                }
                ps.executeUpdate();
            }
            // Insert message
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO chat_messages (conversation_id, sender_type, message, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)")) {
                ps.setString(1, sessionId);
                ps.setString(2, senderType);
                ps.setString(3, message);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.debug("Could not persist chat message to database: {}", e.getMessage());
        }
    }

    private static class RateTracker {
        private long windowStart = System.currentTimeMillis();
        private int count = 0;
    }
}
