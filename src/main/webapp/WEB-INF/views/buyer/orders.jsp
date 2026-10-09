<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="My Orders — DJ Mart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 5rem;">
    <!-- Breadcrumbs -->
    <nav aria-label="Breadcrumb" style="margin-bottom: 1.5rem; font-size: 0.9rem; color: var(--color-text-muted);">
        <a href="${pageContext.request.contextPath}/" style="color: inherit;">Home</a>
        <span style="margin: 0 0.4rem;">/</span>
        <span style="color: var(--color-primary); font-weight: 500;">My Orders</span>
    </nav>

    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <h1 style="font-size: 2.2rem; margin: 0;">Order History</h1>
        <span style="color: var(--color-text-muted); font-size: 0.95rem;">
            Showing ${ordersPage.totalElements} total orders placed
        </span>
    </div>

    <c:choose>
        <c:when test="${not empty ordersPage.content}">
            <div class="orders-list">
                <c:forEach items="${ordersPage.content}" var="order">
                    <div class="order-card">
                        <div class="order-header-row">
                            <div>
                                <span style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--color-text-muted); display: block;">
                                    Order Number
                                </span>
                                <strong style="font-size: 1.15rem; color: var(--color-primary);">
                                    #ORD-${order.id}
                                </strong>
                            </div>

                            <div>
                                <span style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--color-text-muted); display: block;">
                                    Placed On
                                </span>
                                <span style="font-size: 0.95rem; color: var(--color-text-main);">
                                    <fmt:formatDate value="${order.createdAt}" pattern="dd MMM yyyy, hh:mm a"/>
                                </span>
                            </div>

                            <div>
                                <span style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--color-text-muted); display: block;">
                                    Total Value
                                </span>
                                <strong style="font-size: 1.15rem; color: var(--color-primary);">
                                    ₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00"/>
                                </strong>
                            </div>

                            <div>
                                <c:choose>
                                    <c:when test="${order.status == 'PENDING'}">
                                        <span class="order-status-badge status-pending">Pending</span>
                                    </c:when>
                                    <c:when test="${order.status == 'CONFIRMED'}">
                                        <span class="order-status-badge status-confirmed">Confirmed</span>
                                    </c:when>
                                    <c:when test="${order.status == 'SHIPPED'}">
                                        <span class="order-status-badge status-shipped">Dispatched</span>
                                    </c:when>
                                    <c:when test="${order.status == 'DELIVERED'}">
                                        <span class="order-status-badge status-delivered">Delivered</span>
                                    </c:when>
                                    <c:when test="${order.status == 'CANCELLED'}">
                                        <span class="order-status-badge status-cancelled">Cancelled</span>
                                    </c:when>
                                </c:choose>
                            </div>

                            <div>
                                <a href="${pageContext.request.contextPath}/orders/${order.id}" class="btn btn-outline" style="padding: 0.5rem 1rem; font-size: 0.85rem;">
                                    View Details &rarr;
                                </a>
                            </div>
                        </div>

                        <!-- Order items snapshot -->
                        <div style="margin-top: 1rem;">
                            <c:forEach items="${order.items}" var="item">
                                    <c:set var="orderItemThumb" value="${item.productImageUrl != null && not empty item.productImageUrl ? item.productImageUrl : '/static/images/placeholder.svg'}"/>
                                    <img src="<c:url value='${orderItemThumb}'/>"
                                         alt="<c:out value='${item.productName}'/>"
                                         class="order-line-thumb"
                                         onerror="if(this.src!=='<c:url value="/static/images/placeholder.svg"/>'){this.onerror=null;this.src='<c:url value="/static/images/placeholder.svg"/>';}">
                                    <div style="flex: 1; min-width: 0;">
                                        <a href="${pageContext.request.contextPath}/products/${item.productId}"
                                           style="color: var(--color-primary); font-weight: 600; text-decoration: none; font-size: 0.95rem;">
                                            <c:out value="${item.productName}"/>
                                        </a>
                                        <div style="font-size: 0.85rem; color: var(--color-text-muted); margin-top: 0.2rem;">
                                            Qty: ${item.quantity} &times; ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00"/>
                                        </div>
                                    </div>
                                    <div style="font-weight: 600; font-size: 0.95rem; color: var(--color-primary);">
                                        ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00"/>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Pagination -->
            <c:if test="${ordersPage.totalPages > 1}">
                <div class="pagination-bar" style="margin-top: 2.5rem; display: flex; justify-content: center; gap: 0.5rem;">
                    <c:forEach begin="1" end="${ordersPage.totalPages}" var="p">
                        <a href="${pageContext.request.contextPath}/orders?page=${p}"
                           class="page-link ${p == ordersPage.pageNumber ? 'active' : ''}"
                           style="padding: 0.5rem 1rem; border: 1px solid var(--color-border); border-radius: var(--radius-sm); text-decoration: none; color: inherit; ${p == ordersPage.pageNumber ? 'background: var(--color-primary); color: #fff;' : ''}">
                            ${p}
                        </a>
                    </c:forEach>
                </div>
            </c:if>
        </c:when>

        <c:otherwise>
            <!-- Empty State -->
            <div class="empty-state">
                <div class="empty-state-icon">🛍️</div>
                <h3>No Orders Placed Yet</h3>
                <p>When you purchase items through DJ Mart, their shipment tracking, receipts, and order histories will appear here.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
                    Explore Curated Catalog
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
