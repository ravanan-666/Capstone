<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Sign In — DjMart Marketplace" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container">
    <div class="auth-container">
        <div class="auth-header">
            <h1>Welcome Back</h1>
            <p>Sign in to access your DjMart account</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success" role="alert">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty infoMessage}">
            <div class="alert alert-info" role="alert">
                <c:out value="${infoMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="post" novalidate>
            <!-- CSRF Synchronizer Token Protection -->
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken != null ? sessionScope.csrfToken : csrfToken}">

            <c:if test="${not empty redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${redirect}'/>">
            </c:if>

            <div class="form-group">
                <label for="email" class="form-label">Email Address</label>
                <input type="email"
                       id="email"
                       name="email"
                       class="form-control"
                       value="<c:out value='${email}'/>"
                       placeholder="name@domain.com"
                       required
                       autocomplete="email"
                       autofocus>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password</label>
                <input type="password"
                       id="password"
                       name="password"
                       class="form-control"
                       placeholder="Enter your account password"
                       required
                       autocomplete="current-password">
            </div>

            <div style="margin-top: 1.5rem;">
                <button type="submit" class="btn btn-primary btn-block">Sign In</button>
            </div>
        </form>

        <div class="auth-footer">
            <p>New to DjMart? <a href="${pageContext.request.contextPath}/auth/register">Create an account</a></p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
