<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="DjMart — Curated Multi-Seller Marketplace" />
</jsp:include>

<section class="hero" style="background: linear-gradient(180deg, #ffffff 0%, #f4f5f7 100%); padding: 5rem 0; border-bottom: 1px solid var(--color-border);">
    <div class="container" style="text-align: center; max-width: 800px;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); margin-bottom: 1rem; display: inline-block;">
            Premium Artisanal & Tech Goods
        </span>
        <h1 style="font-size: 3.2rem; margin-bottom: 1.5rem; letter-spacing: -1px;">
            The Multi-Seller Marketplace Built for Discernment
        </h1>
        <p style="font-size: 1.15rem; color: var(--color-text-muted); margin-bottom: 2.5rem; line-height: 1.7;">
            Discover curated collections from verified independent sellers. Handcrafted goods, precision tech, and everyday essentials engineered with pure integrity.
        </p>
        <div style="display: flex; gap: 1rem; justify-content: center;">
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary" style="padding: 0.85rem 2rem; font-size: 1rem;">Browse Collection</a>
            <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-outline" style="padding: 0.85rem 2rem; font-size: 1rem;">Become a Seller</a>
        </div>
    </div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
