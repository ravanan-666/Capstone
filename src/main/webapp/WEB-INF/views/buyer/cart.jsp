<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Shopping Bag — DjMart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2.5rem; padding-bottom: 5rem;">
    <!-- Page Header -->
    <div style="margin-bottom: 2rem;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.5rem;">
            Order Review
        </span>
        <h1 style="font-size: 2.2rem; font-weight: 600; color: var(--color-primary); margin-bottom: 0.5rem;">
            Your Shopping Bag
        </h1>
    </div>

    <!-- Empty Bag State -->
    <div id="cartEmptyState" class="empty-state" style="display: ${empty cart || empty cart.items ? 'block' : 'none'};">
        <div class="empty-state-icon">❖</div>
        <h3>Your shopping bag is empty</h3>
        <p>Explore our curated collections of artisanal, tech, and everyday essentials to begin shopping.</p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
            Explore Collection
        </a>
    </div>

    <!-- Active Cart Layout -->
    <div id="cartActiveLayout" class="cart-page-layout" style="display: ${not empty cart && not empty cart.items ? 'grid' : 'none'};">
        <!-- Left: Cart Items List -->
        <div class="cart-items-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid var(--color-border);">
                <h2 style="font-size: 1.3rem; margin: 0;">Bag Items (<span id="cartItemsCount">${cart.totalItems}</span>)</h2>
                <button type="button" id="clearCartBtn" class="cart-item-remove-btn" style="color: var(--color-text-muted);">
                    Clear Bag
                </button>
            </div>

            <div id="cartRowsContainer">
                <c:forEach items="${cart.items}" var="item">
                    <div class="cart-item-row" data-cart-item-id="${item.id}">
                        <img src="${item.productImageUrl != null && not empty item.productImageUrl ? item.productImageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&auto=format&fit=crop'}"
                             alt="<c:out value='${item.productName}'/>"
                             class="cart-item-image">

                        <div>
                            <div class="cart-item-title"><c:out value="${item.productName}"/></div>
                            <div class="cart-item-price">
                                Unit: ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00"/>
                            </div>
                        </div>

                        <div>
                            <div class="quantity-selector-wrap">
                                <button type="button"
                                        class="qty-btn cart-qty-decrease"
                                        data-cart-item-id="${item.id}"
                                        aria-label="Decrease quantity for <c:out value='${item.productName}'/>"
                                        ${item.quantity <= 1 ? 'disabled' : ''}>
                                    &minus;
                                </button>
                                <input type="number"
                                       class="qty-input cart-qty-input"
                                       data-cart-item-id="${item.id}"
                                       value="${item.quantity}"
                                       min="1"
                                       readonly
                                       aria-label="Quantity">
                                <button type="button"
                                        class="qty-btn cart-qty-increase"
                                        data-cart-item-id="${item.id}"
                                        aria-label="Increase quantity for <c:out value='${item.productName}'/>">
                                    &plus;
                                </button>
                            </div>
                        </div>

                        <div class="cart-item-subtotal">
                            ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00"/>
                        </div>

                        <div>
                            <button type="button"
                                    class="cart-item-remove-btn cart-remove-item"
                                    data-cart-item-id="${item.id}"
                                    aria-label="Remove <c:out value='${item.productName}'/> from cart">
                                Remove
                            </button>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <!-- Right: Order Summary -->
        <aside class="order-summary-card">
            <h2 style="font-size: 1.3rem; margin-bottom: 1.5rem;">Order Summary</h2>

            <div class="summary-line">
                <span>Items Subtotal</span>
                <span id="summarySubtotal">₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00"/></span>
            </div>

            <div class="summary-line">
                <span>Standard Delivery</span>
                <span style="color: var(--color-success); font-weight: 500;">Complimentary</span>
            </div>

            <div class="summary-line total">
                <span>Total Amount</span>
                <span id="summaryTotal">₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00"/></span>
            </div>

            <p style="font-size: 0.8rem; color: var(--color-text-muted); margin: 1.25rem 0;">
                Taxes and delivery calculations are verified directly against backend inventory state.
            </p>

            <a href="${pageContext.request.contextPath}/checkout" id="proceedCheckoutBtn" class="btn btn-primary btn-block">
                Proceed to Checkout
            </a>

            <div style="text-align: center; margin-top: 1rem;">
                <a href="${pageContext.request.contextPath}/products" style="font-size: 0.9rem; color: var(--color-text-muted);">
                    &larr; Continue Exploring
                </a>
            </div>
        </aside>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/cart.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
