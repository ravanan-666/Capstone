<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Access Forbidden — DJ Mart" />
</jsp:include>
<div class="container">
    <div class="error-page">
        <h1>403</h1>
        <h2>Access Forbidden</h2>
        <p>You do not have the required permissions to access this portal or resource.</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>
<jsp:include page="../common/footer.jsp" />
