<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Create Account — DJ Mart Marketplace" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container">
    <div class="auth-container" style="max-width: 480px;">
        <div class="auth-header" style="text-align: center;">
            <a href="${pageContext.request.contextPath}/" aria-label="DJ Mart Homepage">
                <img src="${pageContext.request.contextPath}/static/images/dj_mart_logo.jpg" alt="DJ Mart Logo" class="auth-brand-logo">
            </a>
            <h1>Join DJ Mart</h1>
            <p>Create your marketplace account as a Buyer or Seller</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/register" method="post" novalidate>
            <!-- CSRF Synchronizer Token Protection -->
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken != null ? sessionScope.csrfToken : csrfToken}">

            <div class="form-group">
                <label for="name" class="form-label">Full Name</label>
                <input type="text"
                       id="name"
                       name="name"
                       class="form-control"
                       value="<c:out value='${name}'/>"
                       placeholder="e.g. Eleanor Vance"
                       required
                       autofocus>
                <c:if test="${not empty fieldErrors['name']}">
                    <div class="form-error"><c:out value="${fieldErrors['name']}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="email" class="form-label">Email Address</label>
                <input type="email"
                       id="email"
                       name="email"
                       class="form-control"
                       value="<c:out value='${email}'/>"
                       placeholder="name@domain.com"
                       required
                       autocomplete="email">
                <c:if test="${not empty fieldErrors['email']}">
                    <div class="form-error"><c:out value="${fieldErrors['email']}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="role" class="form-label">I want to join as</label>
                <select id="role" name="role" class="form-control" required>
                    <option value="BUYER" ${role == 'BUYER' || empty role ? 'selected' : ''}>Buyer — Browse, buy, and review curated goods</option>
                    <option value="SELLER" ${role == 'SELLER' ? 'selected' : ''}>Seller — List products and fulfill customer orders</option>
                </select>
                <c:if test="${not empty fieldErrors['role']}">
                    <div class="form-error"><c:out value="${fieldErrors['role']}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password</label>
                <div class="password-field-wrapper">
                    <input type="password"
                           id="password"
                           name="password"
                           class="form-control"
                           placeholder="••••••••"
                           required
                           autocomplete="new-password">
                    <button type="button" class="password-toggle-btn" aria-label="Toggle password visibility"
                            onclick="const p = document.getElementById('password'); p.type = p.type === 'password' ? 'text' : 'password'; this.textContent = p.type === 'password' ? '👁' : '🔒';">
                        👁
                    </button>
                </div>
                <div class="form-text">Must be at least 8 characters with upper, lower, digit, and special symbol.</div>
                <c:if test="${not empty fieldErrors['password']}">
                    <div class="form-error"><c:out value="${fieldErrors['password']}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="confirmPassword" class="form-label">Confirm Password</label>
                <div class="password-field-wrapper">
                    <input type="password"
                           id="confirmPassword"
                           name="confirmPassword"
                           class="form-control"
                           placeholder="••••••••"
                           required
                           autocomplete="new-password">
                    <button type="button" class="password-toggle-btn" aria-label="Toggle password visibility"
                            onclick="const p = document.getElementById('confirmPassword'); p.type = p.type === 'password' ? 'text' : 'password'; this.textContent = p.type === 'password' ? '👁' : '🔒';">
                        👁
                    </button>
                </div>
                <c:if test="${not empty fieldErrors['confirmPassword']}">
                    <div class="form-error"><c:out value="${fieldErrors['confirmPassword']}"/></div>
                </c:if>
            </div>

            <div style="margin-top: 1.5rem;">
                <button type="submit" class="btn btn-primary btn-block">Create Account</button>
            </div>
        </form>

        <div class="auth-footer">
            <p>Already have an account? <a href="${pageContext.request.contextPath}/auth/login">Sign in</a></p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
