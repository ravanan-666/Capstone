<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Bad Request — DjMart" />
</jsp:include>
<div class="container">
    <div class="error-page">
        <h1>400</h1>
        <h2>Bad Request</h2>
        <p>The request could not be processed due to malformed or invalid syntax.</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>
<jsp:include page="../common/footer.jsp" />
