<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="${product.name} — DJ Mart" scope="request"/>
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
                <c:set var="detailImg" value="${product.imageUrl != null && not empty product.imageUrl ? product.imageUrl : '/static/images/placeholder.svg'}"/>
                <img id="mainProductImg"
                     src="<c:url value='${detailImg}'/>"
                     alt="<c:out value='${product.name}'/>"
                     loading="eager"
                     onerror="if(this.src!=='<c:url value="/static/images/placeholder.svg"/>'){this.onerror=null;this.src='<c:url value="/static/images/placeholder.svg"/>';}">
            </div>

            <!-- Thumbnail strip (if image present, provides angle previews) -->
            <div class="gallery-thumbnails" id="galleryThumbnails">
                <div class="gallery-thumbnail active" data-img-src="<c:url value='${detailImg}'/>">
                    <img src="<c:url value='${detailImg}'/>"
                         alt="Main view"
                         onerror="if(this.src!=='<c:url value="/static/images/placeholder.svg"/>'){this.onerror=null;this.src='<c:url value="/static/images/placeholder.svg"/>';}">
                </div>
            </div>
        </div>

        <!-- Right: Information & Purchase Controls -->
        <div class="product-info-panel">
            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.35rem;">
                <span class="product-category" style="font-size: 0.85rem; margin-bottom: 0;">
                    <c:out value="${product.category}"/>
                </span>
                <c:if test="${not empty product.brand}">
                    <span style="font-size: 0.85rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">
                        Brand: <c:out value="${product.brand}"/>
                    </span>
                </c:if>
            </div>

            <h1 style="font-size: 2rem; margin-bottom: 0.5rem;"><c:out value="${product.name}"/></h1>
            <div style="font-size: 0.8rem; color: var(--color-text-muted); margin-bottom: 0.75rem;">
                SKU: <code><c:out value="${product.sku != null ? product.sku : 'DJM-PROD-'.concat(product.id)}"/></code>
            </div>

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

            <div style="display: flex; align-items: baseline; gap: 0.75rem; flex-wrap: wrap; margin-bottom: 0.35rem;">
                <span class="product-price" style="font-size: 2.2rem; font-weight: 700; color: var(--color-primary);">
                    ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
                </span>
                <c:if test="${product.originalPrice != null && product.originalPrice > product.price}">
                    <span style="font-size: 1.25rem; color: var(--color-text-muted); text-decoration: line-through;">
                        ₹<fmt:formatNumber value="${product.originalPrice}" pattern="#,##0.00"/>
                    </span>
                    <span class="badge" style="background: #dcfce7; color: #15803d; font-size: 0.85rem; padding: 0.35rem 0.65rem; border-radius: 4px; font-weight: 700;">
                        ${product.discountPercent}% OFF
                    </span>
                </c:if>
            </div>
            <div style="font-size: 0.8rem; color: var(--color-success); font-weight: 600; margin-bottom: 1.25rem;">
                ✓ Verified Current Market Price in INR
                <c:if test="${product.priceVerifiedAt != null}">
                    &bull; <span style="color: var(--color-text-muted); font-weight: normal;">Updated: <fmt:formatDate value="${product.priceVerifiedAt}" pattern="dd MMM yyyy"/></span>
                </c:if>
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

    <!-- Customer Reviews & Ratings Section -->
    <div id="reviewsSection" class="reviews-container">
        <div style="display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; gap: 1rem; margin-bottom: 2rem;">
            <div>
                <h2 style="font-size: 1.85rem; margin-bottom: 0.25rem;">Client Reviews &amp; Reflections</h2>
                <div style="font-size: 0.95rem; color: var(--color-text-muted);">
                    Based on verified acquisitions through DJ Mart Atelier
                </div>
            </div>

            <div style="display: flex; align-items: center; gap: 0.75rem;">
                <span style="font-size: 1.75rem; font-family: 'Playfair Display', serif; font-weight: 700; color: var(--color-primary);">
                    <c:choose>
                        <c:when test="${product.averageRating != null && product.averageRating > 0}">
                            <fmt:formatNumber value="${product.averageRating}" maxFractionDigits="1"/>
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </span>
                <span style="color: #f59e0b; font-size: 1.25rem;">★★★★★</span>
                <span style="color: var(--color-text-muted); font-size: 0.9rem;">
                    (${product.reviewCount != null ? product.reviewCount : 0} reviews)
                </span>
            </div>
        </div>

        <!-- Add Review Form (for logged-in clients) -->
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <div id="reviewFormContainer" class="checkout-section-card" style="margin-bottom: 2.5rem;">
                    <h3>Share Your Assessment</h3>
                    <p style="font-size: 0.875rem; color: var(--color-text-muted); margin-bottom: 1.25rem;">
                        Verified reviews may be submitted once your order containing this item has been delivered.
                    </p>

                    <form id="submitReviewForm">
                        <input type="hidden" name="productId" id="reviewProductId" value="${product.id}">
                        <input type="hidden" id="selectedRating" name="rating" value="5">

                        <div class="form-group" style="margin-bottom: 1.25rem;">
                            <label class="form-label">Your Rating</label>
                            <div class="star-rating-select" id="starRatingSelect" aria-label="Select star rating from 1 to 5">
                                <span class="star-icon active" data-rating="1">★</span>
                                <span class="star-icon active" data-rating="2">★</span>
                                <span class="star-icon active" data-rating="3">★</span>
                                <span class="star-icon active" data-rating="4">★</span>
                                <span class="star-icon active" data-rating="5">★</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="reviewComment">Written Assessment</label>
                            <textarea id="reviewComment" name="comment" class="form-control" rows="3"
                                      placeholder="Reflect on craftsmanship, material quality, and fulfillment experience..."
                                      maxlength="1000"></textarea>
                        </div>

                        <button type="submit" id="submitReviewBtn" class="btn btn-primary">
                            Submit Assessment
                        </button>
                    </form>
                </div>
            </c:when>
            <c:otherwise>
                <div class="checkout-section-card" style="margin-bottom: 2.5rem; text-align: center; padding: 2rem;">
                    <p style="margin-bottom: 1rem; color: var(--color-text-muted);">
                        Have you received this item? Sign in to your account to contribute a verified review.
                    </p>
                    <a href="${pageContext.request.contextPath}/auth/login?redirect=/products/${product.id}" class="btn btn-outline">
                        Sign In to Review
                    </a>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Existing Reviews List Container -->
        <div id="reviewsList" class="review-list" data-product-id="${product.id}">
            <div style="text-align: center; padding: 2rem; color: var(--color-text-muted);" id="reviewsLoading">
                Loading reviews...
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/api-client.js"></script>
<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/product-details.js"></script>
<script src="${pageContext.request.contextPath}/static/js/reviews.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
