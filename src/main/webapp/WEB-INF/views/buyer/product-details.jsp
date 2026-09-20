<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="${product.name} — DjMart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2.5rem; padding-bottom: 5rem;">
    <!-- Breadcrumb Navigation -->
    <nav aria-label="Breadcrumb" style="margin-bottom: 1.5rem; font-size: 0.9rem; color: var(--color-text-muted);">
        <a href="${pageContext.request.contextPath}/" style="color: inherit;">Home</a>
        <span style="margin: 0 0.4rem;">/</span>
        <a href="${pageContext.request.contextPath}/products" style="color: inherit;">Products</a>
        <span style="margin: 0 0.4rem;">/</span>
        <a href="${pageContext.request.contextPath}/products?category=${fn:escapeXml(product.category)}" style="color: inherit;"><c:out value="${product.category}"/></a>
        <span style="margin: 0 0.4rem;">/</span>
        <span style="color: var(--color-primary); font-weight: 500;"><c:out value="${product.name}"/></span>
    </nav>

    <!-- Details Side-by-Side Layout -->
    <div class="product-details-layout">
        <!-- Left: Product Image & Gallery -->
        <div class="product-gallery">
            <div class="gallery-main-image">
                <img id="mainProductImg"
                     src="${product.imageUrl != null && not empty product.imageUrl ? product.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop'}"
                     alt="<c:out value='${product.name}'/>"
                     loading="eager">
            </div>

            <!-- Thumbnail strip (if image present, provides angle previews) -->
            <div class="gallery-thumbnails" id="galleryThumbnails">
                <div class="gallery-thumbnail active" data-img-src="${product.imageUrl}">
                    <img src="${product.imageUrl != null && not empty product.imageUrl ? product.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&auto=format&fit=crop'}"
                         alt="Main view">
                </div>
            </div>
        </div>

        <!-- Right: Information & Purchase Controls -->
        <div class="product-info-panel">
            <span class="product-category" style="font-size: 0.85rem; margin-bottom: 0.5rem;">
                <c:out value="${product.category}"/>
            </span>

            <h1><c:out value="${product.name}"/></h1>

            <div class="product-rating" style="margin-bottom: 1.25rem;">
                <span style="font-size: 1.1rem;">★</span>
                <span style="font-weight: 600; font-size: 1rem; color: var(--color-primary);">
                    <c:choose>
                        <c:when test="${product.averageRating != null && product.averageRating > 0}">
                            <fmt:formatNumber value="${product.averageRating}" maxFractionDigits="1"/>
                        </c:when>
                        <c:otherwise>New</c:otherwise>
                    </c:choose>
                </span>
                <span class="product-rating-count" style="font-size: 0.9rem;">
                    (<c:out value="${product.reviewCount != null ? product.reviewCount : 0}"/> customer reviews)
                </span>
            </div>

            <div class="product-price">
                ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
            </div>

            <div class="product-description-text">
                <c:out value="${product.description}"/>
            </div>

            <!-- Stock Status Indicator -->
            <div style="margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.75rem;">
                <span style="font-size: 0.9rem; font-weight: 600; color: var(--color-text-main);">Availability:</span>
                <c:choose>
                    <c:when test="${product.stockQty <= 0}">
                        <span class="badge badge-out-of-stock">Out of Stock</span>
                    </c:when>
                    <c:when test="${product.stockQty <= 5}">
                        <span class="badge badge-low-stock">Low Stock — Only ${product.stockQty} remaining</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-in-stock">In Stock (${product.stockQty} units available)</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Purchase Controls Form -->
            <c:choose>
                <c:when test="${product.stockQty > 0}">
                    <div style="margin-bottom: 1.5rem;">
                        <label for="productQty" class="form-label">Select Quantity</label>
                        <div class="quantity-selector-wrap">
                            <button type="button"
                                    id="decreaseQtyBtn"
                                    class="qty-btn"
                                    aria-label="Decrease quantity"
                                    disabled>
                                &minus;
                            </button>
                            <input type="number"
                                   id="productQty"
                                   name="quantity"
                                   class="qty-input"
                                   value="1"
                                   min="1"
                                   max="${product.stockQty}"
                                   readonly
                                   aria-label="Product quantity">
                            <button type="button"
                                    id="increaseQtyBtn"
                                    class="qty-btn"
                                    aria-label="Increase quantity"
                                    ${product.stockQty <= 1 ? 'disabled' : ''}>
                                &plus;
                            </button>
                        </div>
                    </div>

                    <div class="purchase-actions-grid">
                        <button type="button"
                                id="detailsAddToCartBtn"
                                class="btn btn-primary btn-block"
                                data-product-id="${product.id}"
                                data-max-stock="${product.stockQty}">
                            Add to Cart
                        </button>
                        <button type="button"
                                id="detailsBuyNowBtn"
                                class="btn btn-accent btn-block"
                                data-product-id="${product.id}">
                            Buy Now
                        </button>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-danger" role="alert" style="margin-top: 1rem;">
                        This item is currently sold out. Please check back later or explore alternative products in this category.
                    </div>
                </c:otherwise>
            </c:choose>

            <!-- Provenance & Guarantee Notes -->
            <div style="margin-top: 2.5rem; padding-top: 1.5rem; border-top: 1px solid var(--color-border-light); font-size: 0.875rem; color: var(--color-text-muted);">
                <p style="margin-bottom: 0.4rem;">
                    <strong>Fulfillment:</strong> Dispatched securely by <c:out value="${product.sellerName != null ? product.sellerName : 'Verified Independent Seller'}"/>.
                </p>
                <p>
                    <strong>Assurance:</strong> Authentic goods inspected under Anna University Capstone standards.
                </p>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/product-details.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
