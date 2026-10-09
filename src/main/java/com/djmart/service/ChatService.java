package com.djmart.service;

/**
 * Service orchestrating the AI chatbot assistant, rate limiting, and response caching.
 */
public interface ChatService {

    /**
     * Processes a user question, applying rate limits and AI context.
     *
     * @param sessionId active user session ID
     * @param userMessage user input message
     * @return AI reply message
     */
    default String processMessage(String sessionId, String userMessage) {
        return processMessage(sessionId, userMessage, null, null);
    }

    /**
     * Processes a user question with authenticated user context.
     *
     * @param sessionId active user session ID
     * @param userMessage user input message
     * @param userId optional authenticated user ID
     * @param userRole optional authenticated user role
     * @return AI reply message
     */
    String processMessage(String sessionId, String userMessage, Long userId, String userRole);
}
