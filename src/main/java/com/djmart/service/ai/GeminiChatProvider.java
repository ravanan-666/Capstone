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
 * ChatProvider connecting securely to Google Gemini REST API.
 * Uses Gson for robust JSON serialization and parsing.
 * Gracefully falls back to DatabaseAwareChatEngine if the API key is not configured or network fails.
 */
public class GeminiChatProvider implements ChatProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiChatProvider.class);

    private final String apiKey;
    private final ChatProvider fallbackProvider;

    public GeminiChatProvider(String apiKey, ChatProvider fallbackProvider) {
        this.apiKey = apiKey;
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String getReply(String userMessage, String context) {
        if (apiKey == null || apiKey.trim().isEmpty() || "mock".equalsIgnoreCase(apiKey)) {
            return fallbackProvider.getReply(userMessage, context);
        }

        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            conn.setDoOutput(true);

            String systemPrompt = "You are the AI Concierge for DJ Mart, an authentic e-commerce marketplace in India. " +
                    "All prices are in Indian Rupees (INR / ₹). Never invent prices, stock numbers, or order IDs. " +
                    "Use the verified marketplace catalog and policies provided in context: " + context + ".";

            JsonObject root = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();
            JsonArray parts = new JsonArray();

            JsonObject sysPart = new JsonObject();
            sysPart.addProperty("text", systemPrompt + "\n\nCustomer question: " + userMessage);
            parts.add(sysPart);

            contentObj.add("parts", parts);
            contents.add(contentObj);
            root.add("contents", contents);

            String jsonPayload = root.toString();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
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
                if (jsonResp.has("candidates") && jsonResp.getAsJsonArray("candidates").size() > 0) {
                    JsonObject firstCandidate = jsonResp.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (firstCandidate.has("content")) {
                        JsonObject candidateContent = firstCandidate.getAsJsonObject("content");
                        if (candidateContent.has("parts") && candidateContent.getAsJsonArray("parts").size() > 0) {
                            String text = candidateContent.getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
                            return text.trim();
                        }
                    }
                }
                return fallbackProvider.getReply(userMessage, context);
            } else {
                LOGGER.warn("Gemini API call responded with status {}. Using database-aware fallback.", responseCode);
                return fallbackProvider.getReply(userMessage, context);
            }
        } catch (Exception e) {
            LOGGER.warn("Gemini API call encountered error: {}. Utilizing database-aware fallback.", e.getMessage());
            return fallbackProvider.getReply(userMessage, context);
        }
    }
}
