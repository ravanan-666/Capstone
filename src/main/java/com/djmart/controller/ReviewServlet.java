package com.djmart.controller;

import com.djmart.dao.jdbc.OrderDAOImpl;
import com.djmart.dao.jdbc.OrderItemDAOImpl;
import com.djmart.dao.jdbc.ProductDAOImpl;
import com.djmart.dao.jdbc.ReviewDAOImpl;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ReviewRequest;
import com.djmart.dto.ReviewResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.ValidationException;
import com.djmart.model.Role;
import com.djmart.service.ReviewService;
import com.djmart.service.impl.ReviewServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller orchestrating review submissions, buyer eligibility checks,
 * and paginated review listing on products.
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {
        "/reviews/*",
        "/api/reviews/*",
        "/api/v1/reviews/*"
})
public class ReviewServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServlet.class);

    private final ReviewService reviewService;

    public ReviewServlet() {
        this(new ReviewServiceImpl(new ReviewDAOImpl(), new ProductDAOImpl(), new OrderDAOImpl(), new OrderItemDAOImpl()));
    }

    public ReviewServlet(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);

        try {
            // Check review eligibility
            if (path.contains("/eligibility")) {
                UserResponse user = requireAuthenticatedUser(request);
                Long productId = getLongParam(request, "productId");
                if (productId == null) {
                    throw new ValidationException("Product ID is required to check eligibility");
                }
                boolean eligible = reviewService.isEligibleToReview(user.getId(), productId);
                Map<String, Object> result = new HashMap<>();
                result.put("eligible", eligible);
                result.put("productId", productId);
                sendSuccess(response, HttpServletResponse.SC_OK, result);
                return;
            }

            // Get product reviews
            Long productId = extractProductId(path);
            if (productId == null) {
                productId = getLongParam(request, "productId");
            }

            if (productId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required", "MISSING_PRODUCT_ID");
                return;
            }

            int page = getIntParam(request, "page", 1);
            int size = getIntParam(request, "size", 10);
            PageResponse<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, page, size);
            sendSuccess(response, HttpServletResponse.SC_OK, reviews);

        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserResponse user = requireAuthenticatedUser(request);
            ReviewRequest reviewReq;

            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                reviewReq = parseRequestBody(request, ReviewRequest.class);
            } else {
                Long productId = getLongParam(request, "productId");
                int rating = getIntParam(request, "rating", 5);
                String comment = getStringParam(request, "comment");
                reviewReq = new ReviewRequest(productId, rating, comment);
            }

            if (reviewReq == null || reviewReq.getProductId() == null) {
                throw new ValidationException("Product ID and rating are required");
            }

            ReviewResponse created = reviewService.addReview(user.getId(), reviewReq);
            LOGGER.info("Review created ID: {} by user: {} for product: {}",
                    created.getId(), user.getId(), reviewReq.getProductId());

            if (isJsonRequest(request)) {
                sendSuccess(response, HttpServletResponse.SC_CREATED, created, "Review submitted successfully");
            } else {
                redirect(request, response, "/products/" + reviewReq.getProductId() + "?reviewed=true");
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserResponse user = requireAuthenticatedUser(request);
            Long reviewId = extractIdFromPath(getPath(request));
            if (reviewId == null) {
                reviewId = getLongParam(request, "reviewId");
            }

            if (reviewId == null) {
                throw new ValidationException("Review ID is required for deletion");
            }

            boolean isAdmin = user.getRole() == Role.ADMIN;
            reviewService.deleteReview(reviewId, user.getId(), isAdmin);
            sendSuccess(response, HttpServletResponse.SC_OK, null, "Review deleted successfully");
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    private Long extractProductId(String path) {
        if (path == null) return null;
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            if ("product".equalsIgnoreCase(segments[i]) && i + 1 < segments.length) {
                try {
                    return Long.parseLong(segments[i + 1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
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
