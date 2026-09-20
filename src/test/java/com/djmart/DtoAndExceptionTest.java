package com.djmart;

import com.djmart.dto.*;
import com.djmart.exception.*;
import com.djmart.model.Role;
import com.djmart.model.User;
import com.djmart.util.JsonUtil;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndExceptionTest {

    @Test
    @DisplayName("Verify ApiResponse success envelope format matches requirements")
    void testApiResponseSuccessEnvelope() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Mechanical Keyboard");
        product.setPrice(new BigDecimal("89.50"));

        ApiResponse<ProductResponse> response = ApiResponse.success("Product added successfully", product);

        assertTrue(response.isSuccess());
        assertEquals("Product added successfully", response.getMessage());
        assertNotNull(response.getData());
        assertNull(response.getErrorCode());

        String json = JsonUtil.toJson(response);
        JsonObject jsonObject = JsonUtil.fromJson(json, JsonObject.class);

        assertTrue(jsonObject.get("success").getAsBoolean());
        assertEquals("Product added successfully", jsonObject.get("message").getAsString());
        assertTrue(jsonObject.has("data"));
        assertEquals("Mechanical Keyboard", jsonObject.getAsJsonObject("data").get("name").getAsString());
    }

    @Test
    @DisplayName("Verify ApiResponse error envelope format matches requirements")
    void testApiResponseErrorEnvelope() {
        ApiResponse<Void> response = ApiResponse.error("Product not found", "PRODUCT_NOT_FOUND");

        assertFalse(response.isSuccess());
        assertEquals("Product not found", response.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", response.getErrorCode());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals("PRODUCT_NOT_FOUND", response.getError().getCode());

        String json = JsonUtil.toJson(response);
        JsonObject jsonObject = JsonUtil.fromJson(json, JsonObject.class);

        assertFalse(jsonObject.get("success").getAsBoolean());
        assertEquals("Product not found", jsonObject.get("message").getAsString());
        assertEquals("PRODUCT_NOT_FOUND", jsonObject.get("errorCode").getAsString());
    }

    @Test
    @DisplayName("SECURITY: Verify UserResponse never exposes password or passwordHash")
    void testUserResponseSecurity() {
        User user = new User(
                1L,
                "Admin User",
                "admin@djmart.com",
                "$2a$10$superSecretHashedPassword12345",
                Role.ADMIN,
                Timestamp.from(Instant.now())
        );

        UserResponse dto = UserResponse.fromUser(user);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Admin User", dto.getName());
        assertEquals("admin@djmart.com", dto.getEmail());
        assertEquals(Role.ADMIN, dto.getRole());

        // Verify serialized JSON strictly lacks password or passwordHash keys
        String json = JsonUtil.toJson(dto);
        assertFalse(json.contains("password"));
        assertFalse(json.contains("passwordHash"));
        assertFalse(json.contains("superSecretHashedPassword12345"));
    }

    @Test
    @DisplayName("Verify CartResponse and CartItemResponse calculate subtotals and totals accurately")
    void testCartCalculations() {
        CartItemResponse item1 = new CartItemResponse(
                1L, 101L, "Headphones", "http://img/1.png", new BigDecimal("150.00"), 2
        );
        CartItemResponse item2 = new CartItemResponse(
                2L, 102L, "Mouse", "http://img/2.png", new BigDecimal("40.50"), 1
        );

        assertEquals(new BigDecimal("300.00"), item1.getSubtotal());
        assertEquals(new BigDecimal("40.50"), item2.getSubtotal());

        CartResponse cart = new CartResponse(List.of(item1, item2));

        assertEquals(3, cart.getTotalItems());
        assertEquals(new BigDecimal("340.50"), cart.getGrandTotal());
    }

    @Test
    @DisplayName("Verify OrderItemResponse subtotal calculation")
    void testOrderItemResponse() {
        OrderItemResponse orderItem = new OrderItemResponse(
                1L, 201L, "Denim Jacket", "http://img/jacket.png", 3, new BigDecimal("79.95")
        );

        assertEquals(new BigDecimal("239.85"), orderItem.getSubtotal());
    }

    @Test
    @DisplayName("Verify PageResponse pagination calculation")
    void testPageResponse() {
        List<String> items = List.of("Item 1", "Item 2", "Item 3");
        PageResponse<String> page = new PageResponse<>(items, 1, 10, 25);

        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getPageSize());
        assertEquals(25, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @DisplayName("Verify all custom exceptions have correct HTTP status codes and error codes")
    void testCustomExceptions() {
        ValidationException valEx = new ValidationException("Validation failed");
        assertEquals(400, valEx.getStatusCode());
        assertEquals("VALIDATION_ERROR", valEx.getErrorCode());

        AuthenticationException authEx = new AuthenticationException("Invalid password");
        assertEquals(401, authEx.getStatusCode());
        assertEquals("UNAUTHENTICATED", authEx.getErrorCode());

        AuthorizationException forbiddenEx = new AuthorizationException("Access denied");
        assertEquals(403, forbiddenEx.getStatusCode());
        assertEquals("FORBIDDEN", forbiddenEx.getErrorCode());

        ProductNotFoundException prodNotFoundEx = new ProductNotFoundException(42L);
        assertEquals(404, prodNotFoundEx.getStatusCode());
        assertEquals("PRODUCT_NOT_FOUND", prodNotFoundEx.getErrorCode());

        InsufficientStockException stockEx = new InsufficientStockException(42L, 5, 2);
        assertEquals(409, stockEx.getStatusCode());
        assertEquals("INSUFFICIENT_STOCK", stockEx.getErrorCode());
        assertEquals(42L, stockEx.getProductId());
        assertEquals(5, stockEx.getRequestedQty());
        assertEquals(2, stockEx.getAvailableQty());

        ConflictException conflictEx = new ConflictException("User already exists");
        assertEquals(409, conflictEx.getStatusCode());
        assertEquals("CONFLICT", conflictEx.getErrorCode());

        OrderException orderEx = new OrderException("Cannot cancel delivered order");
        assertEquals(400, orderEx.getStatusCode());
        assertEquals("ORDER_ERROR", orderEx.getErrorCode());

        DatabaseException dbEx = new DatabaseException("Failed connection");
        assertEquals(500, dbEx.getStatusCode());
        assertEquals("DATABASE_ERROR", dbEx.getErrorCode());
    }
}
