package com.djmart.controller;

import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.dao.jdbc.ReviewDAOImpl;
import com.djmart.dao.jdbc.UserDAOImpl;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductRequest;
import com.djmart.dto.ProductResponse;
import com.djmart.dto.UserResponse;
import com.djmart.model.Role;
import com.djmart.service.ProductService;
import com.djmart.service.impl.ProductServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller orchestrating catalog browsing, product search, details presentation,
 * and seller product management.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products/*", "/api/products/*", "/api/v1/products/*"})
public class ProductServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServlet.class);

    private final ProductService productService;

    public ProductServlet() {
        this(new ProductServiceImpl(new ProductDAOImpl(), new ReviewDAOImpl()));
    }

    public ProductServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);
        boolean isApi = isJsonRequest(request);

        try {
            // Category list API
            if (path.equals("/api/products/categories") || path.equals("/api/v1/products/categories")) {
                List<String> categories = productService.getCategories();
                sendSuccess(response, HttpServletResponse.SC_OK, categories);
                return;
            }

            // Single product details
            Long productId = extractProductId(path);
            if (productId != null) {
                ProductResponse product = productService.getProductById(productId);
                if (isApi) {
                    sendSuccess(response, HttpServletResponse.SC_OK, product);
                } else {
                    request.setAttribute("product", product);
                    forwardToJsp(request, response, "buyer/product-details");
                }
                return;
            }

            // Search / Catalog list
            String keyword = getStringParam(request, "search");
            if (keyword == null) {
                keyword = getStringParam(request, "keyword");
            }
            String category = getStringParam(request, "category");
            BigDecimal minPrice = getBigDecimalParam(request, "minPrice");
            BigDecimal maxPrice = getBigDecimalParam(request, "maxPrice");
            String sort = getStringParam(request, "sort");

            String sortBy = "id";
            String sortOrder = "desc";
            if (sort != null) {
                switch (sort.toLowerCase()) {
                    case "price_asc" -> { sortBy = "price"; sortOrder = "asc"; }
                    case "price_desc" -> { sortBy = "price"; sortOrder = "desc"; }
                    case "newest" -> { sortBy = "created_at"; sortOrder = "desc"; }
                    case "name_asc" -> { sortBy = "name"; sortOrder = "asc"; }
                    default -> { sortBy = "id"; sortOrder = "desc"; }
                }
            } else {
                String explicitSortBy = getStringParam(request, "sortBy");
                String explicitSortOrder = getStringParam(request, "sortOrder");
                if (explicitSortBy != null) sortBy = explicitSortBy;
                if (explicitSortOrder != null) sortOrder = explicitSortOrder;
            }

            int page = getIntParam(request, "page", 1);
            int size = getIntParam(request, "size", 12);

            PageResponse<ProductResponse> pageResult = productService.searchProducts(
                    keyword, category, minPrice, maxPrice, sortBy, sortOrder, page, size);

            if (isApi) {
                sendSuccess(response, HttpServletResponse.SC_OK, pageResult);
            } else {
                List<String> categories = productService.getCategories();
                request.setAttribute("products", pageResult.getData());
                request.setAttribute("pageResult", pageResult);
                request.setAttribute("categories", categories);
                request.setAttribute("keyword", keyword);
                request.setAttribute("selectedCategory", category);
                request.setAttribute("minPrice", minPrice);
                request.setAttribute("maxPrice", maxPrice);
                request.setAttribute("sort", sort != null ? sort : "newest");
                request.setAttribute("page", page);
                request.setAttribute("size", size);
                forwardToJsp(request, response, "buyer/products");
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);

            ProductRequest req = parseRequestBody(request, ProductRequest.class);
            ProductResponse created = productService.createProduct(user.getId(), req);

            sendSuccess(response, HttpServletResponse.SC_CREATED, created, "Product listing created successfully");
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);
            String path = getPath(request);
            Long productId = extractProductId(path);

            ProductRequest req = parseRequestBody(request, ProductRequest.class);
            boolean isAdmin = user.getRole() == Role.ADMIN;
            ProductResponse updated = productService.updateProduct(user.getId(), productId, req, isAdmin);

            sendSuccess(response, HttpServletResponse.SC_OK, updated, "Product listing updated successfully");
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            requireRole(request, Role.SELLER, Role.ADMIN);
            UserResponse user = getAuthenticatedUser(request);
            String path = getPath(request);
            Long productId = extractProductId(path);

            boolean isAdmin = user.getRole() == Role.ADMIN;
            productService.deleteProduct(user.getId(), productId, isAdmin);

            sendSuccess(response, HttpServletResponse.SC_OK, null, "Product listing deleted successfully");
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private Long extractProductId(String path) {
        if (path == null) return null;
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            if (("products".equals(segments[i]) || "products/".equals(segments[i])) && i + 1 < segments.length) {
                String potentialId = segments[i + 1];
                if (!potentialId.isEmpty() && !"categories".equalsIgnoreCase(potentialId)) {
                    try {
                        return Long.parseLong(potentialId);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return null;
    }
}
