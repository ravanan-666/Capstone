<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Seller Studio & Atelier — DJ Mart" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="padding-top: 2rem; padding-bottom: 5rem;">
    <!-- Dashboard Header -->
    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 style="font-size: 2.2rem; margin: 0 0 0.35rem;">Seller Studio &amp; Fulfillment</h1>
            <span style="color: var(--color-text-muted); font-size: 0.95rem;">
                Merchant Atelier: <c:out value="${sessionScope.user.name}"/> (${sessionScope.user.email})
            </span>
        </div>

        <div>
            <button type="button" id="openAddProductModalBtn" class="btn btn-primary">
                + Add New Product Listing
            </button>
        </div>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${param.created == 'true'}">
        <div class="alert alert-success" role="alert" style="margin-bottom: 1.5rem;">
            ✓ Product listing created successfully and published to the marketplace.
        </div>
    </c:if>
    <c:if test="${param.updated == 'true'}">
        <div class="alert alert-success" role="alert" style="margin-bottom: 1.5rem;">
            ✓ Product details updated successfully.
        </div>
    </c:if>
    <c:if test="${param.deleted == 'true'}">
        <div class="alert alert-warning" role="alert" style="margin-bottom: 1.5rem;">
            Product listing was removed from the marketplace.
        </div>
    </c:if>

    <!-- Statistics KPI Cards -->
    <div class="dashboard-grid">
        <div class="stat-card">
            <span class="stat-label">Active Listings</span>
            <span class="stat-value">${stats.totalProducts}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Incoming Orders</span>
            <span class="stat-value">${stats.totalOrders}</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Gross Merchandise Sales</span>
            <span class="stat-value">₹<fmt:formatNumber value="${stats.revenue}" pattern="#,##0"/></span>
        </div>

        <div class="stat-card" style="${stats.lowStockCount > 0 ? 'border-left: 4px solid var(--color-warning);' : ''}">
            <span class="stat-label">Low Stock Alerts (&le; 5)</span>
            <span class="stat-value" style="${stats.lowStockCount > 0 ? 'color: #d97706;' : ''}">
                ${stats.lowStockCount}
            </span>
        </div>
    </div>

    <!-- Product Management Table -->
    <div class="checkout-section-card" style="margin-bottom: 3rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
            <h3 style="margin: 0; border: none; padding: 0;">Product Inventory (${products.totalElements} Total)</h3>
        </div>

        <c:choose>
            <c:when test="${not empty products.data}">
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th>Category</th>
                                <th>Unit Price</th>
                                <th>Stock Status</th>
                                <th>Rating</th>
                                <th style="text-align: right;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${products.data}" var="p">
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 0.85rem;">
                                            <c:set var="sellerProdImg" value="${p.imageUrl != null && not empty p.imageUrl ? p.imageUrl : '/static/images/placeholder.svg'}"/>
                                            <img src="<c:url value='${sellerProdImg}'/>"
                                                 alt="<c:out value='${p.name}'/>"
                                                 style="width: 50px; height: 50px; border-radius: var(--radius-sm); object-fit: cover; border: 1px solid var(--color-border-light);"
                                                 onerror="if(this.src!=='<c:url value="/static/images/placeholder.svg"/>'){this.onerror=null;this.src='<c:url value="/static/images/placeholder.svg"/>';}">
                                            <div>
                                                <strong style="color: var(--color-primary); font-size: 0.95rem; display: block;">
                                                    <c:out value="${p.name}"/>
                                                </strong>
                                                <small style="color: var(--color-text-muted);">ID: #${p.id}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td><c:out value="${p.category}"/></td>
                                    <td><strong>₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00"/></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.stockQty <= 0}">
                                                <span class="badge badge-out-of-stock">0 (Out of stock)</span>
                                            </c:when>
                                            <c:when test="${p.stockQty <= 5}">
                                                <span class="badge badge-low-stock">${p.stockQty} (Low stock)</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-in-stock">${p.stockQty} available</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span style="color: #f59e0b;">★</span>
                                        <fmt:formatNumber value="${p.averageRating}" maxFractionDigits="1"/>
                                        <small style="color: var(--color-text-muted);">(${p.reviewCount})</small>
                                    </td>
                                    <td style="text-align: right;">
                                        <button type="button" class="btn btn-outline edit-product-btn"
                                                style="padding: 0.35rem 0.75rem; font-size: 0.85rem; margin-right: 0.35rem;"
                                                data-id="${p.id}"
                                                data-name="<c:out value='${p.name}'/>"
                                                data-category="<c:out value='${p.category}'/>"
                                                data-price="${p.price}"
                                                data-stock="${p.stockQty}"
                                                data-description="<c:out value='${p.description}'/>"
                                                data-image="<c:out value='${p.imageUrl}'/>">
                                            Edit
                                        </button>

                                        <form action="${pageContext.request.contextPath}/seller/products/delete" method="POST" style="display: inline;"
                                              onsubmit="return confirm('Permanently delete product \'${fn:escapeXml(p.name)}\'?');">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <button type="submit" class="btn btn-outline"
                                                    style="padding: 0.35rem 0.75rem; font-size: 0.85rem; color: var(--color-danger); border-color: var(--color-danger);">
                                                Delete
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Product Pagination -->
                <c:if test="${products.totalPages > 1}">
                    <div style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 1.5rem;">
                        <c:forEach begin="1" end="${products.totalPages}" var="pageIdx">
                            <a href="${pageContext.request.contextPath}/seller/dashboard?page=${pageIdx}"
                               class="btn btn-outline ${pageIdx == products.pageNumber ? 'active' : ''}"
                               style="padding: 0.4rem 0.85rem; ${pageIdx == products.pageNumber ? 'background: var(--color-primary); color: #fff;' : ''}">
                                ${pageIdx}
                            </a>
                        </c:forEach>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <h3>No Product Listings Found</h3>
                    <p>Begin publishing your catalog to start receiving customer orders.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Recent Relevant Orders -->
    <div class="checkout-section-card">
        <h3 style="margin-bottom: 1.25rem;">Recent Incoming Orders</h3>

        <c:choose>
            <c:when test="${not empty recentOrders}">
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Order</th>
                                <th>Date</th>
                                <th>Total</th>
                                <th>Status</th>
                                <th>Update Workflow Status</th>
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
                                    <td><fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></td>
                                    <td>₹<fmt:formatNumber value="${ord.totalAmount}" pattern="#,##0.00"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${ord.status == 'PENDING'}"><span class="order-status-badge status-pending">Pending</span></c:when>
                                            <c:when test="${ord.status == 'CONFIRMED'}"><span class="order-status-badge status-confirmed">Confirmed</span></c:when>
                                            <c:when test="${ord.status == 'SHIPPED'}"><span class="order-status-badge status-shipped">Dispatched</span></c:when>
                                            <c:when test="${ord.status == 'DELIVERED'}"><span class="order-status-badge status-delivered">Delivered</span></c:when>
                                            <c:when test="${ord.status == 'CANCELLED'}"><span class="order-status-badge status-cancelled">Cancelled</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${ord.status != 'DELIVERED' && ord.status != 'CANCELLED'}">
                                            <form action="${pageContext.request.contextPath}/api/orders/${ord.id}/status" method="POST" style="display: flex; gap: 0.5rem; align-items: center;" class="order-status-update-form">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <select name="status" class="form-control" style="padding: 0.35rem 0.5rem; font-size: 0.85rem; width: auto;">
                                                    <c:if test="${ord.status == 'PENDING'}">
                                                        <option value="CONFIRMED">Advance to Confirmed</option>
                                                        <option value="CANCELLED">Cancel Order</option>
                                                    </c:if>
                                                    <c:if test="${ord.status == 'CONFIRMED'}">
                                                        <option value="SHIPPED">Advance to Shipped / Dispatched</option>
                                                        <option value="CANCELLED">Cancel Order</option>
                                                    </c:if>
                                                    <c:if test="${ord.status == 'SHIPPED'}">
                                                        <option value="DELIVERED">Mark as Delivered</option>
                                                    </c:if>
                                                </select>
                                                <button type="submit" class="btn btn-outline" style="padding: 0.35rem 0.75rem; font-size: 0.85rem;">
                                                    Update
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${ord.status == 'DELIVERED'}">
                                            <span style="color: var(--color-success); font-size: 0.85rem;">✓ Order Complete</span>
                                        </c:if>
                                        <c:if test="${ord.status == 'CANCELLED'}">
                                            <span style="color: var(--color-danger); font-size: 0.85rem;">Order Cancelled</span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state" style="padding: 2rem;">
                    <p style="margin: 0; color: var(--color-text-muted);">No orders received yet.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Add Product Modal -->
