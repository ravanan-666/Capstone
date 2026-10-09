<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="DJ Mart — Modern Curated E-Commerce & Precision Tech" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<!-- Hero Section with Search -->
<section class="hero" style="background: linear-gradient(135deg, #0f172a 0%, #1e293b 60%, #0f172a 100%); color: #ffffff; padding: 5rem 0 4.5rem; position: relative; overflow: hidden;">
    <div class="container" style="text-align: center; max-width: 860px; position: relative; z-index: 2;">
        <span style="display: inline-block; text-transform: uppercase; letter-spacing: 2.5px; font-size: 0.8rem; font-weight: 700; color: #60a5fa; margin-bottom: 1.25rem; background: rgba(96, 165, 250, 0.12); padding: 0.35rem 1rem; border-radius: 20px; border: 1px solid rgba(96, 165, 250, 0.25);">
            Curated Artisanal &amp; Precision Tech Marketplace
        </span>
        <h1 style="font-size: 3.4rem; margin-bottom: 1.25rem; letter-spacing: -1px; line-height: 1.15; color: #ffffff;">
            Crafted for those who demand excellence.
        </h1>
        <p style="font-size: 1.15rem; color: #94a3b8; margin-bottom: 2.5rem; line-height: 1.7; max-width: 700px; margin-left: auto; margin-right: auto;">
            Discover verified products across electronics, heirloom fashion, home essentials, and stationery. Guaranteed authentic Indian Rupee market prices with AI-guided assistance.
        </p>

        <!-- Quick Search Bar in Hero -->
        <form action="${pageContext.request.contextPath}/products" method="GET" style="display: flex; max-width: 600px; margin: 0 auto 2.25rem; background: #ffffff; border-radius: 8px; padding: 0.35rem; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);">
            <input type="text"
                   name="search"
                   placeholder="Search products, brands (e.g. Sony, Apple, Nike)..."
                   style="flex: 1; border: none; outline: none; padding: 0.75rem 1.25rem; font-size: 1rem; color: #0f172a; border-radius: 6px;"
                   autocomplete="off">
            <button type="submit" class="btn btn-primary" style="padding: 0.75rem 1.75rem; font-size: 0.95rem; font-weight: 600;">
                Search
            </button>
        </form>

        <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/products" class="btn btn-accent" style="padding: 0.85rem 2.2rem; font-size: 1rem;">
                Explore All Products &rarr;
            </a>
            <button type="button" onclick="document.getElementById('chatToggleBtn').click();" class="btn btn-outline" style="padding: 0.85rem 1.8rem; font-size: 1rem; color: #ffffff; border-color: rgba(255, 255, 255, 0.3); background: rgba(255, 255, 255, 0.05);">
                ✨ Ask DJ Mart AI
            </button>
        </div>
    </div>
</section>

<!-- Trust & Marketplace Values Bar -->
<section style="background: #ffffff; border-bottom: 1px solid var(--color-border); padding: 2rem 0; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);">
    <div class="container" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 2rem; text-align: center;">
        <div style="padding: 0.5rem;">
            <div style="font-size: 1.6rem; margin-bottom: 0.4rem;">🏷️</div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem; color: var(--color-primary);">Verified INR Pricing</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Authoritative prices verified from official retailers</p>
        </div>
        <div style="padding: 0.5rem;">
            <div style="font-size: 1.6rem; margin-bottom: 0.4rem;">🛡️</div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem; color: var(--color-primary);">Atomic Transactions</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Zero stock inconsistencies with database transactions</p>
        </div>
        <div style="padding: 0.5rem;">
            <div style="font-size: 1.6rem; margin-bottom: 0.4rem;">🤖</div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem; color: var(--color-primary);">Real AI Shopping Engine</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Live database querying for budgets &amp; recommendations</p>
        </div>
        <div style="padding: 0.5rem;">
            <div style="font-size: 1.6rem; margin-bottom: 0.4rem;">🚚</div>
            <h4 style="font-size: 0.95rem; font-family: var(--font-sans); font-weight: 700; margin-bottom: 0.25rem; color: var(--color-primary);">Nationwide Delivery</h4>
            <p style="font-size: 0.825rem; color: var(--color-text-muted); margin: 0;">Careful packaging and doorstep courier tracking</p>
        </div>
    </div>
