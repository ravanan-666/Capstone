<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Page Not Found — DJ Mart" />
</jsp:include>
<div class="container">
    <div class="error-page">
        <h1>404</h1>
        <h2>Page Not Found</h2>
        <p>The marketplace listing or page you requested could not be found.</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>
<jsp:include page="../common/footer.jsp" />