<div id="addProductModal" class="modal-backdrop">
    <div class="modal-box">
        <div class="modal-header">
            <h3>Add New Product Listing</h3>
            <button type="button" class="modal-close-btn" id="closeAddModalBtn">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/seller/products" method="POST">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <div class="form-group">
                <label class="form-label" for="addProductName">Product Title *</label>
                <input type="text" id="addProductName" name="name" class="form-control" placeholder="e.g. Masterwork Chronograph" required>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label" for="addProductCategory">Category *</label>
                    <input type="text" id="addProductCategory" name="category" class="form-control" list="categoryOptions" placeholder="Watches, Audio, etc." required>
                    <datalist id="categoryOptions">
                        <c:forEach items="${categories}" var="cat">
                            <option value="${cat}">
                        </c:forEach>
                    </datalist>
                </div>

                <div class="form-group">
                    <label class="form-label" for="addProductPrice">Price (₹) *</label>
                    <input type="number" id="addProductPrice" name="price" class="form-control" step="0.01" min="1" placeholder="2499.00" required>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label" for="addProductStock">Stock Quantity *</label>
                    <input type="number" id="addProductStock" name="stockQty" class="form-control" min="0" value="10" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="addProductImage">Image URL</label>
                    <input type="url" id="addProductImage" name="imageUrl" class="form-control" placeholder="https://images.unsplash.com/...">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="addProductDescription">Description *</label>
                <textarea id="addProductDescription" name="description" class="form-control" rows="3" placeholder="Artisan narrative, materials, and specification details..." required></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem;">
                <button type="button" class="btn btn-outline" id="cancelAddModalBtn">Cancel</button>
                <button type="submit" class="btn btn-primary">Create Listing</button>
            </div>
        </form>
    </div>
</div>

<!-- Edit Product Modal -->
<div id="editProductModal" class="modal-backdrop">
    <div class="modal-box">
        <div class="modal-header">
            <h3>Edit Product Listing</h3>
            <button type="button" class="modal-close-btn" id="closeEditModalBtn">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/seller/products/update" method="POST">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="productId" id="editProductId" value="">

            <div class="form-group">
                <label class="form-label" for="editProductName">Product Title *</label>
                <input type="text" id="editProductName" name="name" class="form-control" required>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label" for="editProductCategory">Category *</label>
                    <input type="text" id="editProductCategory" name="category" class="form-control" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="editProductPrice">Price (₹) *</label>
                    <input type="number" id="editProductPrice" name="price" class="form-control" step="0.01" min="1" required>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label" for="editProductStock">Stock Quantity *</label>
                    <input type="number" id="editProductStock" name="stockQty" class="form-control" min="0" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="editProductImage">Image URL</label>
                    <input type="url" id="editProductImage" name="imageUrl" class="form-control">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="editProductDescription">Description *</label>
                <textarea id="editProductDescription" name="description" class="form-control" rows="3" required></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem;">
                <button type="button" class="btn btn-outline" id="cancelEditModalBtn">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/api-client.js"></script>
<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/seller.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
