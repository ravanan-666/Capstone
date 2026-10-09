package com.djmart.controller;

import com.djmart.dto.*;
import com.djmart.model.OrderStatus;
import com.djmart.model.Role;
import com.djmart.service.CartService;
import com.djmart.service.OrderService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServletTest {

    private OrderService orderService;
    private CartService cartService;
    private OrderServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        orderService = mock(OrderService.class);
        cartService = mock(CartService.class);
        servlet = new OrderServlet(orderService, cartService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private UserResponse createUser(Long id, Role role) {
        return new UserResponse(id, "Test User", "user" + id + "@djmart.com", role, Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("GET /checkout: If cart is empty, redirects to /cart?empty=true")
    void testCheckoutView_EmptyCart_Redirects() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/checkout");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.BUYER));
        when(cartService.getCart(1L)).thenReturn(new CartResponse());

        servlet.doGet(request, response);

        verify(response).sendRedirect("/cart?empty=true");
    }

    @Test
    @DisplayName("GET /checkout: If cart has merchandise, renders buyer/checkout.jsp")
    void testCheckoutView_WithItems_RendersJsp() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/checkout");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.BUYER));

        CartItemResponse item = new CartItemResponse(10L, 100L, "Sample Watch", "img.jpg", new BigDecimal("1500.00"), 1);
        CartResponse cart = new CartResponse(List.of(item));
        when(cartService.getCart(1L)).thenReturn(cart);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("cart"), any(CartResponse.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /checkout: Placing order redirects to order details with success parameter")
    void testCheckoutSubmit_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/checkout");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.BUYER));
        when(request.getParameter("shippingAddress")).thenReturn("123 Heritage Boulevard, Suite 4B");
        when(request.getParameter("paymentConfirmed")).thenReturn("true");

        OrderResponse placedOrder = new OrderResponse();
        placedOrder.setId(501L);
        placedOrder.setStatus(OrderStatus.CONFIRMED);
        placedOrder.setTotalAmount(new BigDecimal("2999.00"));

        when(orderService.checkout(eq(1L), any(CheckoutRequest.class))).thenReturn(placedOrder);

        servlet.doPost(request, response);

        verify(orderService).checkout(eq(1L), any(CheckoutRequest.class));
        verify(response).sendRedirect("/orders/501?success=true");
    }

    @Test
    @DisplayName("GET /orders: Lists buyer's past orders and forwards to buyer/orders.jsp")
    void testOrderHistory_Buyer() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/orders");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.BUYER));

        PageResponse<OrderResponse> pageResponse = new PageResponse<>(Collections.emptyList(), 1, 10, 0);
        when(orderService.getOrdersByBuyer(1L, 1, 10)).thenReturn(pageResponse);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("ordersPage"), any(PageResponse.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /orders/{id}: Renders order details view")
    void testOrderDetails() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/orders/501");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.BUYER));

        OrderResponse order = new OrderResponse();
        order.setId(501L);
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderService.getOrderById(501L, 1L, Role.BUYER)).thenReturn(order);

        servlet.doGet(request, response);

        verify(request).setAttribute("order", order);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel: Cancels order and restores stock")
    void testCancelOrder_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/orders/501/cancel");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.BUYER));

        when(orderService.cancelOrder(501L, 1L, Role.BUYER)).thenReturn(true);

        servlet.doPost(request, response);

        verify(orderService).cancelOrder(501L, 1L, Role.BUYER);
        verify(response).sendRedirect("/orders/501?cancelled=true");
    }

    @Test
    @DisplayName("POST /api/orders/{id}/status: Seller advances order status")
    void testUpdateStatus_Seller() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders/501/status");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(2L, Role.SELLER));
        when(request.getParameter("status")).thenReturn("SHIPPED");

        OrderResponse updated = new OrderResponse();
        updated.setId(501L);
        updated.setStatus(OrderStatus.SHIPPED);
        when(orderService.updateOrderStatus(501L, OrderStatus.SHIPPED, 2L, Role.SELLER)).thenReturn(updated);

        servlet.doPost(request, response);

        verify(orderService).updateOrderStatus(501L, OrderStatus.SHIPPED, 2L, Role.SELLER);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Order status updated successfully"));
    }
}
