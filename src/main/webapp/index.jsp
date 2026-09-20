<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="DjMart — Curated Multi-Seller Marketplace" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<!-- Hero Section -->
<section class="hero" style="background: linear-gradient(180deg, #ffffff 0%, #fbfbf9 100%); padding: 5rem 0 4rem; border-bottom: 1px solid var(--color-border);">
    <div class="container" style="text-align: center; max-width: 820px;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); margin-bottom: 1rem; display: inline-block;">
            Premium Artisanal & Precision Marketplace
        </span>
        <h1 style="font-size: 3.2rem; margin-bottom: 1.5rem; letter-spacing: -1px; line-height: 1.2;">
            Designed for those who value quality.
        </h1>
        <p style="font-size: 1.15rem; color: var(--color-text-muted); margin-bottom: 2.5rem; line-height: 1.7;">
            Discover curated collections from verified independent makers and sellers. Handcrafted goods, precision tech, and everyday essentials engineered with pure integrity.
        </p>
        <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary" style="padding: 0.85rem 2.2rem; font-size: 1rem;">
                Explore Collection
            </a>
            <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-outline" style="padding: 0.85rem 2.2rem; font-size: 1rem;">
                Become a Seller
            </a>
        </div>
    </div>
</section>

<!-- Marketplace Values Bar -->
<section style="background: #ffffff; border-bottom: 1px solid var(--color-border); padding: 1.75rem 0;">
    <div class="container" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; text-align: center;">
        <div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem;">Verified Independent Sellers</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Each merchant catalog thoroughly inspected</p>
        </div>
        <div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem;">Transparent Server Pricing</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Zero hidden charges; authoritative totals</p>
        </div>
        <div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem;">Direct Artisan Sourcing</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Ethically made products built to endure</p>
        </div>
        <div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem;">Secure Transactions</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Multi-layered defense with atomic checkout</p>
        </div>
    </div>
</section>

<!-- Curated Categories Section -->
<section class="container" style="padding: 4.5rem 1.5rem 3rem;">
    <div style="text-align: center; margin-bottom: 3rem;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.5rem;">
            Curated Categories
        </span>
        <h2 style="font-size: 2.2rem; color: var(--color-primary);">Explore by Discipline</h2>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1.75rem;">
        <!-- Category 1: Electronics -->
        <a href="${pageContext.request.contextPath}/products?category=Electronics" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, border-color 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #eee;">
                <img src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500" alt="Electronics" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.5rem;">
                <h3 style="font-size: 1.25rem; margin-bottom: 0.35rem;">Precision Electronics</h3>
                <p style="font-size: 0.9rem; color: var(--color-text-muted); margin: 0;">Acoustic fidelity and high-performance mechanical instruments.</p>
            </div>
        </a>

        <!-- Category 2: Fashion -->
        <a href="${pageContext.request.contextPath}/products?category=Fashion" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, border-color 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #eee;">
                <img src="https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500" alt="Fashion" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.5rem;">
                <h3 style="font-size: 1.25rem; margin-bottom: 0.35rem;">Heirloom Fashion</h3>
                <p style="font-size: 0.9rem; color: var(--color-text-muted); margin: 0;">Full-grain leather and organic textiles tailored for longevity.</p>
            </div>
        </a>

        <!-- Category 3: Home & Kitchen -->
        <a href="${pageContext.request.contextPath}/products?category=Home+%26+Kitchen" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, border-color 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #eee;">
                <img src="https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500" alt="Home & Kitchen" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.5rem;">
                <h3 style="font-size: 1.25rem; margin-bottom: 0.35rem;">Home & Living</h3>
                <p style="font-size: 0.9rem; color: var(--color-text-muted); margin: 0;">Culinary cutlery and pour-over tools designed for quiet daily beauty.</p>
            </div>
        </a>

        <!-- Category 4: Books & Stationery -->
        <a href="${pageContext.request.contextPath}/products?category=Books+%26+Stationery" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, border-color 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #eee;">
                <img src="https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500" alt="Books & Stationery" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.5rem;">
                <h3 style="font-size: 1.25rem; margin-bottom: 0.35rem;">Archival Stationery</h3>
                <p style="font-size: 0.9rem; color: var(--color-text-muted); margin: 0;">Heavyweight archival paper and instruments for considered thought.</p>
            </div>
        </a>
    </div>
</section>

<!-- Promotional Banner -->
<section style="background: var(--color-primary); color: #ffffff; padding: 4.5rem 0; margin-top: 2rem;">
    <div class="container" style="text-align: center; max-width: 720px;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.75rem;">
            Ethical Provenance
        </span>
        <h2 style="font-size: 2.2rem; color: #ffffff; margin-bottom: 1.25rem;">
            A better standard of everyday essentials.
        </h2>
        <p style="color: #b0bec5; font-size: 1.05rem; line-height: 1.7; margin-bottom: 2rem;">
            Every artisan on DjMart undergoes peer and provenance verification. We believe marketplace commerce should honor craft, quality, and human care.
        </p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-accent" style="padding: 0.85rem 2.2rem; font-size: 1rem;">
            Discover More
        </a>
    </div>
</section>

<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
