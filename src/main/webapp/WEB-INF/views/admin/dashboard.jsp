<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Platform Administration — DJ Mart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 5rem;">
    <!-- Action Alerts -->
    <c:if test="${param.statusUpdated == 'true'}">
        <div class="alert alert-success" style="margin-bottom: 1.5rem;">
            ✓ Order lifecycle status was updated successfully.
        </div>
    </c:if>
    <c:if test="${param.productUpdated == 'true'}">
        <div class="alert alert-success" style="margin-bottom: 1.5rem;">
            ✓ Product price, stock, or imagery was updated successfully.
        </div>
    </c:if>
    <c:if test="${param.categoryCreated == 'true'}">
        <div class="alert alert-success" style="margin-bottom: 1.5rem;">
            ✓ New marketplace category created successfully.
        </div>
    </c:if>

    <!-- Admin Header & Actions -->
    <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid var(--color-border); padding-bottom: 1.5rem;">
        <div>
            <h1 style="font-size: 2.2rem; margin: 0 0 0.35rem; color: var(--color-primary);">Marketplace Administration &amp; Governance</h1>
            <span style="color: var(--color-text-muted); font-size: 0.95rem;">
                Superintendent: <strong><c:out value="${sessionScope.user.name}"/></strong> (<c:out value="${sessionScope.user.email}"/>) &bull; Role: <span class="order-status-badge status-confirmed">ADMIN</span>
            </span>
        </div>
        <div style="display: flex; gap: 0.75rem; align-items: center;">
            <a href="${pageContext.request.contextPath}/admin/analytics" class="btn btn-primary" style="font-size: 0.9rem;">
                📊 View Analytics &amp; Revenue &rarr;
            </a>
        </div>
    </div>

    <!-- Platform KPI Cards -->
    <div class="dashboard-grid" style="margin-bottom: 3rem;">
        <div class="stat-card">
            <span class="stat-label">Total Users</span>
            <span class="stat-value">${stats.totalUsers}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Registered Buyers</span>
            <span class="stat-value">${stats.totalBuyers}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Active Sellers</span>
            <span class="stat-value">${stats.totalSellers}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Catalog Products</span>
            <span class="stat-value">${stats.totalProducts}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Platform Orders</span>
            <span class="stat-value">${stats.totalOrders}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Gross Merchandise Value (GMV)</span>
            <span class="stat-value" style="color: var(--color-success);">₹<fmt:formatNumber value="${stats.totalRevenue}" pattern="#,##0"/></span>
        </div>
    </div>

    <!-- Two-Column Operational Row: Low Stock Alerts & Best Sellers -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(480px, 1fr)); gap: 2rem; margin-bottom: 3rem;">
        <!-- Low Stock Alerts -->
        <div class="checkout-section-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
                <h3 style="margin: 0;">⚠️ Low-Stock Alerts (&le; 20 units)</h3>
                <span class="badge badge-low-stock">${fn:length(lowStockProducts)} items</span>
            </div>
            <c:choose>
                <c:when test="${not empty lowStockProducts}">
                    <div class="data-table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>SKU</th>
                                    <th style="text-align: center;">Stock</th>
                                    <th style="text-align: right;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${lowStockProducts}" var="lsp">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/products/${lsp.id}" style="color: var(--color-primary); font-weight: 600;">
                                                <c:out value="${lsp.name}"/>
                                            </a>
                                        </td>
                                        <td><code>${lsp.sku}</code></td>
                                        <td style="text-align: center;">
                                            <strong style="color: ${lsp.stockQty <= 5 ? 'var(--color-danger)' : 'var(--color-warning)'};">
                                                ${lsp.stockQty}
                                            </strong>
                                        </td>
                                        <td style="text-align: right;">
                                            <form action="${pageContext.request.contextPath}/admin/product/update" method="POST" style="display: inline-flex; gap: 0.35rem; align-items: center;">
                                                <input type="hidden" name="productId" value="${lsp.id}">
                                                <input type="number" name="stock" value="${lsp.stockQty + 25}" min="0" style="width: 65px; padding: 0.25rem 0.4rem; font-size: 0.8rem; border: 1px solid var(--color-border); border-radius: 4px;">
                                                <button type="submit" class="btn btn-outline" style="padding: 0.25rem 0.6rem; font-size: 0.75rem;">+ Restock</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state" style="padding: 2rem;">
                        <p style="margin: 0; color: var(--color-success); font-weight: 600;">All products have healthy inventory levels.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Best-Selling Products -->
        <div class="checkout-section-card">
            <h3 style="margin-bottom: 1.25rem;">🏆 Best-Selling Products</h3>
            <c:choose>
                <c:when test="${not empty bestSellers}">
                    <div class="data-table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Rank &amp; Name</th>
                                    <th style="text-align: center;">Units Sold</th>
                                    <th style="text-align: right;">Revenue</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${bestSellers}" var="bs" varStatus="loop">
                                    <tr>
                                        <td>
                                            <span style="font-weight: 700; color: var(--color-accent); margin-right: 0.4rem;">#${loop.index + 1}</span>
                                            <a href="${pageContext.request.contextPath}/products/${bs.product_id}" style="color: var(--color-primary); font-weight: 600;">
                                                <c:out value="${bs.product_name}"/>
                                            </a>
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
                        <p style="margin: 0; color: var(--color-text-muted);">No sales data available yet.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Product Catalog Controls (Price & Stock Management) -->
    <div class="checkout-section-card" style="margin-bottom: 3rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
            <h3 style="margin: 0;">Product Catalog Management (${fn:length(allProducts)} Displayed)</h3>
            <span style="font-size: 0.85rem; color: var(--color-text-muted);">Direct price and inventory modification</span>
        </div>

        <c:choose>
            <c:when test="${not empty allProducts}">
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Product Details</th>
                                <th>Category</th>
                                <th>SKU</th>
                                <th>Current Price (₹)</th>
                                <th>Stock</th>
                                <th style="text-align: right;">Update Price &amp; Stock</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${allProducts}" var="prod">
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                                            <img src="${prod.imageUrl}" alt="" style="width: 36px; height: 36px; object-fit: cover; border-radius: 4px; border: 1px solid var(--color-border);">
                                            <div>
                                                <a href="${pageContext.request.contextPath}/products/${prod.id}" style="font-weight: 600; color: var(--color-primary);">
                                                    <c:out value="${prod.name}"/>
                                                </a>
                                                <div style="font-size: 0.75rem; color: var(--color-text-muted);"><c:out value="${prod.brand}"/></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge" style="background: var(--color-surface-subtle); color: var(--color-text-main);"><c:out value="${prod.category}"/></span></td>
                                    <td><code>${prod.sku}</code></td>
                                    <td><strong>₹<fmt:formatNumber value="${prod.price}" pattern="#,##0.00"/></strong></td>
                                    <td>
                                        <span class="badge ${prod.stockQty <= 5 ? 'badge-low-stock' : 'badge-in-stock'}">
                                            ${prod.stockQty}
                                        </span>
                                    </td>
                                    <td style="text-align: right;">
                                        <form action="${pageContext.request.contextPath}/admin/product/update" method="POST" style="display: inline-flex; gap: 0.4rem; align-items: center; justify-content: flex-end;">
                                            <input type="hidden" name="productId" value="${prod.id}">
                                            <input type="number" name="price" value="${prod.price}" step="0.01" min="1" placeholder="Price" style="width: 90px; padding: 0.3rem 0.5rem; font-size: 0.8rem; border: 1px solid var(--color-border); border-radius: 4px;" title="New Price in INR">
                                            <input type="number" name="stock" value="${prod.stockQty}" min="0" placeholder="Stock" style="width: 65px; padding: 0.3rem 0.5rem; font-size: 0.8rem; border: 1px solid var(--color-border); border-radius: 4px;" title="Stock Quantity">
                                            <button type="submit" class="btn btn-outline" style="padding: 0.3rem 0.7rem; font-size: 0.8rem;">Save</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <p>No products available in the catalog.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Category Creation Card -->
    <div class="checkout-section-card" style="margin-bottom: 3rem;">
        <h3 style="margin-bottom: 1.25rem;">Add New Marketplace Category</h3>
        <form action="${pageContext.request.contextPath}/admin/category/create" method="POST" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; align-items: flex-end;">
            <div class="form-group" style="margin-bottom: 0;">
                <label class="form-label">Category Name *</label>
                <input type="text" name="name" class="form-control" placeholder="e.g. Gourmet Coffee" required>
            </div>
            <div class="form-group" style="margin-bottom: 0;">
                <label class="form-label">URL Slug (Optional)</label>
                <input type="text" name="slug" class="form-control" placeholder="e.g. gourmet-coffee">
            </div>
            <div class="form-group" style="margin-bottom: 0;">
                <label class="form-label">Description</label>
                <input type="text" name="description" class="form-control" placeholder="Brief category description">
            </div>
            <div>
                <button type="submit" class="btn btn-primary" style="padding: 0.7rem 1.5rem; width: 100%;">Create Category</button>
            </div>
        </form>
    </div>

    <!-- Sitewide Recent Orders Table with Inline Status Transitions -->
    <div class="checkout-section-card" style="margin-bottom: 3rem;">
        <h3 style="margin-bottom: 1.25rem;">Marketplace Orders Oversight &amp; Lifecycle Controls</h3>

        <c:choose>
            <c:when test="${not empty recentOrders}">
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Order</th>
                                <th>Buyer ID</th>
                                <th>Order Date</th>
                                <th>Total GMV</th>
                                <th>Current Status</th>
                                <th style="text-align: right;">Update Lifecycle Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${recentOrders}" var="ord">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/orders/${ord.id}"
                                           style="color: var(--color-primary); font-weight: 600; text-decoration: none;">
                                            #ORD-${ord.id}
                                        </a>
                                    </td>
                                    <td>User #${ord.buyerId}</td>
                                    <td><fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></td>
                                    <td><strong>₹<fmt:formatNumber value="${ord.totalAmount}" pattern="#,##0.00"/></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${ord.status == 'PENDING'}"><span class="order-status-badge status-pending">Pending</span></c:when>
                                            <c:when test="${ord.status == 'CONFIRMED'}"><span class="order-status-badge status-confirmed">Confirmed</span></c:when>
                                            <c:when test="${ord.status == 'SHIPPED'}"><span class="order-status-badge status-shipped">Dispatched</span></c:when>
                                            <c:when test="${ord.status == 'DELIVERED'}"><span class="order-status-badge status-delivered">Delivered</span></c:when>
                                            <c:when test="${ord.status == 'CANCELLED'}"><span class="order-status-badge status-cancelled">Cancelled</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <form action="${pageContext.request.contextPath}/admin/order/status" method="POST" style="display: inline-flex; gap: 0.4rem; align-items: center; justify-content: flex-end;">
                                            <input type="hidden" name="orderId" value="${ord.id}">
                                            <select name="status" style="padding: 0.3rem 0.5rem; font-size: 0.8rem; border: 1px solid var(--color-border); border-radius: 4px;">
                                                <option value="PENDING" ${ord.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                                                <option value="CONFIRMED" ${ord.status == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                                                <option value="SHIPPED" ${ord.status == 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                                                <option value="DELIVERED" ${ord.status == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                                                <option value="CANCELLED" ${ord.status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                            </select>
                                            <button type="submit" class="btn btn-outline" style="padding: 0.3rem 0.7rem; font-size: 0.8rem;">Change</button>
                                            <a href="${pageContext.request.contextPath}/orders/${ord.id}" class="btn btn-outline" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">
                                                View &rarr;
                                            </a>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state" style="padding: 2rem;">
                    <p style="margin: 0; color: var(--color-text-muted);">No orders recorded across the platform.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- User Management Table -->
    <div class="checkout-section-card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
            <h3 style="margin: 0; border: none; padding: 0;">Registered Marketplace Users (${usersPage.totalElements} Total)</h3>
        </div>

        <c:choose>
            <c:when test="${not empty usersPage.content}">
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Full Name</th>
                                <th>Email Address</th>
                                <th>Assigned Role</th>
                                <th>Registration Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${usersPage.content}" var="u">
                                <tr>
                                    <td><strong>#USR-${u.id}</strong></td>
                                    <td><c:out value="${u.name}"/></td>
                                    <td><c:out value="${u.email}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.role == 'ADMIN'}">
                                                <span class="order-status-badge status-cancelled">ADMIN</span>
                                            </c:when>
                                            <c:when test="${u.role == 'SELLER'}">
                                                <span class="order-status-badge status-confirmed">SELLER</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="order-status-badge status-delivered">BUYER</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <c:if test="${usersPage.totalPages > 1}">
                    <div style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 1.5rem;">
                        <c:forEach begin="1" end="${usersPage.totalPages}" var="pIdx">
                            <a href="${pageContext.request.contextPath}/admin/dashboard?page=${pIdx}"
                               class="btn btn-outline ${pIdx == usersPage.pageNumber ? 'active' : ''}"
                               style="padding: 0.4rem 0.85rem; ${pIdx == usersPage.pageNumber ? 'background: var(--color-primary); color: #fff;' : ''}">
                                ${pIdx}
                            </a>
                        </c:forEach>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <p>No registered users found in the marketplace.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
