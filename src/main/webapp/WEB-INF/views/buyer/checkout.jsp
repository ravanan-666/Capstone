<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Secure Checkout — DJ Mart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 5rem;">
    <!-- Breadcrumb -->
    <nav aria-label="Breadcrumb" style="margin-bottom: 1.5rem; font-size: 0.9rem; color: var(--color-text-muted);">
        <a href="${pageContext.request.contextPath}/" style="color: inherit;">Home</a>
        <span style="margin: 0 0.4rem;">/</span>
        <a href="${pageContext.request.contextPath}/cart" style="color: inherit;">Bag</a>
        <span style="margin: 0 0.4rem;">/</span>
        <span style="color: var(--color-primary); font-weight: 500;">Checkout</span>
    </nav>

    <h1 style="font-size: 2.2rem; margin-bottom: 2rem;">Checkout & Finalize</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" role="alert" style="margin-bottom: 1.5rem;">
            <c:out value="${errorMessage}"/>
        </div>
    </c:if>

    <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="POST" novalidate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

        <div class="checkout-layout">
            <!-- Left Column: Shipping & Payment Details -->
            <div class="checkout-main">
                <!-- Customer Details -->
                <div class="checkout-section-card">
                    <h3>1. Customer Identity</h3>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label class="form-label" for="customerName">Full Name</label>
                            <input type="text" id="customerName" class="form-control"
                                   value="<c:out value='${user.name}'/>" readonly>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="customerEmail">Account Email</label>
                            <input type="email" id="customerEmail" class="form-control"
                                   value="<c:out value='${user.email}'/>" readonly>
                        </div>
                    </div>
                </div>

                <!-- Shipping Address -->
                <div class="checkout-section-card">
                    <h3>2. Delivery Destination</h3>
                    <div class="form-group">
                        <label class="form-label" for="streetAddress">Street Address / Suite <span style="color: var(--color-danger);">*</span></label>
                        <input type="text" id="streetAddress" name="streetAddress" class="form-control"
                               placeholder="e.g. 142 Heritage Boulevard, Apartment 4B" required minlength="5">
                        <small style="color: var(--color-text-muted); font-size: 0.8rem;">Minimum 5 characters required for courier delivery.</small>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label class="form-label" for="city">City <span style="color: var(--color-danger);">*</span></label>
                            <input type="text" id="city" name="city" class="form-control" placeholder="Chennai" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="state">State / Province <span style="color: var(--color-danger);">*</span></label>
                            <input type="text" id="state" name="state" class="form-control" placeholder="Tamil Nadu" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="postalCode">Postal PIN Code <span style="color: var(--color-danger);">*</span></label>
                            <input type="text" id="postalCode" name="postalCode" class="form-control" placeholder="600025" required>
                        </div>
                    </div>

                    <!-- Hidden concatenated shipping address for backend validation -->
                    <input type="hidden" id="fullShippingAddress" name="shippingAddress" value="">
                </div>

                <!-- Mock Payment Section UI -->
                <div class="checkout-section-card">
                    <h3>3. Payment Method (Mock Flow)</h3>
                    <p style="font-size: 0.875rem; color: var(--color-text-muted); margin-bottom: 1.25rem;">
                        Demonstration environment: No live funds will be debited. Select a payment instrument to verify order authorization.
                    </p>

                    <div class="payment-methods-grid">
                        <label class="payment-method-card selected" id="cardOptionLabel">
                            <input type="radio" name="paymentMethod" value="CARD" checked>
                            <div>
                                <strong style="display: block; font-size: 0.95rem;">Credit / Debit Card</strong>
                                <span style="font-size: 0.8rem; color: var(--color-text-muted);">Visa, MasterCard, RuPay</span>
                            </div>
                        </label>

                        <label class="payment-method-card" id="upiOptionLabel">
                            <input type="radio" name="paymentMethod" value="UPI">
                            <div>
                                <strong style="display: block; font-size: 0.95rem;">Instant UPI / QR</strong>
                                <span style="font-size: 0.8rem; color: var(--color-text-muted);">GPay, PhonePe, Paytm</span>
                            </div>
                        </label>

                        <label class="payment-method-card" id="codOptionLabel">
                            <input type="radio" name="paymentMethod" value="COD">
                            <div>
                                <strong style="display: block; font-size: 0.95rem;">Cash on Delivery</strong>
                                <span style="font-size: 0.8rem; color: var(--color-text-muted);">Pay upon courier receipt</span>
                            </div>
                        </label>
                    </div>

                    <!-- Card Details Input (Simulated) -->
                    <div id="cardDetailsSection" class="payment-details-box">
                        <div class="form-group">
                            <label class="form-label" for="mockCardNumber">Card Number</label>
                            <input type="text" id="mockCardNumber" class="form-control" placeholder="4532 •••• •••• 8912" maxlength="19">
                        </div>
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label class="form-label" for="mockCardExpiry">Expiry Date</label>
                                <input type="text" id="mockCardExpiry" class="form-control" placeholder="MM/YY" maxlength="5">
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="mockCardCvv">Security CVV</label>
                                <input type="password" id="mockCardCvv" class="form-control" placeholder="•••" maxlength="4">
                            </div>
                        </div>
                    </div>

                    <!-- UPI Input (Simulated) -->
                    <div id="upiDetailsSection" class="payment-details-box" style="display: none;">
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" for="mockUpiId">Virtual Payment Address (VPA / UPI ID)</label>
                            <input type="text" id="mockUpiId" class="form-control" placeholder="username@okhdfcbank">
                        </div>
                    </div>

                    <!-- COD Notice -->
                    <div id="codDetailsSection" class="payment-details-box" style="display: none;">
                        <p style="margin: 0; font-size: 0.9rem; color: var(--color-text-main);">
                            ✓ Cash on Delivery confirmed. Please keep exact change ready upon parcel arrival.
                        </p>
                    </div>

                    <input type="hidden" name="paymentConfirmed" value="true">
                </div>
            </div>

            <!-- Right Column: Authoritative Order Summary -->
            <div class="checkout-sidebar">
                <div class="order-summary-card">
                    <h3 style="font-size: 1.3rem; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--color-border-light);">
                        Order Summary (${cart.itemCount} items)
                    </h3>

                    <!-- Line Items Preview -->
                    <div style="max-height: 280px; overflow-y: auto; margin-bottom: 1.25rem; padding-right: 0.5rem;">
                        <c:forEach items="${cart.items}" var="item">
                            <div class="order-line-item">
                                <img src="${item.productImageUrl != null && not empty item.productImageUrl ? item.productImageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=120&auto=format&fit=crop'}"
                                     alt="<c:out value='${item.productName}'/>"
                                     class="order-line-thumb">
                                <div style="flex: 1; min-width: 0;">
                                    <h4 style="font-size: 0.9rem; margin-bottom: 0.2rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                        <c:out value="${item.productName}"/>
                                    </h4>
                                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">
                                        Qty: ${item.quantity} &times; ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00"/>
                                    </div>
                                </div>
                                <div style="font-weight: 600; font-size: 0.9rem;">
                                    ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00"/>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Calculations -->
                    <div class="summary-line">
                        <span>Items Subtotal</span>
                        <span>₹<fmt:formatNumber value="${cart.totalAmount}" pattern="#,##0.00"/></span>
                    </div>
                    <div class="summary-line">
                        <span>Curated Logistics</span>
                        <span style="color: var(--color-success); font-weight: 600;">Complimentary</span>
                    </div>
                    <div class="summary-line">
                        <span>Applicable Taxes</span>
                        <span>Included</span>
                    </div>

                    <div class="summary-line total">
                        <span>Total Due</span>
                        <span>₹<fmt:formatNumber value="${cart.totalAmount}" pattern="#,##0.00"/></span>
                    </div>

                    <button type="submit" id="placeOrderBtn" class="btn btn-primary btn-block" style="margin-top: 1.5rem; padding: 1rem;">
                        <span id="placeOrderBtnText">Place Authoritative Order</span>
                        <span id="placeOrderSpinner" style="display: none;">Processing Order...</span>
                    </button>

                    <div style="margin-top: 1.25rem; font-size: 0.8rem; color: var(--color-text-muted); text-align: center; line-height: 1.5;">
                        Protected by 256-bit encrypted checkout and atomic database inventory management.
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>

<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/checkout.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
