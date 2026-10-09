<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Order #ORD-${order.id} — DJ Mart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 5rem;">
    <!-- Breadcrumb -->
    <nav aria-label="Breadcrumb" style="margin-bottom: 1.5rem; font-size: 0.9rem; color: var(--color-text-muted);">
        <a href="${pageContext.request.contextPath}/" style="color: inherit;">Home</a>
        <span style="margin: 0 0.4rem;">/</span>
        <a href="${pageContext.request.contextPath}/orders" style="color: inherit;">Orders</a>
        <span style="margin: 0 0.4rem;">/</span>
        <span style="color: var(--color-primary); font-weight: 500;">#ORD-${order.id}</span>
    </nav>

    <c:if test="${param.success == 'true'}">
        <div class="alert alert-success" role="alert" style="margin-bottom: 1.5rem;">
            ✓ Congratulations! Your order <strong>#ORD-${order.id}</strong> has been successfully placed and authorized.
        </div>
    </c:if>

    <c:if test="${param.cancelled == 'true'}">
        <div class="alert alert-warning" role="alert" style="margin-bottom: 1.5rem;">
            This order has been cancelled and product inventory has been safely restored.
        </div>
    </c:if>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 style="font-size: 2.2rem; margin: 0 0 0.35rem;">Order #ORD-${order.id}</h1>
            <span style="color: var(--color-text-muted); font-size: 0.95rem;">
                Placed on <fmt:formatDate value="${order.createdAt}" pattern="MMMM dd, yyyy 'at' hh:mm a"/>
            </span>
        </div>

        <div>
            <c:choose>
                <c:when test="${order.status == 'PENDING'}">
                    <span class="order-status-badge status-pending" style="font-size: 0.95rem; padding: 0.5rem 1.25rem;">
                        Status: Pending
                    </span>
                </c:when>
                <c:when test="${order.status == 'CONFIRMED'}">
                    <span class="order-status-badge status-confirmed" style="font-size: 0.95rem; padding: 0.5rem 1.25rem;">
                        Status: Confirmed
                    </span>
                </c:when>
                <c:when test="${order.status == 'SHIPPED'}">
                    <span class="order-status-badge status-shipped" style="font-size: 0.95rem; padding: 0.5rem 1.25rem;">
                        Status: Dispatched / In Transit
                    </span>
                </c:when>
                <c:when test="${order.status == 'DELIVERED'}">
                    <span class="order-status-badge status-delivered" style="font-size: 0.95rem; padding: 0.5rem 1.25rem;">
                        Status: Delivered
                    </span>
                </c:when>
                <c:when test="${order.status == 'CANCELLED'}">
                    <span class="order-status-badge status-cancelled" style="font-size: 0.95rem; padding: 0.5rem 1.25rem;">
                        Status: Cancelled
                    </span>
                </c:when>
            </c:choose>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2.5rem; align-items: flex-start;">
        <!-- Left Column: Items Purchased -->
        <div>
            <div class="checkout-section-card">
                <h3>Ordered Merchandise (${fn:length(order.items)} items)</h3>

                <div class="order-items-table-wrap">
                    <c:forEach items="${order.items}" var="item">
                        <div class="order-line-item" style="padding: 1.25rem 0;">
                            <img src="${item.productImageUrl != null && not empty item.productImageUrl ? item.productImageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=150&auto=format&fit=crop'}"
                                 alt="<c:out value='${item.productName}'/>"
                                 class="order-line-thumb"
                                 style="width: 80px; height: 80px;">
                            <div style="flex: 1; min-width: 0;">
                                <a href="${pageContext.request.contextPath}/products/${item.productId}"
                                   style="color: var(--color-primary); font-weight: 600; font-size: 1.05rem; text-decoration: none;">
                                    <c:out value="${item.productName}"/>
                                </a>
                                <div style="font-size: 0.9rem; color: var(--color-text-muted); margin-top: 0.35rem;">
                                    Unit Price: ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00"/>
                                </div>
                                <div style="font-size: 0.9rem; color: var(--color-text-muted);">
                                    Quantity: ${item.quantity}
                                </div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 700; font-size: 1.15rem; color: var(--color-primary);">
                                    ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00"/>
                                </div>
                                <c:if test="${order.status == 'DELIVERED'}">
                                    <a href="${pageContext.request.contextPath}/products/${item.productId}#reviewsSection"
                                       class="btn btn-outline"
                                       style="margin-top: 0.5rem; padding: 0.35rem 0.75rem; font-size: 0.8rem;">
                                        Write Review
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- Order Cancellation Action (if eligible) -->
            <c:if test="${order.status == 'PENDING' || order.status == 'CONFIRMED'}">
                <div class="checkout-section-card" style="border-left: 4px solid var(--color-warning);">
                    <h3 style="color: #92400e; margin-bottom: 0.5rem;">Order Management</h3>
                    <p style="font-size: 0.9rem; color: var(--color-text-muted); margin-bottom: 1.25rem;">
                        This order is currently being processed. If you need to cancel this order, you may do so before it is dispatched to courier logistics.
                    </p>

                    <form id="cancelOrderForm" action="${pageContext.request.contextPath}/orders/${order.id}/cancel" method="POST"
                          onsubmit="return confirm('Are you sure you wish to cancel this order? This action will restore product inventory and cannot be undone.');">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <button type="submit" class="btn btn-outline" style="color: var(--color-danger); border-color: var(--color-danger);">
                            Cancel Order & Restore Inventory
                        </button>
                    </form>
                </div>
            </c:if>
        </div>

        <!-- Right Column: Shipping Details & Financial Breakdown -->
        <div>
            <!-- Destination Card -->
            <div class="checkout-section-card">
                <h3>Delivery Address</h3>
                <p style="margin: 0; line-height: 1.6; color: var(--color-text-main); font-size: 0.95rem;">
                    <strong><c:out value="${sessionScope.user.name}"/></strong><br>
                    <c:out value="${order.shippingAddress}"/><br>
                    India
                </p>
            </div>

            <!-- Summary Breakdown -->
            <div class="order-summary-card" style="position: static;">
                <h3 style="font-size: 1.25rem; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--color-border-light);">
                    Payment Summary
                </h3>

                <div class="summary-line">
                    <span>Subtotal</span>
                    <span>₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00"/></span>
                </div>
                <div class="summary-line">
                    <span>Shipping & Handling</span>
                    <span style="color: var(--color-success); font-weight: 600;">Complimentary</span>
                </div>
                <div class="summary-line">
                    <span>Taxes & Cess</span>
                    <span>Included</span>
                </div>

                <div class="summary-line total">
                    <span>Total Paid</span>
                    <span>₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00"/></span>
                </div>

                <div style="margin-top: 1.5rem; padding-top: 1.25rem; border-top: 1px solid var(--color-border-light); font-size: 0.85rem; color: var(--color-text-muted);">
                    Payment Status: <strong>Authorized via Mock Gateway</strong>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
