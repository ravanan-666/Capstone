<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'DjMart — Premium Multi-Seller Marketplace'}" /></title>
    <meta name="csrf-token" content="${sessionScope.csrfToken != null ? sessionScope.csrfToken : csrfToken}">
    <meta name="context-path" content="${pageContext.request.contextPath}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
<header class="site-header">
    <div class="container">
        <a href="${pageContext.request.contextPath}/" class="brand-logo">Dj<span>Mart</span></a>
        
        <button class="mobile-menu-toggle" id="mobileMenuToggle" aria-label="Toggle Navigation" aria-expanded="false">
            <span class="bar"></span>
            <span class="bar"></span>
            <span class="bar"></span>
        </button>

        <nav class="nav-container" id="navContainer">
            <ul class="nav-menu">
                <li><a href="${pageContext.request.contextPath}/products" class="nav-link">Explore Products</a></li>
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <c:if test="${sessionScope.user.role == 'BUYER'}">
                            <li>
                                <a href="${pageContext.request.contextPath}/cart" class="nav-link cart-link">
                                    Cart <span id="navCartBadge" class="badge badge-accent"></span>
                                </a>
                            </li>
                            <li><a href="${pageContext.request.contextPath}/orders" class="nav-link">My Orders</a></li>
                        </c:if>
                        <c:if test="${sessionScope.user.role == 'SELLER'}">
                            <li><a href="${pageContext.request.contextPath}/seller/dashboard" class="nav-link">Seller Dashboard</a></li>
                        </c:if>
                        <c:if test="${sessionScope.user.role == 'ADMIN'}">
                            <li><a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link">Admin Panel</a></li>
                        </c:if>
                        <li><a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-outline">Logout</a></li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="${pageContext.request.contextPath}/cart" class="nav-link cart-link">
                                Cart <span id="navCartBadge" class="badge badge-accent"></span>
                            </a>
                        </li>
                        <li><a href="${pageContext.request.contextPath}/auth/login" class="nav-link">Sign In</a></li>
                        <li><a href="${pageContext.request.contextPath}/auth/register" class="btn btn-primary">Join Marketplace</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </nav>
    </div>
</header>
<div id="toastContainer" class="toast-container" aria-live="polite" aria-atomic="true"></div>
<main class="main-content">
