package com.djmart.controller;

import com.djmart.dto.UserResponse;
import com.djmart.service.ChatService;
import com.djmart.service.impl.ChatServiceImpl;
import com.djmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Controller endpoint handling customer assistance queries via AI / live database tools.
 * Maps to /api/chat and /api/v1/chat.
 */
@WebServlet(name = "ChatServlet", urlPatterns = {"/api/chat", "/api/v1/chat"})
public class ChatServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServlet.class);

    private final ChatService chatService;

    public ChatServlet() {
        this(new ChatServiceImpl());
    }

    public ChatServlet(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(true);
            List<Map<String, Object>> messages = fetchRecentChatMessages(session.getId());
            sendSuccess(response, HttpServletResponse.SC_OK, messages);
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String message = null;

            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                ChatMessageRequest req = parseRequestBody(request, ChatMessageRequest.class);
                if (req != null) {
                    message = req.message;
                }
            } else {
                message = getStringParam(request, "message");
            }

            Long userId = null;
            String userRole = null;
            try {
                UserResponse user = getAuthenticatedUser(request);
                if (user != null) {
                    userId = user.getId();
                    userRole = user.getRole() != null ? user.getRole().name() : null;
                }
            } catch (Exception ignored) {}

            HttpSession session = request.getSession(true);
            String reply = chatService.processMessage(session.getId(), message, userId, userRole);

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);

            sendSuccess(response, HttpServletResponse.SC_OK, data);
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private List<Map<String, Object>> fetchRecentChatMessages(String sessionId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sender_type, message, created_at FROM chat_messages WHERE conversation_id = ? ORDER BY id ASC LIMIT 50";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sender", rs.getString("sender_type").toLowerCase());
                    map.put("text", rs.getString("message"));
                    map.put("time", rs.getTimestamp("created_at"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not fetch chat history: {}", e.getMessage());
        }
        return list;
    }

    private static class ChatMessageRequest {
        private String message;
    }
}
