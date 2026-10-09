<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Marketplace Analytics & Performance — DJ Mart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 5rem;">
    <!-- Analytics Header -->
    <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid var(--color-border); padding-bottom: 1.5rem;">
        <div>
            <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.35rem;">
                Platform Intelligence
            </span>
            <h1 style="font-size: 2.2rem; margin: 0 0 0.35rem; color: var(--color-primary);">Marketplace Analytics &amp; Revenue</h1>
            <p style="color: var(--color-text-muted); font-size: 0.95rem; margin: 0;">
                Real-time operational telemetry, verified gross revenue, order status distribution, and inventory velocity.
            </p>
        </div>
        <div style="display: flex; gap: 0.75rem; align-items: center;">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline" style="font-size: 0.9rem;">
                &larr; Admin Dashboard
            </a>
            <button onclick="window.location.reload();" class="btn btn-primary" style="font-size: 0.9rem;">
                ↻ Refresh Telemetry
            </button>
        </div>
    </div>

    <!-- KPI Overview Cards -->
    <div class="dashboard-grid" style="margin-bottom: 2.5rem;">
        <!-- Card 1: Verified Revenue -->
        <div class="stat-card" style="border-left: 4px solid var(--color-success);">
            <span class="stat-label">Verified Revenue (Valid Orders)</span>
            <span class="stat-value" style="color: var(--color-success);">
                ₹<fmt:formatNumber value="${salesSummary.totalRevenue != null ? salesSummary.totalRevenue : 0}" pattern="#,##0.00"/>
            </span>
            <span style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.35rem; display: block;">
                Excludes cancelled orders
            </span>
        </div>

        <!-- Card 2: Completed Orders -->
        <div class="stat-card" style="border-left: 4px solid var(--color-accent);">
            <span class="stat-label">Completed / Valid Orders</span>
            <span class="stat-value" style="color: var(--color-accent);">
                ${salesSummary.completedOrders != null ? salesSummary.completedOrders : 0}
            </span>
            <span style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.35rem; display: block;">
                Delivered, Shipped & Confirmed
            </span>
        </div>

        <!-- Card 3: Total Orders -->
        <div class="stat-card" style="border-left: 4px solid var(--color-primary);">
            <span class="stat-label">Total Platform Orders</span>
            <span class="stat-value">
                ${stats.totalOrders != null ? stats.totalOrders : 0}
            </span>
            <span style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.35rem; display: block;">
                All lifecycle states
            </span>
        </div>

        <!-- Card 4: Average Order Value -->
        <div class="stat-card" style="border-left: 4px solid #8b5cf6;">
            <span class="stat-label">Average Order Value (AOV)</span>
            <span class="stat-value" style="color: #8b5cf6;">
                <c:choose>
                    <c:when test="${salesSummary.completedOrders != null && salesSummary.completedOrders > 0}">
                        ₹<fmt:formatNumber value="${salesSummary.totalRevenue / salesSummary.completedOrders}" pattern="#,##0.00"/>
                    </c:when>
                    <c:otherwise>₹0.00</c:otherwise>
                </c:choose>
            </span>
            <span style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.35rem; display: block;">
                Revenue per valid order
            </span>
        </div>

        <!-- Card 5: Catalog Products -->
        <div class="stat-card" style="border-left: 4px solid #06b6d4;">
            <span class="stat-label">Active Catalog Products</span>
            <span class="stat-value" style="color: #06b6d4;">
                ${stats.totalProducts != null ? stats.totalProducts : 0}
            </span>
            <span style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.35rem; display: block;">
                Across 5 product categories
            </span>
        </div>

        <!-- Card 6: Low-Stock Alerts -->
        <div class="stat-card" style="border-left: 4px solid var(--color-warning);">
            <span class="stat-label">Inventory Attention Required</span>
            <span class="stat-value" style="color: var(--color-warning);">
                ${fn:length(lowStock)}
            </span>
            <span style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.35rem; display: block;">
                Items with stock &le; 15 units
            </span>
        </div>
    </div>

    <!-- Order Workflow Status Breakdown -->
    <div class="checkout-section-card" style="margin-bottom: 2.5rem;">
        <h3 style="margin-bottom: 1.25rem; font-size: 1.25rem;">Order Lifecycle Distribution</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 1rem;">
            <div style="background: var(--color-surface-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--color-border); text-align: center;">
                <span style="font-size: 0.85rem; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase;">Pending</span>
                <div style="font-size: 1.75rem; font-weight: 700; color: #d97706; margin-top: 0.35rem;">
                    ${salesSummary.pendingOrders != null ? salesSummary.pendingOrders : 0}
                </div>
            </div>
            <div style="background: var(--color-surface-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--color-border); text-align: center;">
                <span style="font-size: 0.85rem; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase;">Confirmed</span>
                <div style="font-size: 1.75rem; font-weight: 700; color: #2563eb; margin-top: 0.35rem;">
                    ${salesSummary.confirmedOrders != null ? salesSummary.confirmedOrders : 0}
                </div>
            </div>
            <div style="background: var(--color-surface-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--color-border); text-align: center;">
                <span style="font-size: 0.85rem; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase;">Dispatched / Shipped</span>
                <div style="font-size: 1.75rem; font-weight: 700; color: #7c3aed; margin-top: 0.35rem;">
                    ${salesSummary.shippedOrders != null ? salesSummary.shippedOrders : 0}
                </div>
            </div>
            <div style="background: var(--color-surface-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--color-border); text-align: center;">
                <span style="font-size: 0.85rem; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase;">Delivered</span>
                <div style="font-size: 1.75rem; font-weight: 700; color: var(--color-success); margin-top: 0.35rem;">
                    ${salesSummary.deliveredOrders != null ? salesSummary.deliveredOrders : 0}
                </div>
            </div>
            <div style="background: var(--color-surface-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--color-border); text-align: center;">
                <span style="font-size: 0.85rem; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase;">Cancelled</span>
                <div style="font-size: 1.75rem; font-weight: 700; color: var(--color-danger); margin-top: 0.35rem;">
                    ${salesSummary.cancelledOrders != null ? salesSummary.cancelledOrders : 0}
                </div>
            </div>
        </div>
    </div>

    <!-- Daily Sales Velocity (Chart & Table) -->
    <div class="checkout-section-card" style="margin-bottom: 2.5rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.5rem;">
            <div>
                <h3 style="margin: 0; font-size: 1.25rem;">Daily Sales Velocity (Past 14 Days)</h3>
                <span style="font-size: 0.85rem; color: var(--color-text-muted);">Authoritative settled transaction volume grouped by date</span>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty dailySales}">
                <!-- Responsive CSS/SVG Bar Chart -->
                <div style="background: var(--color-surface-subtle); border-radius: var(--radius-md); padding: 1.5rem 1rem; margin-bottom: 1.5rem; border: 1px solid var(--color-border);">
                    <div style="display: flex; align-items: flex-end; justify-content: space-between; gap: 0.5rem; height: 180px; padding-top: 1rem;">
                        <c:forEach items="${dailySales}" var="day">
                            <div style="flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end;">
                                <span style="font-size: 0.725rem; font-weight: 600; color: var(--color-primary); margin-bottom: 0.25rem;">
                                    ₹<fmt:formatNumber value="${day.revenue}" pattern="#,##0"/>
                                </span>
                                <div style="width: 100%; max-width: 38px; background: linear-gradient(180deg, var(--color-accent) 0%, #1e40af 100%); border-radius: 4px 4px 0 0; min-height: 8px; height: ${day.revenue > 0 ? (day.revenue > 100000 ? 120 : (day.revenue / 1000) * 1.5 + 12) : 8}px; transition: height 0.3s ease;"
                                     title="Date: ${day.sale_date} | Revenue: ₹${day.revenue} | Orders: ${day.order_count}">
                                </div>
                                <span style="font-size: 0.7rem; color: var(--color-text-muted); margin-top: 0.4rem; white-space: nowrap;">
                                    ${day.sale_date}
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Settlement Date</th>
                                <th style="text-align: center;">Orders Placed</th>
                                <th style="text-align: right;">Day Gross Revenue</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${dailySales}" var="day">
                                <tr>
                                    <td><strong>${day.sale_date}</strong></td>
                                    <td style="text-align: center;">${day.order_count}</td>
                                    <td style="text-align: right;"><strong>₹<fmt:formatNumber value="${day.revenue}" pattern="#,##0.00"/></strong></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state" style="padding: 2.5rem 1.5rem;">
                    <div class="empty-state-icon">📊</div>
                    <h3>No Settled Orders in the Selected Window</h3>
                    <p>When customers complete purchases, daily sales velocity will automatically visualize here.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Two-Column Grid: Best Sellers & Low Stock Alerts -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(480px, 1fr)); gap: 2rem; margin-bottom: 2.5rem;">
        <!-- Column 1: Best-Selling Products -->
        <div class="checkout-section-card">
            <h3 style="margin-bottom: 1.25rem; font-size: 1.25rem;">Top Performing Products</h3>
            <c:choose>
                <c:when test="${not empty bestSellers}">
                    <div class="data-table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Rank &amp; Product</th>
                                    <th style="text-align: center;">Units Sold</th>
                                    <th style="text-align: right;">Total Generated</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${bestSellers}" var="bs" varStatus="loop">
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                                <span style="display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 50%; background: ${loop.index == 0 ? '#fbbf24' : (loop.index == 1 ? '#94a3b8' : (loop.index == 2 ? '#b45309' : '#e2e8f0'))}; color: ${loop.index < 3 ? '#000' : '#475569'}; font-size: 0.75rem; font-weight: 700;">
                                                    ${loop.index + 1}
                                                </span>
                                                <a href="${pageContext.request.contextPath}/products/${bs.product_id}" style="color: var(--color-primary); font-weight: 600;">
                                                    <c:out value="${bs.product_name}"/>
                                                </a>
                                            </div>
                                        </td>
                                        <td style="text-align: center;"><strong>${bs.total_units}</strong></td>
                                        <td style="text-align: right;">₹<fmt:formatNumber value="${bs.total_sales}" pattern="#,##0.00"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state" style="padding: 2rem;">
                        <p style="margin: 0; color: var(--color-text-muted);">No sales data recorded yet to compute rankings.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Column 2: Low-Stock Inventory Watch -->
        <div class="checkout-section-card">
            <h3 style="margin-bottom: 1.25rem; font-size: 1.25rem;">Inventory Attention List (&le; 15 units)</h3>
            <c:choose>
                <c:when test="${not empty lowStock}">
                    <div class="data-table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Category</th>
                                    <th style="text-align: center;">Remaining</th>
                                    <th style="text-align: right;">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${lowStock}" var="ls">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/products/${ls.id}" style="color: var(--color-primary); font-weight: 600;">
                                                <c:out value="${ls.name}"/>
                                            </a>
                                            <div style="font-size: 0.75rem; color: var(--color-text-muted);">SKU: ${ls.sku}</div>
                                        </td>
                                        <td><span class="badge" style="background: var(--color-surface-subtle); color: var(--color-text-main);"><c:out value="${ls.category}"/></span></td>
                                        <td style="text-align: center;">
                                            <strong style="color: ${ls.stockQty <= 5 ? 'var(--color-danger)' : 'var(--color-warning)'};">
                                                ${ls.stockQty}
                                            </strong>
                                        </td>
                                        <td style="text-align: right;">
                                            <c:choose>
                                                <c:when test="${ls.stockQty <= 0}">
                                                    <span class="badge badge-out-of-stock">Depleted</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-low-stock">Critical</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state" style="padding: 2rem;">
                        <p style="margin: 0; color: var(--color-success); font-weight: 600;">All products have healthy inventory levels (&gt; 15 units).</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Active Categories Summary -->
    <div class="checkout-section-card">
        <h3 style="margin-bottom: 1rem; font-size: 1.25rem;">Marketplace Category Distribution</h3>
        <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
            <c:forEach items="${categories}" var="cName">
                <a href="${pageContext.request.contextPath}/products?category=${fn:escapeXml(cName)}"
                   class="badge badge-accent"
                   style="font-size: 0.9rem; padding: 0.5rem 1rem; text-decoration: none;">
                    📁 <c:out value="${cName}"/>
                </a>
            </c:forEach>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