</section>

<!-- Curated Categories Section (5 Categories) -->
<section class="container" style="padding: 4.5rem 1.5rem 3rem;">
    <div style="text-align: center; margin-bottom: 3rem;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.5rem;">
            Curated Categories
        </span>
        <h2 style="font-size: 2.2rem; color: var(--color-primary);">Explore by Discipline</h2>
        <p style="color: var(--color-text-muted); max-width: 600px; margin: 0.5rem auto 0;">Over 20 hand-selected products cataloged across 5 essential departments.</p>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem;">
        <!-- Category 1: Electronics -->
        <a href="${pageContext.request.contextPath}/products?category=Electronics" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, box-shadow 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #f1f5f9;">
                <img src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop" alt="Electronics" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.25rem;">
                <span class="badge" style="background: #eff6ff; color: #2563eb; margin-bottom: 0.35rem;">7 Products</span>
                <h3 style="font-size: 1.15rem; margin-bottom: 0.25rem;">Precision Electronics</h3>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin: 0;">Sony, Apple, Dell, Keychron, Marshall, boAt acoustic instruments.</p>
            </div>
        </a>

        <!-- Category 2: Fashion -->
        <a href="${pageContext.request.contextPath}/products?category=Fashion" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, box-shadow 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #f1f5f9;">
                <img src="https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500&auto=format&fit=crop" alt="Fashion" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.25rem;">
                <span class="badge" style="background: #eff6ff; color: #2563eb; margin-bottom: 0.35rem;">5 Products</span>
                <h3 style="font-size: 1.15rem; margin-bottom: 0.25rem;">Heirloom Fashion</h3>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin: 0;">Levi's, Hidesign leather, Raymond, Fabindia, Nike footwear.</p>
            </div>
        </a>

        <!-- Category 3: Home & Kitchen -->
        <a href="${pageContext.request.contextPath}/products?category=Home+%26+Kitchen" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, box-shadow 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #f1f5f9;">
                <img src="https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500&auto=format&fit=crop" alt="Home & Kitchen" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.25rem;">
                <span class="badge" style="background: #eff6ff; color: #2563eb; margin-bottom: 0.35rem;">4 Products</span>
                <h3 style="font-size: 1.15rem; margin-bottom: 0.25rem;">Home &amp; Culinary</h3>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin: 0;">Fellow Stagg kettles, Prestige cookware, Shun knives, Philips.</p>
            </div>
        </a>

        <!-- Category 4: Books & Stationery -->
        <a href="${pageContext.request.contextPath}/products?category=Books+%26+Stationery" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, box-shadow 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #f1f5f9;">
                <img src="https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop" alt="Books & Stationery" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.25rem;">
                <span class="badge" style="background: #eff6ff; color: #2563eb; margin-bottom: 0.35rem;">4 Products</span>
                <h3 style="font-size: 1.15rem; margin-bottom: 0.25rem;">Archival Stationery</h3>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin: 0;">Leuchtturm1917, Lamy Safari, Parker Sonnet, Midori journals.</p>
            </div>
        </a>

        <!-- Category 5: Fitness & Lifestyle -->
        <a href="${pageContext.request.contextPath}/products?category=Fitness+%26+Lifestyle" class="card" style="display: flex; flex-direction: column; overflow: hidden; padding: 0; text-decoration: none; color: inherit; transition: transform 0.2s ease, box-shadow 0.2s ease;">
            <div style="height: 180px; overflow: hidden; background-color: #f1f5f9;">
                <img src="https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?w=500&auto=format&fit=crop" alt="Fitness & Lifestyle" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div style="padding: 1.25rem;">
                <span class="badge" style="background: #eff6ff; color: #2563eb; margin-bottom: 0.35rem;">4 Products</span>
                <h3 style="font-size: 1.15rem; margin-bottom: 0.25rem;">Fitness &amp; Lifestyle</h3>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin: 0;">Fitbit trackers, Manduka yoga mats, Milton bottles, Titan.</p>
            </div>
        </a>
    </div>
