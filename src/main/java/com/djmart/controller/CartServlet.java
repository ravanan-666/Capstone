package com.djmart.controller;

import com.djmart.dao.jdbc.CartDAOImpl;
import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.dto.CartItemRequest;
import com.djmart.dto.CartItemResponse;
import com.djmart.dto.CartResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.ValidationException;
import com.djmart.service.CartService;
import com.djmart.service.impl.CartServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller orchestrating shopping cart interactions, inventory validation,
 * quantity adjustments, and server-authoritative totals.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart/*", "/api/cart/*", "/api/v1/cart/*"})
public class CartServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartServlet.class);

    private final CartService cartService;

    public CartServlet() {
        this(new CartServiceImpl(new CartDAOImpl(), new ProductDAOImpl()));
    }

    public CartServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserResponse user = requireAuthenticatedUser(request);
            CartResponse cart = cartService.getCart(user.getId());

            if (isJsonRequest(request)) {
                sendSuccess(response, HttpServletResponse.SC_OK, cart);
            } else {
                request.setAttribute("cart", cart);
                forwardToJsp(request, response, "buyer/cart");
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);
        try {
            UserResponse user = requireAuthenticatedUser(request);

            if (path.endsWith("/add") || path.endsWith("/items")) {
                handleAdd(request, response, user.getId());
            } else if (path.endsWith("/update")) {
                handleUpdate(request, response, user.getId());
            } else if (path.endsWith("/remove")) {
                handleRemove(request, response, user.getId());
            } else if (path.endsWith("/clear")) {
                handleClear(request, response, user.getId());
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Cart endpoint not found", "NOT_FOUND");
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserResponse user = requireAuthenticatedUser(request);
            handleUpdate(request, response, user.getId());
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserResponse user = requireAuthenticatedUser(request);
            Long cartItemId = getLongParam(request, "cartItemId");
            if (cartItemId == null) {
                cartItemId = extractIdFromPath(getPath(request));
            }

            if (cartItemId != null) {
                cartService.removeFromCart(user.getId(), cartItemId);
                CartResponse updatedCart = cartService.getCart(user.getId());
                sendSuccess(response, HttpServletResponse.SC_OK, updatedCart, "Item removed from cart");
            } else {
                cartService.clearCart(user.getId());
                sendSuccess(response, HttpServletResponse.SC_OK, new CartResponse(), "Cart cleared");
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws IOException {
        Long productId;
        int quantity;

        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            CartItemRequest req = parseRequestBody(request, CartItemRequest.class);
            if (req == null || req.getProductId() == null) {
                throw new ValidationException("Product ID is required");
            }
            productId = req.getProductId();
            quantity = req.getQuantity() != null ? req.getQuantity() : 1;
        } else {
            productId = getLongParam(request, "productId");
            quantity = getIntParam(request, "quantity", 1);
        }

        if (productId == null) {
            throw new ValidationException("Product ID is required");
        }

        cartService.addToCart(userId, productId, quantity);
        CartResponse updatedCart = cartService.getCart(userId);
        sendSuccess(response, HttpServletResponse.SC_OK, updatedCart, "Product added to cart");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws IOException {
        Long cartItemId;
        int quantity;

        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            CartItemRequest req = parseRequestBody(request, CartItemRequest.class);
            cartItemId = req != null ? req.getCartItemId() : null;
            quantity = req != null && req.getQuantity() != null ? req.getQuantity() : 1;
        } else {
            cartItemId = getLongParam(request, "cartItemId");
            quantity = getIntParam(request, "quantity", 1);
        }

        if (cartItemId == null) {
            cartItemId = extractIdFromPath(getPath(request));
        }

        if (cartItemId == null) {
            throw new ValidationException("Cart item ID is required");
        }

        cartService.updateQuantity(userId, cartItemId, quantity);
        CartResponse updatedCart = cartService.getCart(userId);
        sendSuccess(response, HttpServletResponse.SC_OK, updatedCart, "Cart quantity updated");
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws IOException {
        Long cartItemId = getLongParam(request, "cartItemId");
        if (cartItemId == null) {
            cartItemId = extractIdFromPath(getPath(request));
        }
        if (cartItemId == null) {
            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                CartItemRequest req = parseRequestBody(request, CartItemRequest.class);
                if (req != null) cartItemId = req.getCartItemId();
            }
        }

        if (cartItemId == null) {
            throw new ValidationException("Cart item ID is required");
        }

        cartService.removeFromCart(userId, cartItemId);
        CartResponse updatedCart = cartService.getCart(userId);
        sendSuccess(response, HttpServletResponse.SC_OK, updatedCart, "Item removed from cart");
    }

    private void handleClear(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws IOException {
        cartService.clearCart(userId);
        sendSuccess(response, HttpServletResponse.SC_OK, new CartResponse(), "Cart cleared successfully");
    }

    private Long extractIdFromPath(String path) {
        if (path == null) return null;
        String[] segments = path.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isEmpty()) {
                try {
                    return Long.parseLong(segments[i]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
