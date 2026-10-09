package com.djmart.controller;

import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductRequest;
import com.djmart.dto.ProductResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.ProductNotFoundException;
import com.djmart.model.Role;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServletTest {

    private ProductService productService;
    private ProductServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        productService = mock(ProductService.class);
        servlet = new ProductServlet(productService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private ProductResponse createSampleProduct(Long id, String name, BigDecimal price, int stock) {
        ProductResponse p = new ProductResponse();
        p.setId(id);
        p.setName(name);
        p.setCategory("Electronics");
        p.setPrice(price);
        p.setStockQty(stock);
        p.setDescription("Test description");
        p.setAverageRating(4.5);
        p.setReviewCount(10);
        return p;
    }

    @Test
    @DisplayName("Catalog Browser View: GET /products forwards to buyer/products.jsp with catalog data")
    void testCatalogBrowse_BrowserJsp() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/products");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("size")).thenReturn("12");

        List<ProductResponse> list = List.of(createSampleProduct(1L, "Studio Headphones", new BigDecimal("14999.00"), 15));
        PageResponse<ProductResponse> pageResp = new PageResponse<>(list, 1, 12, 1L);
        when(productService.searchProducts(any(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(pageResp);
        when(productService.getCategories()).thenReturn(List.of("Electronics", "Fashion"));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("products"), eq(list));
        verify(request).setAttribute(eq("categories"), anyList());
        verify(request).getRequestDispatcher("/WEB-INF/views/buyer/products.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Product Details Browser View: GET /products/{id} forwards to buyer/product-details.jsp")
    void testProductDetails_BrowserJsp() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/products/5");
        when(request.getMethod()).thenReturn("GET");

        ProductResponse product = createSampleProduct(5L, "Mechanical Keyboard", new BigDecimal("8500.00"), 8);
        when(productService.getProductById(5L)).thenReturn(product);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("product"), eq(product));
        verify(request).getRequestDispatcher("/WEB-INF/views/buyer/product-details.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Product Search API: GET /api/products returns JSON PageResponse")
    void testSearchApi_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("search")).thenReturn("keyboard");

        List<ProductResponse> list = List.of(createSampleProduct(5L, "Mechanical Keyboard", new BigDecimal("8500.00"), 8));
        PageResponse<ProductResponse> pageResp = new PageResponse<>(list, 1, 12, 1L);
        when(productService.searchProducts(eq("keyboard"), any(), any(), any(), anyString(), anyString(), eq(1), eq(12)))
                .thenReturn(pageResp);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("Categories API: GET /api/products/categories returns distinct category strings")
    void testCategoriesApi_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/products/categories");
        when(request.getMethod()).thenReturn("GET");

        when(productService.getCategories()).thenReturn(List.of("Electronics", "Fashion", "Home"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Electronics"));
        assertTrue(json.contains("Fashion"));
    }

    @Test
    @DisplayName("Product Details API: GET /api/products/999 not found returns 404 NOT_FOUND")
    void testProductDetailsApi_NotFound() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/products/999");
        when(request.getMethod()).thenReturn("GET");

        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("Create Product API: SELLER can create product listing")
    void testCreateProduct_Seller_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);

        UserResponse sellerUser = new UserResponse(2L, "Seller One", "seller@djmart.com", Role.SELLER, Timestamp.from(Instant.now()));
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(sellerUser);

        String json = "{\"name\":\"Ceramic Mug\",\"description\":\"Handcrafted\",\"price\":850.00,\"stockQty\":20,\"category\":\"Home\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        ProductResponse created = createSampleProduct(10L, "Ceramic Mug", new BigDecimal("850.00"), 20);
        when(productService.createProduct(eq(2L), any(ProductRequest.class))).thenReturn(created);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
        assertTrue(responseWriter.toString().contains("Ceramic Mug"));
    }

    @Test
    @DisplayName("Create Product API: BUYER cannot create product listing (403 FORBIDDEN)")
    void testCreateProduct_Buyer_Forbidden() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);

        UserResponse buyerUser = new UserResponse(3L, "Buyer One", "buyer@djmart.com", Role.BUYER, Timestamp.from(Instant.now()));
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(buyerUser);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("\"success\":false"));
        assertTrue(responseWriter.toString().contains("FORBIDDEN"));
    }
}