</section>

<!-- AI Assistant Spotlight Section -->
<section style="background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%); border-top: 1px solid #bfdbfe; border-bottom: 1px solid #bfdbfe; padding: 4rem 0;">
    <div class="container" style="display: grid; grid-template-columns: 1.2fr 1fr; gap: 3rem; align-items: center;">
        <div>
            <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: var(--color-accent); display: block; margin-bottom: 0.5rem;">
                Intelligent Marketplace Engine
            </span>
            <h2 style="font-size: 2.2rem; color: var(--color-primary); margin-bottom: 1rem; line-height: 1.25;">
                Meet DJ Mart AI: Conversational Shopping Built In.
            </h2>
            <p style="color: #334155; font-size: 1.05rem; line-height: 1.7; margin-bottom: 1.5rem;">
                Tired of search bars that don't understand you? Our AI chatbot connects directly with live database inventory. Ask for budget recommendations, check item availability, track active orders, or compare products instantly.
            </p>
            <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                <button type="button" onclick="document.getElementById('chatToggleBtn').click();" class="btn btn-primary" style="padding: 0.8rem 1.75rem;">
                    Launch AI Chat &rarr;
                </button>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-outline" style="background: #fff; padding: 0.8rem 1.5rem;">
                    Browse Catalog
                </a>
            </div>
        </div>

        <!-- Chat Preview Card Mockup -->
        <div style="background: #ffffff; border-radius: 12px; padding: 1.5rem; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08); border: 1px solid #cbd5e1;">
            <div style="display: flex; align-items: center; gap: 0.75rem; border-bottom: 1px solid #e2e8f0; padding-bottom: 1rem; margin-bottom: 1rem;">
                <div style="width: 38px; height: 38px; border-radius: 50%; background: #2563eb; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700;">DJ</div>
                <div>
                    <strong style="color: #0f172a; font-size: 0.95rem; display: block;">DJ Mart AI Assistant</strong>
                    <span style="font-size: 0.75rem; color: #16a34a; font-weight: 600;">● Online &bull; Live Database</span>
                </div>
            </div>
            <div style="display: flex; flex-direction: column; gap: 0.75rem; font-size: 0.9rem;">
                <div style="align-self: flex-end; background: #2563eb; color: #fff; padding: 0.6rem 1rem; border-radius: 12px 12px 2px 12px; max-width: 80%;">
                    "Show me wireless headphones under ₹10,000"
                </div>
                <div style="align-self: flex-start; background: #f1f5f9; color: #0f172a; padding: 0.75rem 1rem; border-radius: 12px 12px 12px 2px; max-width: 90%;">
                    Here are top audio recommendations matching your budget:
                    <div style="margin-top: 0.5rem; font-weight: 600;">
                        • boAt Rockerz 550 — ₹1,999.00 (In Stock)<br>
                        • Marshall Emberton II — ₹17,499.00
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Call to Action Footer Banner -->
<section style="background: var(--color-primary); color: #ffffff; padding: 4.5rem 0;">
    <div class="container" style="text-align: center; max-width: 720px;">
        <span style="text-transform: uppercase; letter-spacing: 2px; font-size: 0.8rem; font-weight: 700; color: #60a5fa; display: block; margin-bottom: 0.75rem;">
            Ethical Provenance &amp; Engineering
        </span>
        <h2 style="font-size: 2.2rem; color: #ffffff; margin-bottom: 1.25rem;">
            A better standard of everyday commerce.
        </h2>
        <p style="color: #94a3b8; font-size: 1.05rem; line-height: 1.7; margin-bottom: 2rem;">
            Every artisan and product on DJ Mart undergoes provenance inspection. We believe digital marketplaces should celebrate craft, precision, and customer trust.
        </p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-accent" style="padding: 0.85rem 2.2rem; font-size: 1rem;">
            Explore All 24 Products
        </a>
    </div>
</section>

<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
