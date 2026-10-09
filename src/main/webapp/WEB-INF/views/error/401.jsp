<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Unauthorized — DJ Mart" />
</jsp:include>
<div class="container">
    <div class="error-page">
        <h1>401</h1>
        <h2>Authentication Required</h2>
        <p>You must be signed in to access this marketplace feature.</p>
        <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary">Sign In</a>
    </div>
</div>
<jsp:include page="../common/footer.jsp" />
