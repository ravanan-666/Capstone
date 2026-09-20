package com.djmart.controller;

import com.djmart.dto.CartItemResponse;
import com.djmart.dto.CartResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.InsufficientStockException;
import com.djmart.model.Role;
import com.djmart.service.CartService;
import com.djmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartServletTest {

    private CartService cartService;
    private CartServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        cartService = mock(CartService.class);
        servlet = new CartServlet(cartService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private UserResponse createBuyerUser(Long id) {
        return new UserResponse(id, "Buyer User", "buyer@djmart.com", Role.BUYER, Timestamp.from(Instant.now()));
    }

    private CartResponse createSampleCart(Long cartItemId, Long productId, int quantity, BigDecimal price) {
        CartItemResponse item = new CartItemResponse(cartItemId, productId, "Sample Product", "img.jpg", price, quantity);
        List<CartItemResponse> list = new ArrayList<>();
        list.add(item);
        return new CartResponse(list);
    }

    @Test
    @DisplayName("GET /api/cart: Authenticated buyer retrieves authoritative cart JSON")
    void testGetCart_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(session);

        UserResponse buyer = createBuyerUser(1L);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyer);

        CartResponse cart = createSampleCart(10L, 1L, 2, new BigDecimal("1500.00"));
        when(cartService.getCart(1L)).thenReturn(cart);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"grandTotal\":3000.00"));
        assertTrue(json.contains("\"totalItems\":2"));
    }

    @Test
    @DisplayName("GET /api/cart: Unauthenticated request rejected with 401")
    void testGetCart_Unauthenticated() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("POST /api/cart/add: Adds item and returns authoritative updated cart")
    void testAddToCart_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart/add");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getSession(false)).thenReturn(session);

        UserResponse buyer = createBuyerUser(1L);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyer);

        String body = "{\"productId\":1,\"quantity\":2}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        CartItemResponse addedItem = new CartItemResponse(10L, 1L, "Sample Product", "img.jpg", new BigDecimal("1500.00"), 2);
        when(cartService.addToCart(1L, 1L, 2)).thenReturn(addedItem);

        CartResponse updatedCart = createSampleCart(10L, 1L, 2, new BigDecimal("1500.00"));
        when(cartService.getCart(1L)).thenReturn(updatedCart);

        servlet.doPost(request, response);

        verify(cartService).addToCart(1L, 1L, 2);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"totalItems\":2"));
    }

    @Test
    @DisplayName("POST /api/cart/add: Stock exceeded returns 409 INSUFFICIENT_STOCK")
    void testAddToCart_StockExceeded() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart/add");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getSession(false)).thenReturn(session);

        UserResponse buyer = createBuyerUser(1L);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyer);

        String body = "{\"productId\":1,\"quantity\":99}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        when(cartService.addToCart(1L, 1L, 99))
                .thenThrow(new InsufficientStockException("Requested quantity 99 exceeds available stock of 5"));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("POST /api/cart/update: Updates quantity and returns authoritative totals")
    void testUpdateQuantity_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart/update");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getSession(false)).thenReturn(session);

        UserResponse buyer = createBuyerUser(1L);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyer);

        String body = "{\"cartItemId\":10,\"quantity\":3}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        CartItemResponse updatedItem = new CartItemResponse(10L, 1L, "Sample Product", "img.jpg", new BigDecimal("1500.00"), 3);
        when(cartService.updateQuantity(1L, 10L, 3)).thenReturn(updatedItem);

        CartResponse updatedCart = createSampleCart(10L, 1L, 3, new BigDecimal("1500.00"));
        when(cartService.getCart(1L)).thenReturn(updatedCart);

        servlet.doPost(request, response);

        verify(cartService).updateQuantity(1L, 10L, 3);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"grandTotal\":4500.00"));
    }

    @Test
    @DisplayName("POST /api/cart/remove: Removes item and returns updated totals")
    void testRemoveItem_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart/remove");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);
        when(request.getParameter("cartItemId")).thenReturn("10");

        UserResponse buyer = createBuyerUser(1L);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyer);

        when(cartService.removeFromCart(1L, 10L)).thenReturn(true);
        when(cartService.getCart(1L)).thenReturn(new CartResponse());

        servlet.doPost(request, response);

        verify(cartService).removeFromCart(1L, 10L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"totalItems\":0"));
    }

    @Test
    @DisplayName("POST /api/cart/clear: Clears cart and returns empty bag")
    void testClearCart_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/cart/clear");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);

        UserResponse buyer = createBuyerUser(1L);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyer);

        when(cartService.clearCart(1L)).thenReturn(true);

        servlet.doPost(request, response);

        verify(cartService).clearCart(1L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"totalItems\":0"));
    }
}
