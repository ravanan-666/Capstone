<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Server Error — DJ Mart" />
</jsp:include>
<div class="container">
    <div class="error-page">
        <h1>500</h1>
        <h2>Internal Server Error</h2>
        <p>A momentary system error occurred while fulfilling your request. Our technical team has been notified. No sensitive details are disclosed.</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>
<jsp:include page="../common/footer.jsp" />
