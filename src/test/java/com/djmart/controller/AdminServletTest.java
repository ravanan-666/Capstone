package com.djmart.controller;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dao.UserDAO;
import com.djmart.dto.OrderResponse;
import com.djmart.dto.PageResponse;
import com.djmart.dto.UserResponse;
import com.djmart.model.Role;
import com.djmart.service.OrderService;
import com.djmart.service.ProductService;
import com.djmart.service.UserService;
import com.djmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminServletTest {

    private UserService userService;
    private OrderService orderService;
    private ProductService productService;
    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private AdminServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        userService = mock(UserService.class);
        orderService = mock(OrderService.class);
        productService = mock(ProductService.class);
        userDAO = mock(UserDAO.class);
        orderDAO = mock(OrderDAO.class);
        productDAO = mock(ProductDAO.class);

        servlet = new AdminServlet(userService, orderService, productService, userDAO, orderDAO, productDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private UserResponse createAdminUser(Long id) {
        return new UserResponse(id, "Admin User", "admin@djmart.com", Role.ADMIN, Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("GET /admin/dashboard: Authenticated admin views dashboard JSP with platform KPIs")
    void testAdminDashboard_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createAdminUser(1L));

        when(userDAO.count()).thenReturn(150L);
        when(userDAO.countByRole(Role.BUYER)).thenReturn(130L);
        when(userDAO.countByRole(Role.SELLER)).thenReturn(19L);
        when(productDAO.countAll()).thenReturn(85L);
        when(orderDAO.countAll()).thenReturn(320L);
        when(orderDAO.calculateTotalRevenue()).thenReturn(new BigDecimal("1250000.00"));

        PageResponse<UserResponse> users = new PageResponse<>(Collections.emptyList(), 1, 15, 150);
        when(userService.findAllUsers(1, 15)).thenReturn(users);

        PageResponse<OrderResponse> orders = new PageResponse<>(Collections.emptyList(), 1, 10, 320);
        when(orderService.getAllOrders(1, 10)).thenReturn(orders);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("stats"), anyMap());
        verify(request).setAttribute(eq("usersPage"), eq(users));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /api/admin/stats: Returns platform KPIs as JSON")
    void testGetPlatformStats_Json() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/admin/stats");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createAdminUser(1L));

        when(userDAO.count()).thenReturn(50L);
        when(orderDAO.calculateTotalRevenue()).thenReturn(new BigDecimal("99000.00"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
        assertTrue(responseWriter.toString().contains("totalUsers"));
    }
}
