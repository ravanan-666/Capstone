package com.djmart.controller;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dto.*;
import com.djmart.model.Role;
import com.djmart.service.OrderService;
import com.djmart.service.ProductService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SellerServletTest {

    private ProductService productService;
    private OrderService orderService;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private SellerServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        productService = mock(ProductService.class);
        orderService = mock(OrderService.class);
        productDAO = mock(ProductDAO.class);
        orderDAO = mock(OrderDAO.class);
        orderItemDAO = mock(OrderItemDAO.class);

        servlet = new SellerServlet(productService, orderService, productDAO, orderDAO, orderItemDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private UserResponse createSellerUser(Long id) {
        return new UserResponse(id, "Seller User", "seller@djmart.com", Role.SELLER, Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("GET /seller/dashboard: Authenticated seller views dashboard JSP with aggregated metrics")
    void testSellerDashboard_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/seller/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createSellerUser(2L));

        when(productDAO.countBySeller(2L)).thenReturn(8L);
        when(orderDAO.countRelevantSellerOrders(2L)).thenReturn(12L);
        when(productDAO.countLowStockBySeller(2L, 5)).thenReturn(2L);
        when(orderItemDAO.calculateSellerRevenue(2L)).thenReturn(new BigDecimal("45000.00"));

        PageResponse<ProductResponse> products = new PageResponse<>(Collections.emptyList(), 1, 10, 8);
        when(productService.getProductsBySeller(2L, 1, 10)).thenReturn(products);

        PageResponse<OrderResponse> orders = new PageResponse<>(Collections.emptyList(), 1, 5, 12);
        when(orderService.getOrdersBySeller(2L, 1, 5)).thenReturn(orders);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("stats"), anyMap());
        verify(request).setAttribute(eq("products"), eq(products));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /seller/products: Seller creates product listing and redirects with created=true")
    void testCreateProduct_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/seller/products");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createSellerUser(2L));

        when(request.getParameter("name")).thenReturn("Linen Overcoat");
        when(request.getParameter("description")).thenReturn("Artisan hand-woven linen garment");
        when(request.getParameter("price")).thenReturn("7500.00");
        when(request.getParameter("stockQty")).thenReturn("15");
        when(request.getParameter("category")).thenReturn("Apparel");

        ProductResponse created = new ProductResponse(100L, 2L, "Seller User", "Linen Overcoat", "desc", new BigDecimal("7500.00"), 15, "Apparel", null, null, 0, Timestamp.from(Instant.now()));
        when(productService.createProduct(eq(2L), any(ProductRequest.class))).thenReturn(created);

        servlet.doPost(request, response);

        verify(productService).createProduct(eq(2L), any(ProductRequest.class));
        verify(response).sendRedirect("/seller/dashboard?created=true");
    }

    @Test
    @DisplayName("POST /seller/products/update: Seller updates product listing")
    void testUpdateProduct_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/seller/products/update");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createSellerUser(2L));

        when(request.getParameter("productId")).thenReturn("100");
        when(request.getParameter("name")).thenReturn("Linen Overcoat Updated");
        when(request.getParameter("description")).thenReturn("Updated description");
        when(request.getParameter("price")).thenReturn("7200.00");
        when(request.getParameter("stockQty")).thenReturn("20");
        when(request.getParameter("category")).thenReturn("Apparel");

        servlet.doPost(request, response);

        verify(productService).updateProduct(eq(2L), eq(100L), any(ProductRequest.class), eq(false));
        verify(response).sendRedirect("/seller/dashboard?updated=true");
    }

    @Test
    @DisplayName("POST /seller/products/delete: Seller deletes product listing")
    void testDeleteProduct_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/seller/products/delete");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createSellerUser(2L));
        when(request.getParameter("productId")).thenReturn("100");

        servlet.doPost(request, response);

        verify(productService).deleteProduct(2L, 100L, false);
        verify(response).sendRedirect("/seller/dashboard?deleted=true");
    }
}
