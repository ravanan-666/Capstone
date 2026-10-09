package com.djmart.controller;

import com.djmart.dto.PageResponse;
import com.djmart.dto.ReviewRequest;
import com.djmart.dto.ReviewResponse;
import com.djmart.dto.UserResponse;
import com.djmart.model.Role;
import com.djmart.service.ReviewService;
import com.djmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewServletTest {

    private ReviewService reviewService;
    private ReviewServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        reviewService = mock(ReviewService.class);
        servlet = new ReviewServlet(reviewService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
    }

    private UserResponse createBuyerUser(Long id) {
        return new UserResponse(id, "Buyer", "buyer@djmart.com", Role.BUYER, Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("GET /api/reviews/product/{id}: Retrieves reviews for product")
    void testGetReviews_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/reviews/product/10");
        PageResponse<ReviewResponse> page = new PageResponse<>(Collections.emptyList(), 1, 10, 0);
        when(reviewService.getReviewsByProduct(10L, 1, 10)).thenReturn(page);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }

    @Test
    @DisplayName("GET /api/reviews/eligibility: Checks if buyer can review product")
    void testEligibility_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/reviews/eligibility");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createBuyerUser(1L));
        when(request.getParameter("productId")).thenReturn("10");
        when(reviewService.isEligibleToReview(1L, 10L)).thenReturn(true);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"eligible\":true"));
    }

    @Test
    @DisplayName("POST /api/reviews: Submits a review and returns 201 CREATED")
    void testSubmitReview_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/reviews");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createBuyerUser(1L));
        when(request.getParameter("productId")).thenReturn("10");
        when(request.getParameter("rating")).thenReturn("5");
        when(request.getParameter("comment")).thenReturn("Outstanding quality and finish");

        ReviewResponse created = new ReviewResponse(1L, 10L, 1L, "Buyer", 5, "Outstanding quality and finish", Timestamp.from(Instant.now()));
        when(reviewService.addReview(eq(1L), any(ReviewRequest.class))).thenReturn(created);

        servlet.doPost(request, response);

        verify(reviewService).addReview(eq(1L), any(ReviewRequest.class));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        assertTrue(responseWriter.toString().contains("Review submitted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id}: Deletes review")
    void testDeleteReview_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/reviews/1");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createBuyerUser(1L));
        when(reviewService.deleteReview(1L, 1L, false)).thenReturn(true);

        servlet.doDelete(request, response);

        verify(reviewService).deleteReview(1L, 1L, false);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Review deleted successfully"));
    }
}
