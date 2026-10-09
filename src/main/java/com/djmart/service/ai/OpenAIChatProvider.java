package com.djmart.service.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ChatProvider connecting to OpenAI Chat Completions API (or OpenAI-compatible endpoints like Groq/Ollama).
 * Uses server-side API key and falls back securely to DatabaseAwareChatEngine.
 */
public class OpenAIChatProvider implements ChatProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIChatProvider.class);

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final ChatProvider fallbackProvider;

    public OpenAIChatProvider(String apiKey, ChatProvider fallbackProvider) {
        this(apiKey, "https://api.openai.com/v1", "gpt-4o-mini", fallbackProvider);
    }

    public OpenAIChatProvider(String apiKey, String baseUrl, String model, ChatProvider fallbackProvider) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl.trim() : "https://api.openai.com/v1";
        this.model = model != null && !model.isBlank() ? model.trim() : "gpt-4o-mini";
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String getReply(String userMessage, String context) {
        if (apiKey == null || apiKey.trim().isEmpty() || "mock".equalsIgnoreCase(apiKey)) {
            return fallbackProvider.getReply(userMessage, context);
        }

        try {
            String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            conn.setDoOutput(true);

            String systemPrompt = "You are the AI Concierge for DJ Mart, an authentic e-commerce marketplace in India. " +
                    "All prices are in Indian Rupees (INR / ₹). Never invent prices or stock. " +
                    "Context: " + context + ". Answer concisely and helpfully.";

            JsonObject root = new JsonObject();
            root.addProperty("model", model);

            JsonArray messages = new JsonArray();
            JsonObject sysMsg = new JsonObject();
            sysMsg.addProperty("role", "system");
            sysMsg.addProperty("content", systemPrompt);
            messages.add(sysMsg);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            root.add("messages", messages);
            root.addProperty("temperature", 0.7);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(root.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                JsonObject jsonResp = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (jsonResp.has("choices") && jsonResp.getAsJsonArray("choices").size() > 0) {
                    JsonObject choice = jsonResp.getAsJsonArray("choices").get(0).getAsJsonObject();
                    if (choice.has("message") && choice.getAsJsonObject("message").has("content")) {
                        return choice.getAsJsonObject("message").get("content").getAsString().trim();
                    }
                }
                return fallbackProvider.getReply(userMessage, context);
            } else {
                LOGGER.warn("OpenAI API returned status {}. Using database-aware fallback.", responseCode);
                return fallbackProvider.getReply(userMessage, context);
            }
        } catch (Exception e) {
            LOGGER.warn("OpenAI API call failed: {}. Utilizing database-aware fallback.", e.getMessage());
            return fallbackProvider.getReply(userMessage, context);
        }
    }
}
