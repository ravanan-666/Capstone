package com.djmart.service;

import com.djmart.dao.CartDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dto.CartItemResponse;
import com.djmart.dto.CartResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.InsufficientStockException;
import com.djmart.exception.ProductNotFoundException;
import com.djmart.model.CartItem;
import com.djmart.model.Product;
import com.djmart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartDAO, productDAO);
    }

    @Test
    @DisplayName("Get Cart: Server calculates running totals and subtotals accurately")
    void testGetCartServerCalculation() {
        Long userId = 5L;

        Product p1 = new Product();
        p1.setId(10L);
        p1.setName("Item 1");
        p1.setPrice(new BigDecimal("25.00"));

        Product p2 = new Product();
        p2.setId(20L);
        p2.setName("Item 2");
        p2.setPrice(new BigDecimal("10.50"));

        CartItem item1 = new CartItem(1L, userId, 10L, 2, null);
        item1.setProduct(p1);

        CartItem item2 = new CartItem(2L, userId, 20L, 3, null);
        item2.setProduct(p2);

        when(cartDAO.findByUser(userId)).thenReturn(List.of(item1, item2));

        CartResponse cart = cartService.getCart(userId);

        assertNotNull(cart);
        assertEquals(5, cart.getTotalItems());
        // (25.00 * 2) + (10.50 * 3) = 50.00 + 31.50 = 81.50
        assertEquals(new BigDecimal("81.50"), cart.getGrandTotal());
    }

    @Test
    @DisplayName("Add to Cart: Exceeding stock throws InsufficientStockException")
    void testAddToCartExceedingStock() {
        Long userId = 4L;
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setStockQty(5); // Only 5 available

        when(productDAO.findById(productId)).thenReturn(Optional.of(product));
        when(cartDAO.findByUserAndProduct(userId, productId)).thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(userId, productId, 10));
        verify(cartDAO, never()).addOrUpdateItem(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Add to Cart: Non-existent product throws ProductNotFoundException")
    void testAddToCartProductNotFound() {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> cartService.addToCart(1L, 999L, 1));
    }

    @Test
    @DisplayName("Update Quantity: Unauthorized user cannot modify someone else's cart item")
    void testUpdateQuantityUnauthorized() {
        Long ownerId = 4L;
        Long hackerId = 99L;
        Long cartItemId = 1L;

        CartItem cartItem = new CartItem(cartItemId, ownerId, 2L, 1, null);
        when(cartDAO.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        assertThrows(AuthorizationException.class, () -> cartService.updateQuantity(hackerId, cartItemId, 5));
        verify(cartDAO, never()).updateQuantity(anyLong(), anyInt());
    }
}
