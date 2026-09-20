<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Curated Collection — DjMart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 4rem;">
    <!-- Editorial Page Heading -->
    <div style="margin-bottom: 2rem;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.5rem;">
            Marketplace Catalog
        </span>
        <h1 style="font-size: 2.4rem; font-weight: 600; color: var(--color-primary); margin-bottom: 0.5rem;">
            Curated Artisanal & Precision Products
        </h1>
        <p style="color: var(--color-text-muted); font-size: 1.05rem;">
            Explore exceptional goods directly from independent artisans and verified merchants.
        </p>
    </div>

    <!-- Search & Filter Controls -->
    <div class="filter-bar-card">
        <form id="filterForm" onsubmit="event.preventDefault();" class="filter-grid">
            <!-- Keyword Search -->
            <div class="form-group" style="margin-bottom: 0;">
                <label for="searchInput" class="form-label">Search Catalog</label>
                <input type="text"
                       id="searchInput"
                       name="search"
                       class="form-control"
                       placeholder="Search by title, brand, or specs..."
                       value="<c:out value='${keyword}'/>"
                       autocomplete="off">
            </div>

            <!-- Category Filter -->
            <div class="form-group" style="margin-bottom: 0;">
                <label for="categorySelect" class="form-label">Category</label>
                <select id="categorySelect" name="category" class="form-control">
                    <option value="">All Categories</option>
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat}" ${selectedCategory == cat ? 'selected' : ''}>
                            <c:out value="${cat}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- Price Range Filter -->
            <div class="form-group" style="margin-bottom: 0;">
                <label class="form-label">Price Range (₹)</label>
                <div class="price-range-inputs">
                    <input type="number"
                           id="minPriceInput"
                           name="minPrice"
                           class="form-control"
                           placeholder="Min"
                           min="0"
                           value="${minPrice != null ? minPrice : ''}">
                    <span class="price-separator">–</span>
                    <input type="number"
                           id="maxPriceInput"
                           name="maxPrice"
                           class="form-control"
                           placeholder="Max"
                           min="0"
                           value="${maxPrice != null ? maxPrice : ''}">
                </div>
            </div>

            <!-- Sorting Control -->
            <div class="form-group" style="margin-bottom: 0;">
                <label for="sortSelect" class="form-label">Sort By</label>
                <select id="sortSelect" name="sort" class="form-control">
                    <option value="newest" ${sort == 'newest' ? 'selected' : ''}>Newest Arrivals</option>
                    <option value="price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                    <option value="price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                    <option value="name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Product Name: A to Z</option>
                </select>
            </div>

            <!-- Action Controls -->
            <div style="display: flex; gap: 0.5rem; align-items: flex-end;">
                <button type="button" id="clearFiltersBtn" class="btn btn-outline" style="white-space: nowrap;">
                    Clear Filters
                </button>
            </div>
        </form>
    </div>

    <!-- Active Filter Status & Result Count -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <span id="resultsCountText" style="font-size: 0.95rem; color: var(--color-text-muted);">
            Showing <strong id="currentCount">${pageResult != null ? pageResult.totalElements : 0}</strong> products
        </span>
        <div id="loadingIndicator" style="display: none; font-size: 0.9rem; color: var(--color-accent); font-weight: 600;">
            Refreshing catalog...
        </div>
    </div>

    <!-- Product Grid -->
    <div id="productsGrid" class="products-grid">
        <c:forEach items="${products}" var="p">
            <article class="product-card" data-product-id="${p.id}">
                <div class="product-card-image-wrap">
                    <a href="${pageContext.request.contextPath}/products/${p.id}">
                        <img src="${p.imageUrl != null && not empty p.imageUrl ? p.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop'}"
                             alt="<c:out value='${p.name}'/>"
                             class="product-card-image"
                             loading="lazy">
                    </a>
                </div>
                <div class="product-card-content">
                    <div class="product-category"><c:out value="${p.category}"/></div>
                    <h3 class="product-title">
                        <a href="${pageContext.request.contextPath}/products/${p.id}"><c:out value="${p.name}"/></a>
                    </h3>

                    <div class="product-rating">
                        <span>★</span>
                        <span>
                            <c:choose>
                                <c:when test="${p.averageRating != null && p.averageRating > 0}">
                                    <fmt:formatNumber value="${p.averageRating}" maxFractionDigits="1"/>
                                </c:when>
                                <c:otherwise>Unrated</c:otherwise>
                            </c:choose>
                        </span>
                        <span class="product-rating-count">(${p.reviewCount != null ? p.reviewCount : 0})</span>
                    </div>

                    <div class="product-price-stock">
                        <span class="product-price">₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00"/></span>
                        <c:choose>
                            <c:when test="${p.stockQty <= 0}">
                                <span class="badge badge-out-of-stock">Out of Stock</span>
                            </c:when>
                            <c:when test="${p.stockQty <= 5}">
                                <span class="badge badge-low-stock">Only ${p.stockQty} left</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-in-stock">In Stock</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="product-actions">
                        <c:choose>
                            <c:when test="${p.stockQty <= 0}">
                                <button type="button" class="btn btn-outline btn-block" disabled>Unavailable</button>
                            </c:when>
                            <c:otherwise>
                                <button type="button"
                                        class="btn btn-primary btn-block add-to-cart-btn"
                                        data-product-id="${p.id}"
                                        aria-label="Add <c:out value='${p.name}'/> to Cart">
                                    Add to Cart
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </article>
        </c:forEach>
    </div>

    <!-- Empty Catalog State -->
    <div id="emptyCatalog" class="empty-state" style="display: ${empty products ? 'block' : 'none'};">
        <div class="empty-state-icon">❖</div>
        <h3>No Products Found</h3>
        <p>No products match the selected criteria. Try adjusting your keyword, category, or price boundaries.</p>
        <button type="button" id="clearFiltersEmptyBtn" class="btn btn-primary">Reset Filters</button>
    </div>

    <!-- Pagination Controls -->
    <nav id="paginationNav"
         class="pagination-container"
         aria-label="Product Catalog Pagination"
         style="display: ${pageResult != null && pageResult.totalPages > 1 ? 'flex' : 'none'};">
        <button type="button"
                id="prevPageBtn"
                class="page-btn"
                ${pageResult == null || pageResult.page <= 1 ? 'disabled' : ''}
                aria-label="Previous Page">
            &larr; Prev
        </button>

        <span id="pageNumbersContainer" style="display: flex; gap: 0.35rem;">
            <c:if test="${pageResult != null}">
                <c:forEach begin="1" end="${pageResult.totalPages}" var="i">
                    <button type="button"
                            class="page-btn ${pageResult.page == i ? 'active' : ''}"
                            data-page="${i}">
                        ${i}
                    </button>
                </c:forEach>
            </c:if>
        </span>

        <button type="button"
                id="nextPageBtn"
                class="page-btn"
                ${pageResult == null || pageResult.page >= pageResult.totalPages ? 'disabled' : ''}
                aria-label="Next Page">
            Next &rarr;
        </button>
    </nav>
</div>

<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/products.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
