package com.djmart.service;

import com.djmart.config.DatabaseConfig;
import com.djmart.dao.CartDAO;
import com.djmart.dao.OrderDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dto.CheckoutRequest;
import com.djmart.dto.OrderItemResponse;
import com.djmart.dto.OrderResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.InsufficientStockException;
import com.djmart.exception.OrderException;
import com.djmart.exception.ValidationException;
import com.djmart.model.*;
import com.djmart.service.impl.OrderServiceImpl;
import com.djmart.util.DatabaseUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private OrderItemDAO orderItemDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CartDAO cartDAO;

    private OrderService orderService;

    @BeforeAll
    static void initDb() {
        DatabaseConfig testConfig = new DatabaseConfig("test-config.properties");
        DatabaseUtil.initDataSource(testConfig);
    }

    @AfterAll
    static void cleanDb() {
        DatabaseUtil.closeDataSource();
    }

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderDAO, orderItemDAO, productDAO, cartDAO);
    }

    @Test
    @DisplayName("Checkout: Empty cart throws ValidationException")
    void testCheckoutEmptyCart() {
        Long buyerId = 4L;
        when(cartDAO.findByUser(buyerId)).thenReturn(Collections.emptyList());

        CheckoutRequest request = new CheckoutRequest("123 Tech Lane, NY", true);
        assertThrows(ValidationException.class, () -> orderService.checkout(buyerId, request));
    }

    @Test
    @DisplayName("Checkout: Insufficient stock throws InsufficientStockException before transaction commit")
    void testCheckoutInsufficientStock() {
        Long buyerId = 4L;
        Long productId = 1L;

        CartItem item = new CartItem(1L, buyerId, productId, 10, null);
        when(cartDAO.findByUser(buyerId)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(productId);
        product.setStockQty(2); // Only 2 in stock, 10 requested
        product.setPrice(new BigDecimal("100.00"));
        when(productDAO.findById(productId)).thenReturn(Optional.of(product));

        CheckoutRequest request = new CheckoutRequest("123 Tech Lane, NY", true);
        assertThrows(InsufficientStockException.class, () -> orderService.checkout(buyerId, request));
        verify(orderDAO, never()).create(any(), any());
    }

    @Test
    @DisplayName("Checkout: Atomic transaction succeeds and clears cart")
    void testCheckoutSuccess() throws Exception {
        Long buyerId = 4L;
        Long productId = 1L;

        CartItem item = new CartItem(1L, buyerId, productId, 2, null);
        when(cartDAO.findByUser(buyerId)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(productId);
        product.setName("Wireless Headphones");
        product.setStockQty(20);
        product.setPrice(new BigDecimal("150.00"));
        when(productDAO.findById(productId)).thenReturn(Optional.of(product));

        when(productDAO.decrementStock(any(Connection.class), eq(productId), eq(2))).thenReturn(true);

        Order createdOrder = new Order();
        createdOrder.setId(101L);
        createdOrder.setBuyerId(buyerId);
        createdOrder.setStatus(OrderStatus.CONFIRMED);
        createdOrder.setTotalAmount(new BigDecimal("300.00"));
        when(orderDAO.create(any(Connection.class), any(Order.class))).thenReturn(createdOrder);

        CheckoutRequest request = new CheckoutRequest("123 Tech Lane, NY", true);
        OrderResponse response = orderService.checkout(buyerId, request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());

        verify(orderDAO).create(any(Connection.class), any(Order.class));
        verify(orderItemDAO).createBatch(any(Connection.class), anyList());
        verify(cartDAO).clear(any(Connection.class), eq(buyerId));
    }

    @Test
    @DisplayName("Cancel Order: Succeeds for CONFIRMED status and restores inventory")
    void testCancelOrderSuccess() throws Exception {
        Long orderId = 50L;
        Long buyerId = 4L;

        Order order = new Order();
        order.setId(orderId);
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderDAO.findById(orderId)).thenReturn(Optional.of(order));

        OrderItemResponse itemResp = new OrderItemResponse(1L, 10L, "Shirt", null, 3, new BigDecimal("40.00"));
        when(orderItemDAO.findByOrderId(orderId)).thenReturn(List.of(itemResp));

        Product product = new Product();
        product.setId(10L);
        product.setStockQty(15);
        when(productDAO.findById(10L)).thenReturn(Optional.of(product));

        boolean cancelled = orderService.cancelOrder(orderId, buyerId, Role.BUYER);
        assertTrue(cancelled);

        verify(orderDAO).updateStatus(any(Connection.class), eq(orderId), eq(OrderStatus.CANCELLED));
        // Stock restored from 15 + 3 = 18
        verify(productDAO).updateStock(10L, 18);
    }

    @Test
    @DisplayName("Cancel Order: Fails if already SHIPPED or DELIVERED")
    void testCancelShippedOrderThrows() {
        Long orderId = 51L;
        Long buyerId = 4L;

        Order order = new Order();
        order.setId(orderId);
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.SHIPPED);

        when(orderDAO.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderException.class, () -> orderService.cancelOrder(orderId, buyerId, Role.BUYER));
    }

    @Test
    @DisplayName("Cancel Order: Unauthorized buyer cannot cancel someone else's order")
    void testCancelUnauthorized() {
        Order order = new Order();
        order.setId(1L);
        order.setBuyerId(4L);
        order.setStatus(OrderStatus.PENDING);

        when(orderDAO.findById(1L)).thenReturn(Optional.of(order));

        // Stranger buyer (id = 99)
        assertThrows(AuthorizationException.class, () -> orderService.cancelOrder(1L, 99L, Role.BUYER));
    }
}
