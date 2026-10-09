package com.djmart.service.ai;

/**
 * Interface defining the pluggable AI assistant integration contract.
 * Allows swapping between canned mock FAQ provider and live LLM providers.
 */
public interface ChatProvider {

    /**
     * Obtains an AI-generated or domain-rule response for a customer inquiry.
     *
     * @param userMessage user's input query
     * @param context domain context (catalog, policies, marketplace guarantees)
     * @return response text
     */
    String getReply(String userMessage, String context);
}
