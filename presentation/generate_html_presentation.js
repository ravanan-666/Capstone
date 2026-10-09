// presentation/generate_html_presentation.js
// Generates a self-contained interactive HTML5 presentation deck and compiles it to PDF

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('Generating interactive HTML presentation for DJ MART...');

const screenshotDir = path.join(__dirname, 'screenshots');
const logoPath = path.join(__dirname, '..', 'src', 'main', 'webapp', 'static', 'images', 'dj_mart_logo.jpg');

function toBase64(filePath) {
  if (fs.existsSync(filePath)) {
    const ext = path.extname(filePath).slice(1);
    const data = fs.readFileSync(filePath);
    return `data:image/${ext === 'jpg' ? 'jpeg' : ext};base64,${data.toString('base64')}`;
  }
  return '';
}

const logoBase64 = toBase64(logoPath);
const homeImgBase64 = toBase64(path.join(screenshotDir, 'slide7_home.png'));
const productsImgBase64 = toBase64(path.join(screenshotDir, 'slide8_products.png'));
const detailsImgBase64 = toBase64(path.join(screenshotDir, 'slide8_product_details.png'));
const cartImgBase64 = toBase64(path.join(screenshotDir, 'slide9_cart.png'));
const checkoutImgBase64 = toBase64(path.join(screenshotDir, 'slide10_checkout.png'));
const deskImgBase64 = toBase64(path.join(screenshotDir, 'slide12_desktop.png'));
const tabImgBase64 = toBase64(path.join(screenshotDir, 'slide12_tablet.png'));
const mobImgBase64 = toBase64(path.join(screenshotDir, 'slide12_mobile.png'));

const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DJ MART – E-Commerce Website | College Project Presentation</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --obsidian: #0F172A;
      --deep-bg: #0B1120;
      --card-dark: #1E293B;
      --gold: #C5A880;
      --gold-dark: #9A7B4F;
      --gold-light: #E8D5B5;
      --ivory-bg: #F8FAFC;
      --card-light: #FFFFFF;
      --text-dark: #0F172A;
      --text-muted: #64748B;
      --border-light: #E2E8F0;
      --border-gold: #C5A880;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-print-color-adjust: exact !important;
      print-color-adjust: exact !important;
    }

    body {
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      background-color: #060913;
      color: #1E293B;
      overflow: hidden;
      height: 100vh;
      display: flex;
      flex-direction: column;
      user-select: none;
    }

    /* Top Progress Bar */
    #progress-bar {
      position: fixed;
      top: 0;
      left: 0;
      height: 4px;
      background: linear-gradient(90deg, #9A7B4F, #C5A880, #FDE68A);
      width: 0%;
      z-index: 9999;
      transition: width 0.3s ease;
    }

    /* Presentation Viewport */
    #deck-container {
      flex: 1;
      position: relative;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      perspective: 1000px;
    }

    .slide {
      position: absolute;
      width: 96vw;
      max-width: 1500px;
      height: 54vw;
      max-height: 843px;
      aspect-ratio: 16 / 9;
      border-radius: 12px;
      box-shadow: 0 25px 60px rgba(0, 0, 0, 0.6);
      opacity: 0;
      visibility: hidden;
      transform: scale(0.96) translateX(40px);
      transition: opacity 0.4s cubic-bezier(0.16, 1, 0.3, 1),
                  transform 0.4s cubic-bezier(0.16, 1, 0.3, 1),
                  visibility 0.4s;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 44px 54px 30px 54px;
      background: var(--ivory-bg);
    }

    .slide.active {
      opacity: 1;
      visibility: visible;
      transform: scale(1) translateX(0);
      z-index: 10;
    }

    .slide.prev {
      transform: scale(0.96) translateX(-40px);
    }

    /* Dark Slides */
    .slide.dark {
      background: var(--deep-bg);
      color: #FFFFFF;
    }

    /* Header Component */
    .slide-header {
      margin-bottom: 22px;
    }
    .slide-category {
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 2px;
      text-transform: uppercase;
      color: var(--gold-dark);
      margin-bottom: 6px;
    }
    .slide.dark .slide-category {
      color: var(--gold);
    }
    .slide-title {
      font-family: 'Playfair Display', Georgia, serif;
      font-size: 32px;
      font-weight: 700;
      color: var(--text-dark);
      line-height: 1.2;
      margin-bottom: 6px;
    }
    .slide.dark .slide-title {
      color: #FFFFFF;
    }
    .slide-subtitle {
      font-size: 15px;
      color: var(--text-muted);
      font-weight: 400;
    }
    .slide.dark .slide-subtitle {
      color: #94A3B8;
    }
    .gold-bar {
      width: 70px;
      height: 3px;
      background: var(--gold);
      margin-top: 10px;
      border-radius: 2px;
    }

    /* Slide Footer Component */
    .slide-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid var(--border-light);
      padding-top: 14px;
      font-size: 12px;
      color: var(--text-muted);
    }
    .slide.dark .slide-footer {
      border-color: #1E293B;
      color: #64748B;
    }
    .footer-brand {
      font-weight: 600;
      color: var(--text-dark);
    }
    .slide.dark .footer-brand {
      color: var(--gold);
    }
    .footer-tool {
      font-weight: 600;
      color: var(--gold-dark);
    }
    .slide.dark .footer-tool {
      color: var(--gold);
    }
    .slide-num {
      font-weight: 700;
      font-variant-numeric: tabular-nums;
    }

    /* Reusable Card Styles */
    .grid-3x2 {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 18px;
      flex: 1;
    }
    .grid-4x2 {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      flex: 1;
    }
    .grid-2col {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
      flex: 1;
    }

    .info-card {
      background: #FFFFFF;
      border: 1px solid var(--border-light);
      border-radius: 10px;
      padding: 20px;
      display: flex;
      flex-direction: column;
      box-shadow: 0 4px 12px rgba(0,0,0,0.03);
    }
    .slide.dark .info-card {
      background: var(--card-dark);
      border-color: #334155;
    }

    .card-icon {
      font-size: 24px;
      margin-bottom: 10px;
    }
    .card-title {
      font-size: 15px;
      font-weight: 700;
      color: var(--text-dark);
      margin-bottom: 8px;
    }
    .slide.dark .card-title {
      color: #FFFFFF;
    }
    .card-desc {
      font-size: 12.5px;
      line-height: 1.5;
      color: var(--text-muted);
    }
    .slide.dark .card-desc {
      color: #94A3B8;
    }

    /* Comparison Columns */
    .compare-col {
      padding: 24px;
      border-radius: 12px;
      display: flex;
      flex-direction: column;
    }
    .col-problem {
      background: #FEF2F2;
      border: 1.5px solid #FCA5A5;
      color: #991B1B;
    }
    .col-solution {
      background: #F0FDF4;
      border: 1.5px solid #86EFAC;
      color: #166534;
    }
    .compare-title {
      font-size: 15px;
      font-weight: 700;
      letter-spacing: 1px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .compare-list {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 12px;
      font-size: 13px;
      line-height: 1.45;
    }

    /* Mockup Frames */
    .mockup-frame {
      background: #FFFFFF;
      border: 2px solid var(--border-light);
      border-radius: 10px;
      overflow: hidden;
      box-shadow: 0 10px 25px rgba(0,0,0,0.08);
      display: flex;
      flex-direction: column;
      height: 100%;
    }
    .mockup-header {
      background: #F1F5F9;
      padding: 8px 14px;
      display: flex;
      align-items: center;
      gap: 6px;
      border-bottom: 1px solid var(--border-light);
    }
    .dot {
      width: 9px;
      height: 9px;
      border-radius: 50%;
    }
    .dot.red { background: #EF4444; }
    .dot.yellow { background: #F59E0B; }
    .dot.green { background: #10B981; }
    .mockup-url {
      font-size: 11px;
      background: #FFFFFF;
      padding: 2px 14px;
      border-radius: 12px;
      color: #64748B;
      margin-left: 10px;
      border: 1px solid #E2E8F0;
      flex: 1;
      max-width: 320px;
    }
    .mockup-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    /* Navigation Controls Overlay */
    .nav-controls {
      position: fixed;
      bottom: 20px;
      right: 30px;
      display: flex;
      gap: 10px;
      z-index: 1000;
    }
    .nav-btn {
      background: rgba(15, 23, 42, 0.85);
      border: 1px solid rgba(197, 168, 128, 0.4);
      color: #FFFFFF;
      width: 44px;
      height: 44px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 18px;
      transition: all 0.2s;
      backdrop-filter: blur(8px);
    }
    .nav-btn:hover {
      background: var(--gold);
      color: var(--obsidian);
      border-color: var(--gold);
    }
    .key-hint {
      position: fixed;
      bottom: 24px;
      left: 30px;
      color: #64748B;
      font-size: 12px;
      z-index: 1000;
      display: flex;
      gap: 16px;
    }
    .key-hint span {
      background: rgba(255, 255, 255, 0.1);
      padding: 2px 6px;
      border-radius: 4px;
      color: #94A3B8;
      font-family: monospace;
    }

    /* Print styles */
    @media print {
      body {
        background: transparent !important;
        overflow: visible !important;
        height: auto !important;
      }
      #progress-bar, .nav-controls, .key-hint {
        display: none !important;
      }
      #deck-container {
        display: block !important;
        height: auto !important;
      }
      .slide {
        position: relative !important;
        width: 100% !important;
        height: 100vh !important;
        aspect-ratio: 16 / 9 !important;
        opacity: 1 !important;
        visibility: visible !important;
        transform: none !important;
        page-break-after: always !important;
        break-after: page !important;
        box-shadow: none !important;
        margin: 0 !important;
        border-radius: 0 !important;
      }
    }
  </style>
</head>
<body>

  <div id="progress-bar"></div>

  <div id="deck-container">

    <!-- ========================================== -->
    <!-- SLIDE 1: TITLE -->
    <!-- ========================================== -->
    <div class="slide dark active" data-slide="1">
      <div style="display: flex; justify-content: space-between; align-items: flex-start;">
        ${logoBase64 ? `<img src="${logoBase64}" alt="DJ Mart Logo" style="height: 64px; border-radius: 4px; border: 1px solid rgba(197, 168, 128, 0.3);">` : '<div></div>'}
        <div style="background: rgba(197, 168, 128, 0.15); border: 1px solid var(--gold); padding: 6px 16px; border-radius: 20px; font-size: 11px; font-weight: 700; color: var(--gold); letter-spacing: 1.5px;">
          COLLEGE PROJECT PRESENTATION & VIVA
        </div>
      </div>

      <div style="margin: 30px 0; display: flex; justify-content: space-between; align-items: center; gap: 40px;">
        <div style="flex: 1.2;">
          <h1 style="font-family: 'Playfair Display', Georgia, serif; font-size: 68px; font-weight: 700; color: var(--gold); line-height: 1.1; margin-bottom: 12px; letter-spacing: 1px;">
            DJ MART
          </h1>
          <h2 style="font-size: 34px; font-weight: 700; color: #FFFFFF; margin-bottom: 14px;">
            E-Commerce Website
          </h2>
          <p style="font-family: 'Playfair Display', serif; font-style: italic; font-size: 19px; color: var(--gold-light); margin-bottom: 24px;">
            "Smart, Simple & Convenient Online Shopping"
          </p>
          <div style="display: inline-flex; align-items: center; gap: 10px; background: rgba(56, 189, 248, 0.1); border: 1px solid #38BDF8; padding: 8px 18px; border-radius: 8px; font-size: 13px; font-weight: 600; color: #38BDF8;">
            <span>⚡ Developed using Antigravity</span>
          </div>
        </div>

        <!-- Right Side Academic Card -->
        <div style="flex: 0.9; background: var(--card-dark); border: 1.5px solid #334155; border-radius: 12px; padding: 26px 30px;">
          <div style="font-size: 11px; font-weight: 700; color: var(--gold); letter-spacing: 2px; margin-bottom: 16px;">
            PROJECT DETAILS
          </div>
          <div style="display: flex; flex-direction: column; gap: 12px; font-size: 13px;">
            <div>
              <div style="font-size: 10px; color: #94A3B8; text-transform: uppercase;">Project Title</div>
              <div style="font-weight: 700; color: #FFFFFF;">DJ MART E-Commerce Platform</div>
            </div>
            <div>
              <div style="font-size: 10px; color: #94A3B8; text-transform: uppercase;">Student / Presenter</div>
              <div style="font-weight: 700; color: #FFFFFF;">Candidate Name</div>
            </div>
            <div>
              <div style="font-size: 10px; color: #94A3B8; text-transform: uppercase;">Register Number</div>
              <div style="font-weight: 700; color: #FFFFFF;">[Register Number]</div>
            </div>
            <div>
              <div style="font-size: 10px; color: #94A3B8; text-transform: uppercase;">Department</div>
              <div style="font-weight: 700; color: #FFFFFF;">Dept. of Computer Science & Engineering</div>
            </div>
            <div>
              <div style="font-size: 10px; color: #94A3B8; text-transform: uppercase;">Institution & Academic Year</div>
              <div style="font-weight: 700; color: #FFFFFF;">[Engineering College] | 2025 – 2026</div>
            </div>
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">01 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 2: INTRODUCTION -->
    <!-- ========================================== -->
    <div class="slide" data-slide="2">
      <div class="slide-header">
        <div class="slide-category">Project Overview</div>
        <h2 class="slide-title">Introduction</h2>
        <p class="slide-subtitle">A Modern Online Shopping Platform Designed for Retail Convenience</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-3x2">
        <div class="info-card">
          <div class="card-icon">🛍️</div>
          <div class="card-title">Online Product Browsing</div>
          <div class="card-desc">Effortless catalog navigation enabling customers to discover curated luxury, tech, and everyday items anytime.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🔍</div>
          <div class="card-title">Real-Time Product Discovery</div>
          <div class="card-desc">Instant keyword search and multi-category filtering allowing shoppers to locate target goods in milliseconds.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📋</div>
          <div class="card-title">Detailed Product Pages</div>
          <div class="card-desc">Rich presentation featuring crisp photography, transparent pricing in ₹, live inventory badges, and specs.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🛒</div>
          <div class="card-title">Interactive Shopping Cart</div>
          <div class="card-desc">Seamless quantity adjustments (+/-), live subtotal recalculations, and item removals without page reloads.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📦</div>
          <div class="card-title">Streamlined Order Process</div>
          <div class="card-desc">Frictionless checkout capturing delivery details, payment choice, and safe atomic order placement.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">✨</div>
          <div class="card-title">User-Friendly Responsive UI</div>
          <div class="card-desc">Distinctive luxury design language built with mobile-first adaptability across phones, tablets, and desktops.</div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">02 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 3: PROBLEM STATEMENT -->
    <!-- ========================================== -->
    <div class="slide" data-slide="3">
      <div class="slide-header">
        <div class="slide-category">Market Need & Context</div>
        <h2 class="slide-title">Problem Statement</h2>
        <p class="slide-subtitle">Overcoming Traditional Retail Bottlenecks with a Digital Platform</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-2col">
        <div class="compare-col col-problem">
          <div class="compare-title">⚠️ TRADITIONAL SHOPPING BOTTLENECKS</div>
          <ul class="compare-list">
            <li><strong>Mandatory Store Visits:</strong> Requires physical commute, parking hassles, traffic, and geographical boundaries.</li>
            <li><strong>Time-Consuming Process:</strong> Hours wasted traversing aisles, searching shelves, and waiting in billing queues.</li>
            <li><strong>Difficult Product Comparison:</strong> Hard to compare multiple alternative products, prices, and specifications.</li>
            <li><strong>Restricted Store Timings:</strong> Inaccessible outside standard business hours; unavailable during holidays and late nights.</li>
            <li><strong>Uncertain Stock Availability:</strong> Shoppers travel to physical stores only to find target products out of stock.</li>
          </ul>
        </div>

        <div class="compare-col col-solution">
          <div class="compare-title">✅ THE DJ MART DIGITAL SOLUTION</div>
          <ul class="compare-list">
            <li><strong>Universal 24/7 Access:</strong> Shop from any device at any time, eliminating travel and geographical limits.</li>
            <li><strong>Frictionless Fast Shopping:</strong> Discover products, update cart, and place orders in under two minutes.</li>
            <li><strong>Instant Side-by-Side Comparison:</strong> Clear pricing, high-res photos, and complete specifications.</li>
            <li><strong>Always Open & Reliable:</strong> Uninterrupted digital storefront operating 365 days a year without downtime.</li>
            <li><strong>Live Inventory Transparency:</strong> Real-time stock status prevents ordering unavailable products.</li>
          </ul>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">03 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 4: PROJECT OBJECTIVES -->
    <!-- ========================================== -->
    <div class="slide" data-slide="4">
      <div class="slide-header">
        <div class="slide-category">Scope & Roadmap</div>
        <h2 class="slide-title">Project Objectives</h2>
        <p class="slide-subtitle">Key Engineering & Experience Goals Achieved in DJ Mart</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-4x2">
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">01</div>
          <div class="card-title">Develop Modern Web App</div>
          <div class="card-desc">Build a full-featured, secure e-commerce marketplace using Antigravity AI engineering.</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">02</div>
          <div class="card-title">Easy Product Browsing</div>
          <div class="card-desc">Provide seamless catalog navigation across luxury, tech, and lifestyle merchandise.</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">03</div>
          <div class="card-title">Real-Time Search</div>
          <div class="card-desc">Implement responsive keyword search with instant query execution.</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">04</div>
          <div class="card-title">Structured Categories</div>
          <div class="card-desc">Organize catalog into intuitive categories (Electronics, Fashion, Home, Books).</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">05</div>
          <div class="card-title">Dynamic Shopping Cart</div>
          <div class="card-desc">Support quantity updates (+/-) and price recalculations without page reloads.</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">06</div>
          <div class="card-title">Frictionless Checkout</div>
          <div class="card-desc">Deliver validated delivery address capture and atomic order placement.</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">07</div>
          <div class="card-title">Responsive Interface</div>
          <div class="card-desc">Ensure seamless touch navigation across smartphone, tablet, and desktop viewports.</div>
        </div>
        <div class="info-card">
          <div style="font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--gold-dark); margin-bottom: 6px;">08</div>
          <div class="card-title">Elevate Experience</div>
          <div class="card-desc">Combine luxury editorial aesthetics with high-performance Java backend architecture.</div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">04 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 5: TECHNOLOGIES & TOOLS -->
    <!-- ========================================== -->
    <div class="slide" data-slide="5">
      <div class="slide-header">
        <div class="slide-category">Engineering Architecture</div>
        <h2 class="slide-title">Technologies & Tools Used</h2>
        <p class="slide-subtitle">Verified Development Stack Actually Implemented in DJ Mart</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-3x2">
        <div class="info-card">
          <div style="font-size: 10px; font-weight: 700; color: #0284C7; letter-spacing: 1.5px; margin-bottom: 4px;">AI PLATFORM & IDE</div>
          <div class="card-title">Google Antigravity</div>
          <div style="font-size: 11px; color: var(--gold-dark); font-style: italic; margin-bottom: 8px;">Development Environment</div>
          <div class="card-desc">Advanced agentic AI assistant utilized for full-stack architecture design, Java code generation, testing, and system verification.</div>
        </div>
        <div class="info-card">
          <div style="font-size: 10px; font-weight: 700; color: #D97706; letter-spacing: 1.5px; margin-bottom: 4px;">FRONTEND LAYER</div>
          <div class="card-title">HTML5, CSS3 & Vanilla JS</div>
          <div style="font-size: 11px; color: var(--gold-dark); font-style: italic; margin-bottom: 8px;">Client Interface</div>
          <div class="card-desc">Custom luxury design system with CSS custom properties (variables), native ES6+ Fetch API client, and zero heavy frontend libraries.</div>
        </div>
        <div class="info-card">
          <div style="font-size: 10px; font-weight: 700; color: #16A34A; letter-spacing: 1.5px; margin-bottom: 4px;">VIEW ENGINE</div>
          <div class="card-title">Jakarta Server Pages (JSP)</div>
          <div style="font-size: 11px; color: var(--gold-dark); font-style: italic; margin-bottom: 8px;">Dynamic Server Templates</div>
          <div class="card-desc">Jakarta JSP 3.1 & JSTL 3.0 tag library providing clean server rendering, output escaping against XSS, and modular components.</div>
        </div>
        <div class="info-card">
          <div style="font-size: 10px; font-weight: 700; color: #DC2626; letter-spacing: 1.5px; margin-bottom: 4px;">BACKEND ENGINE</div>
          <div class="card-title">Jakarta Servlet 6.0 & Java 17</div>
          <div style="font-size: 11px; color: var(--gold-dark); font-style: italic; margin-bottom: 8px;">Controllers & Business Logic</div>
          <div class="card-desc">High-speed Java enterprise web architecture implementing Layered MVC (Servlets, Filter Chain, Services, and DAOs).</div>
        </div>
        <div class="info-card">
          <div style="font-size: 10px; font-weight: 700; color: #7C3AED; letter-spacing: 1.5px; margin-bottom: 4px;">DATABASE</div>
          <div class="card-title">H2 Relational Database (SQL)</div>
          <div style="font-size: 11px; color: var(--gold-dark); font-style: italic; margin-bottom: 8px;">Embedded Relational Store</div>
          <div class="card-desc">Embedded SQL database engine with ACID transactions, foreign keys, CHECK constraints, and automated schema migration on boot.</div>
        </div>
        <div class="info-card">
          <div style="font-size: 10px; font-weight: 700; color: #0D9488; letter-spacing: 1.5px; margin-bottom: 4px;">SERVER & SECURITY</div>
          <div class="card-title">Tomcat 10.1 & jBCrypt</div>
          <div style="font-size: 11px; color: var(--gold-dark); font-style: italic; margin-bottom: 8px;">Deployment & Cryptography</div>
          <div class="card-desc">Apache Tomcat 10.1 container, HikariCP connection pool, salted BCrypt password hashing, and CSRF synchronizer tokens.</div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">05 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 6: SYSTEM ARCHITECTURE -->
    <!-- ========================================== -->
    <div class="slide dark" data-slide="6">
      <div class="slide-header">
        <div class="slide-category">Technical Blueprint</div>
        <h2 class="slide-title">System Architecture</h2>
        <p class="slide-subtitle">Multi-Tier Layered MVC Design & Complete Request Lifecycle</p>
        <div class="gold-bar"></div>
      </div>

      <!-- Architecture Pipeline Diagram -->
      <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px; margin: 16px 0;">
        <div style="flex: 1; background: var(--card-dark); border: 1.5px solid #38BDF8; padding: 14px; border-radius: 8px; text-align: center;">
          <div style="font-size: 11px; font-weight: 700; color: #38BDF8;">1. CLIENT BROWSER</div>
          <div style="font-size: 9px; color: #94A3B8; margin-top: 4px;">HTML5 / CSS3 / Vanilla JS</div>
        </div>
        <div style="color: var(--gold); font-weight: bold; font-size: 16px;">➔</div>
        <div style="flex: 1; background: var(--card-dark); border: 1.5px solid #F59E0B; padding: 14px; border-radius: 8px; text-align: center;">
          <div style="font-size: 11px; font-weight: 700; color: #F59E0B;">2. APACHE TOMCAT</div>
          <div style="font-size: 9px; color: #94A3B8; margin-top: 4px;">Jakarta EE 6 Container</div>
        </div>
        <div style="color: var(--gold); font-weight: bold; font-size: 16px;">➔</div>
        <div style="flex: 1; background: var(--card-dark); border: 1.5px solid #EF4444; padding: 14px; border-radius: 8px; text-align: center;">
          <div style="font-size: 11px; font-weight: 700; color: #EF4444;">3. FILTER CHAIN</div>
          <div style="font-size: 9px; color: #94A3B8; margin-top: 4px;">Encoding, CSRF & RBAC</div>
        </div>
        <div style="color: var(--gold); font-weight: bold; font-size: 16px;">➔</div>
        <div style="flex: 1; background: var(--card-dark); border: 1.5px solid #10B981; padding: 14px; border-radius: 8px; text-align: center;">
          <div style="font-size: 11px; font-weight: 700; color: #10B981;">4. SERVLET CONTROLLERS</div>
          <div style="font-size: 9px; color: #94A3B8; margin-top: 4px;">Product, Cart, Order Servlets</div>
        </div>
        <div style="color: var(--gold); font-weight: bold; font-size: 16px;">➔</div>
        <div style="flex: 1; background: var(--card-dark); border: 1.5px solid #8B5CF6; padding: 14px; border-radius: 8px; text-align: center;">
          <div style="font-size: 11px; font-weight: 700; color: #8B5CF6;">5. SERVICES & DAOS</div>
          <div style="font-size: 9px; color: #94A3B8; margin-top: 4px;">OrderService, ProductDAO</div>
        </div>
      </div>

      <!-- Lower Architecture Breakdown -->
      <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 20px; margin-top: 12px; flex: 1;">
        <div style="background: var(--card-dark); border: 1px solid #334155; border-radius: 10px; padding: 18px;">
          <div style="font-size: 12px; font-weight: 700; color: var(--gold); margin-bottom: 8px; letter-spacing: 1px;">LAYER RESPONSIBILITIES</div>
          <ul style="list-style: none; display: flex; flex-direction: column; gap: 8px; font-size: 11.5px; color: #E2E8F0; line-height: 1.45;">
            <li><strong>• Presentation Layer (JSP & JS):</strong> Renders semantic HTML via JSTL and issues asynchronous REST JSON API calls.</li>
            <li><strong>• Security Layer (Filters):</strong> Verifies UTF-8 encoding, security headers, CSRF synchronizer tokens, and RBAC roles.</li>
            <li><strong>• Controller Layer (Servlets):</strong> Intercepts HTTP requests, validates inputs, and maps to services or views.</li>
            <li><strong>• Business Logic Layer (Services):</strong> Enforces stock constraints, discount rules, and transactional rollbacks.</li>
            <li><strong>• Data Access Layer (DAOs):</strong> Parameterized PreparedStatements ensuring 100% defense against SQL injection.</li>
          </ul>
        </div>

        <div style="background: #172554; border: 1.5px solid #60A5FA; border-radius: 10px; padding: 18px;">
          <div style="font-size: 12px; font-weight: 700; color: #93C5FD; margin-bottom: 8px; letter-spacing: 1px;">🗄️ H2 DATABASE ENGINE</div>
          <div style="font-size: 11px; color: #DBEAFE; line-height: 1.45;">
            • Embedded relational storage (jdbc:h2:./data/DJ Mart)<br>
            • Schema: users, products, orders, order_items, cart_items, reviews<br>
            • Foreign Keys with ON DELETE CASCADE rules<br>
            • ACID transactions on order placement with rollback<br>
            • HikariCP connection pool with maximum 10 connections
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">06 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 7: HOME PAGE -->
    <!-- ========================================== -->
    <div class="slide" data-slide="7">
      <div class="slide-header">
        <div class="slide-category">User Interface Showcase</div>
        <h2 class="slide-title">DJ Mart – Home Page</h2>
        <p class="slide-subtitle">Actual Live Screenshot of the DJ Mart Landing & Discovery Experience</p>
        <div class="gold-bar"></div>
      </div>

      <div style="display: grid; grid-template-columns: 1.4fr 1fr; gap: 24px; flex: 1; align-items: stretch;">
        <div class="mockup-frame">
          <div class="mockup-header">
            <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
            <span class="mockup-url">http://localhost:8081/Dj Mart/</span>
          </div>
          ${homeImgBase64 ? `<img src="${homeImgBase64}" alt="DJ Mart Homepage" class="mockup-img">` : '<div>Screenshot Pending</div>'}
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; justify-content: center;">
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">01. STICKY LUXURY NAVBAR</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">Official DJ Mart branding logo, search bar, category shortcuts, live cart count badge, and account menu.</div>
          </div>
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">02. EDITORIAL HERO BANNER</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">"Timeless Elegance, Modern Luxury" showcasing premium lifestyle curation with direct call-to-actions.</div>
          </div>
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">03. CATEGORY SHORTCUTS</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">Instant filter cards for Electronics, Fashion, Home & Kitchen, and Books & Stationery.</div>
          </div>
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">04. FEATURED PRODUCTS GRID</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">High-res product photography, pricing in ₹, star ratings, and one-click "Add to Cart" triggers.</div>
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">07 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 8: PRODUCT BROWSING & DETAILS -->
    <!-- ========================================== -->
    <div class="slide" data-slide="8">
      <div class="slide-header">
        <div class="slide-category">Catalog & Exploration</div>
        <h2 class="slide-title">Product Browsing & Details</h2>
        <p class="slide-subtitle">Actual Live Screenshots of Catalog Search, Filters, and Deep Product Insights</p>
        <div class="gold-bar"></div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; flex: 1;">
        <div style="display: flex; flex-direction: column;">
          <div class="mockup-frame" style="flex: 1;">
            <div class="mockup-header">
              <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
              <span class="mockup-url">http://localhost:8081/Dj Mart/products</span>
            </div>
            ${productsImgBase64 ? `<img src="${productsImgBase64}" alt="DJ Mart Products" class="mockup-img">` : '<div>Screenshot</div>'}
          </div>
          <div style="font-size: 11px; font-weight: 700; color: var(--gold-dark); text-align: center; margin-top: 6px;">
            CATALOG WITH CATEGORY FILTERS & SORTING
          </div>
        </div>

        <div style="display: flex; flex-direction: column;">
          <div class="mockup-frame" style="flex: 1;">
            <div class="mockup-header">
              <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
              <span class="mockup-url">http://localhost:8081/Dj Mart/products/1</span>
            </div>
            ${detailsImgBase64 ? `<img src="${detailsImgBase64}" alt="Product Details" class="mockup-img">` : '<div>Screenshot</div>'}
          </div>
          <div style="font-size: 11px; font-weight: 700; color: var(--gold-dark); text-align: center; margin-top: 6px;">
            DETAILED PRODUCT VIEW & SPECIFICATIONS
          </div>
        </div>
      </div>

      <div style="background: #FFFFFF; border: 1px solid var(--border-light); border-radius: 8px; padding: 10px 16px; margin-top: 10px; font-size: 11.5px; color: var(--text-dark);">
        <strong>KEY CAPABILITIES:</strong> Multi-category filter (Electronics, Fashion, Home) • Price sorting (Low to High / High to Low) • High-res gallery • Live stock indicator badge • Full technical specifications • Direct "Add to Cart" with instant feedback.
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">08 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 9: SHOPPING CART -->
    <!-- ========================================== -->
    <div class="slide" data-slide="9">
      <div class="slide-header">
        <div class="slide-category">Order Management</div>
        <h2 class="slide-title">Shopping Cart</h2>
        <p class="slide-subtitle">Actual Live Screenshot of Interactive Cart with Live Price Recalculation</p>
        <div class="gold-bar"></div>
      </div>

      <div style="display: grid; grid-template-columns: 1.4fr 1fr; gap: 24px; flex: 1; align-items: stretch;">
        <div class="mockup-frame">
          <div class="mockup-header">
            <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
            <span class="mockup-url">http://localhost:8081/Dj Mart/cart</span>
          </div>
          ${cartImgBase64 ? `<img src="${cartImgBase64}" alt="DJ Mart Cart" class="mockup-img">` : '<div>Screenshot</div>'}
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; justify-content: center;">
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">01. SELECTED PRODUCT LIST</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">Displays product thumbnail, name, unit price in ₹, and line-item total clearly for verification.</div>
          </div>
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">02. DYNAMIC QUANTITY STEPPER</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">Users can increment (+) or decrement (-) quantities. JavaScript updates /api/v1/cart/items without page reloads.</div>
          </div>
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">03. INSTANT ITEM REMOVAL</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">One-click delete button triggers instant DOM update and adjusts the navbar cart count badge.</div>
          </div>
          <div class="info-card" style="padding: 12px 16px;">
            <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark);">04. ORDER SUMMARY & CHECKOUT</div>
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">Subtotal, estimated shipping, taxes, and final payable amount leading to the checkout flow.</div>
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">09 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 10: CHECKOUT & ORDER PROCESS -->
    <!-- ========================================== -->
    <div class="slide" data-slide="10">
      <div class="slide-header">
        <div class="slide-category">Order Fulfillment</div>
        <h2 class="slide-title">Checkout & Order Process</h2>
        <p class="slide-subtitle">Actual Live Screenshot of Shipping Capture & ACID Transaction Lifecycle</p>
        <div class="gold-bar"></div>
      </div>

      <div style="display: grid; grid-template-columns: 1.25fr 1fr; gap: 24px; flex: 1; align-items: stretch;">
        <div class="mockup-frame">
          <div class="mockup-header">
            <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
            <span class="mockup-url">http://localhost:8081/Dj Mart/checkout</span>
          </div>
          ${checkoutImgBase64 ? `<img src="${checkoutImgBase64}" alt="DJ Mart Checkout" class="mockup-img">` : '<div>Screenshot</div>'}
        </div>

        <div style="background: #FFFFFF; border: 1.5px solid var(--border-light); border-radius: 12px; padding: 20px; display: flex; flex-direction: column; justify-content: space-between;">
          <div style="font-size: 12px; font-weight: 700; color: var(--gold-dark); letter-spacing: 1px; margin-bottom: 8px;">
            END-TO-END CHECKOUT LIFECYCLE
          </div>
          <div style="display: flex; flex-direction: column; gap: 10px; font-size: 11.5px;">
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <span style="background: var(--obsidian); color: var(--gold); font-weight: 700; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; flex-shrink: 0;">1</span>
              <div><strong>Browse Catalog:</strong> Customer explores products and checks specs.</div>
            </div>
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <span style="background: var(--obsidian); color: var(--gold); font-weight: 700; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; flex-shrink: 0;">2</span>
              <div><strong>Add to Cart:</strong> Item placed into session cart via asynchronous API.</div>
            </div>
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <span style="background: var(--obsidian); color: var(--gold); font-weight: 700; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; flex-shrink: 0;">3</span>
              <div><strong>Review Cart:</strong> Quantities verified and totals confirmed by buyer.</div>
            </div>
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <span style="background: var(--obsidian); color: var(--gold); font-weight: 700; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; flex-shrink: 0;">4</span>
              <div><strong>Delivery Details:</strong> Shipping address & contact info validated.</div>
            </div>
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <span style="background: var(--obsidian); color: var(--gold); font-weight: 700; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; flex-shrink: 0;">5</span>
              <div><strong>Payment Selection:</strong> Payment mode selected (Cash on Delivery).</div>
            </div>
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <span style="background: var(--obsidian); color: var(--gold); font-weight: 700; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; flex-shrink: 0;">6</span>
              <div><strong>Place Order & Commit:</strong> Server executes ACID transaction: verifies stock, decrements inventory, creates order record, and clears cart safely.</div>
            </div>
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">10 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 11: KEY FEATURES -->
    <!-- ========================================== -->
    <div class="slide" data-slide="11">
      <div class="slide-header">
        <div class="slide-category">Platform Capabilities</div>
        <h2 class="slide-title">Key Features of DJ Mart</h2>
        <p class="slide-subtitle">Robust Set of Verified Features Implemented in Current Release</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-4x2">
        <div class="info-card">
          <div class="card-icon">👑</div>
          <div class="card-title">User-Friendly Luxury UI</div>
          <div class="card-desc">Bespoke luxury visual language featuring gold accents, obsidian serif typography, and zero generic styling.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">⚡</div>
          <div class="card-title">Real-Time Search</div>
          <div class="card-desc">Integrated search bar executing instant keyword queries across product titles, categories, and descriptions.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📂</div>
          <div class="card-title">Structured Categories</div>
          <div class="card-desc">Multi-tier categorization organizing Electronics, Fashion, Home Essentials, and Stationery.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🔍</div>
          <div class="card-title">Detailed Product View</div>
          <div class="card-desc">High-res photography, complete specifications, live stock indicators, and price breakdowns.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🛒</div>
          <div class="card-title">Dynamic Interactive Cart</div>
          <div class="card-desc">AJAX quantity adjustments (+/-), live total recalculation, and single-click item removal with zero reload.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📝</div>
          <div class="card-title">Validated Checkout</div>
          <div class="card-desc">Delivery address form validation, payment mode selection, and atomic order placement.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📱</div>
          <div class="card-title">Responsive Design</div>
          <div class="card-desc">Fluid layout, slide-out mobile drawer menu, and touch-optimized buttons (minimum 44x44px).</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🛡️</div>
          <div class="card-title">Enterprise Security</div>
          <div class="card-desc">100% PreparedStatements against SQLi, CSRF synchronizer tokens, and salted BCrypt passwords.</div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">11 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 12: RESPONSIVE DESIGN -->
    <!-- ========================================== -->
    <div class="slide" data-slide="12">
      <div class="slide-header">
        <div class="slide-category">Device Adaptability</div>
        <h2 class="slide-title">Responsive Design</h2>
        <p class="slide-subtitle">Actual Live Screenshots Across Desktop, Tablet, and Mobile Screen Formats</p>
        <div class="gold-bar"></div>
      </div>

      <div style="display: grid; grid-template-columns: 1.4fr 1.1fr 0.8fr; gap: 16px; flex: 1;">
        <!-- Desktop -->
        <div style="display: flex; flex-direction: column;">
          <div class="mockup-frame" style="flex: 1;">
            <div class="mockup-header">
              <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
              <span class="mockup-url">Desktop (1440px)</span>
            </div>
            ${deskImgBase64 ? `<img src="${deskImgBase64}" alt="Desktop View" class="mockup-img">` : '<div>Desktop</div>'}
          </div>
          <div style="font-size: 11px; font-weight: 700; color: var(--gold-dark); text-align: center; margin-top: 6px;">
            🖥️ DESKTOP DISPLAY (1440px+)
          </div>
        </div>

        <!-- Tablet -->
        <div style="display: flex; flex-direction: column;">
          <div class="mockup-frame" style="flex: 1;">
            <div class="mockup-header">
              <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
              <span class="mockup-url">Tablet (768px)</span>
            </div>
            ${tabImgBase64 ? `<img src="${tabImgBase64}" alt="Tablet View" class="mockup-img">` : '<div>Tablet</div>'}
          </div>
          <div style="font-size: 11px; font-weight: 700; color: var(--gold-dark); text-align: center; margin-top: 6px;">
            📱 TABLET DISPLAY (768px)
          </div>
        </div>

        <!-- Mobile -->
        <div style="display: flex; flex-direction: column;">
          <div class="mockup-frame" style="flex: 1;">
            <div class="mockup-header">
              <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
              <span class="mockup-url">Mobile (390px)</span>
            </div>
            ${mobImgBase64 ? `<img src="${mobImgBase64}" alt="Mobile View" class="mockup-img">` : '<div>Mobile</div>'}
          </div>
          <div style="font-size: 11px; font-weight: 700; color: var(--gold-dark); text-align: center; margin-top: 6px;">
            📲 MOBILE DISPLAY (390px)
          </div>
        </div>
      </div>

      <div style="background: #FFFFFF; border: 1px solid var(--border-light); border-radius: 8px; padding: 10px 16px; margin-top: 10px; font-size: 11.5px; color: var(--text-dark);">
        <strong>CONSISTENT VISUAL IDENTITY:</strong> The luxury aesthetic—including gold accents, serif typography, high-res photography, and smooth transitions—is preserved uniformly whether accessed from a 4K monitor, iPad, or mobile phone.
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">12 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 13: ADVANTAGES -->
    <!-- ========================================== -->
    <div class="slide" data-slide="13">
      <div class="slide-header">
        <div class="slide-category">Value Proposition</div>
        <h2 class="slide-title">Advantages of DJ Mart</h2>
        <p class="slide-subtitle">Tangible Benefits Delivered to Shoppers and Platform Operators</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-4x2">
        <div class="info-card">
          <div class="card-icon">💡</div>
          <div class="card-title">Ease of Use</div>
          <div class="card-desc">Clear layout, self-explanatory navigation, and zero prior training required for any customer.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">⏱️</div>
          <div class="card-title">Saves Shopping Time</div>
          <div class="card-desc">Eliminates travel and checkout queues; complete purchases in under two minutes.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🌐</div>
          <div class="card-title">24/7 Online Access</div>
          <div class="card-desc">Uninterrupted online storefront available anytime from anywhere with internet connectivity.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🔍</div>
          <div class="card-title">Transparent Information</div>
          <div class="card-desc">Accurate prices, live stock status indicators, and detailed technical specifications.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🚀</div>
          <div class="card-title">Lightweight & Blazing Fast</div>
          <div class="card-desc">Pure Vanilla JS and optimized Jakarta Servlets avoid heavy client framework bloat.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📱</div>
          <div class="card-title">Multi-Device Access</div>
          <div class="card-desc">Flawless responsive behavior across desktop computers, tablets, and smartphones.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">🔒</div>
          <div class="card-title">Bank-Grade Security</div>
          <div class="card-desc">Salted BCrypt hashing, CSRF synchronizer tokens, and SQL injection prevention.</div>
        </div>
        <div class="info-card">
          <div class="card-icon">📈</div>
          <div class="card-title">Modular & Scalable</div>
          <div class="card-desc">Layered MVC architecture enables simple integration of future enterprise modules.</div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">13 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 14: FUTURE ENHANCEMENTS -->
    <!-- ========================================== -->
    <div class="slide" data-slide="14">
      <div class="slide-header">
        <div class="slide-category">Strategic Roadmap</div>
        <h2 class="slide-title">Future Enhancements</h2>
        <p class="slide-subtitle">Clearly Demarcated Features Planned for Enterprise Scale</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-4x2">
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">💳 Payment Gateway</div>
          <div class="card-desc">Integration with Razorpay, Stripe, and UPI for automated real-time digital payments.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">🚚 Live Order Tracking</div>
          <div class="card-desc">Real-time parcel status tracker with courier partner integration and live updates.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">❤️ Customer Wishlist</div>
          <div class="card-desc">Bookmark favorite luxury products for future purchases and receive price drop alerts.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">🤖 AI Recommendations</div>
          <div class="card-desc">Machine learning models suggesting personalized products based on buyer affinities.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">📊 Seller Management</div>
          <div class="card-desc">Automated restocking alerts, barcode scanning, and bulk CSV catalog uploads.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">🔔 SMS / Email Alerts</div>
          <div class="card-desc">Automated order receipts, dispatch updates, and delivery confirmations.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">📲 Native Mobile Apps</div>
          <div class="card-desc">Cross-platform iOS and Android apps built with Flutter for native mobile experience.</div>
        </div>
        <div class="info-card" style="border-color: #CBD5E1;">
          <div style="display: inline-block; background: #FEF3C7; border: 1px solid #F59E0B; border-radius: 4px; padding: 2px 6px; font-size: 9px; font-weight: 700; color: #B45309; margin-bottom: 8px;">FUTURE ROADMAP</div>
          <div class="card-title">📈 Advanced Analytics</div>
          <div class="card-desc">Predictive business intelligence with sales forecasting and customer churn metrics.</div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">14 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 15: CONCLUSION -->
    <!-- ========================================== -->
    <div class="slide" data-slide="15">
      <div class="slide-header">
        <div class="slide-category">Project Summary</div>
        <h2 class="slide-title">Conclusion</h2>
        <p class="slide-subtitle">Summary of Project Achievements & Demonstration Outcome</p>
        <div class="gold-bar"></div>
      </div>

      <div class="grid-2col" style="gap: 20px;">
        <div class="info-card" style="padding: 24px;">
          <div class="card-icon">🏆</div>
          <div style="font-size: 17px; font-weight: 700; color: var(--text-dark); margin-bottom: 8px;">Successful Development with Antigravity</div>
          <div style="font-size: 13.5px; line-height: 1.6; color: var(--text-muted);">
            DJ MART successfully demonstrates the rapid, robust development of a modern e-commerce web application using Google Antigravity, achieving clean architecture and enterprise code quality.
          </div>
        </div>

        <div class="info-card" style="padding: 24px;">
          <div class="card-icon">🎯</div>
          <div style="font-size: 17px; font-weight: 700; color: var(--text-dark); margin-bottom: 8px;">Complete End-to-End Shopping Experience</div>
          <div style="font-size: 13.5px; line-height: 1.6; color: var(--text-muted);">
            Delivered an operational shopping workflow from catalog exploration, real-time search, and category filtering to cart adjustments, address capture, and atomic order placement.
          </div>
        </div>

        <div class="info-card" style="padding: 24px;">
          <div class="card-icon">⚙️</div>
          <div style="font-size: 17px; font-weight: 700; color: var(--text-dark); margin-bottom: 8px;">Demonstration of Modern Web Concepts</div>
          <div style="font-size: 13.5px; line-height: 1.6; color: var(--text-muted);">
            Proved the effectiveness of Jakarta EE 6 Servlets, JSTL server rendering, Vanilla JS API clients, H2 SQL database transactions, and bank-grade OWASP security protections.
          </div>
        </div>

        <div class="info-card" style="padding: 24px;">
          <div class="card-icon">🚀</div>
          <div style="font-size: 17px; font-weight: 700; color: var(--text-dark); margin-bottom: 8px;">Extensible & Production-Ready Foundation</div>
          <div style="font-size: 13.5px; line-height: 1.6; color: var(--text-muted);">
            The layered MVC codebase adheres to industry separation-of-concerns principles, establishing a solid foundation primed for live payment gateways and multi-cloud deployment.
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">15 / 16</div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- SLIDE 16: THANK YOU -->
    <!-- ========================================== -->
    <div class="slide dark" data-slide="16" style="text-align: center; justify-content: space-between;">
      <div style="padding-top: 20px;">
        ${logoBase64 ? `<img src="${logoBase64}" alt="DJ Mart Logo" style="height: 72px; border-radius: 4px; border: 1px solid rgba(197, 168, 128, 0.3);">` : ''}
      </div>

      <div style="margin: auto 0;">
        <h1 style="font-family: 'Playfair Display', Georgia, serif; font-size: 64px; font-weight: 700; color: var(--gold); letter-spacing: 2px; margin-bottom: 12px;">
          THANK YOU
        </h1>
        <h2 style="font-size: 32px; font-weight: 600; color: #FFFFFF; margin-bottom: 16px;">
          Questions & Answers
        </h2>
        <p style="font-family: 'Playfair Display', serif; font-style: italic; font-size: 18px; color: var(--gold-light); margin-bottom: 30px;">
          "Smart, Simple & Convenient Online Shopping"
        </p>

        <div style="display: inline-block; background: var(--card-dark); border: 1.5px solid #334155; border-radius: 12px; padding: 20px 40px; text-align: center;">
          <div style="font-size: 15px; font-weight: 700; color: var(--gold); margin-bottom: 6px;">
            DJ MART — E-Commerce Website
          </div>
          <div style="font-size: 12px; color: #94A3B8; line-height: 1.6;">
            Developed using Antigravity<br>
            College Project Presentation & Viva<br>
            Academic Year 2025 – 2026
          </div>
        </div>
      </div>

      <div class="slide-footer">
        <div class="footer-brand">DJ MART — E-Commerce Website</div>
        <div class="footer-tool">Developed using Antigravity</div>
        <div class="slide-num">16 / 16</div>
      </div>
    </div>

  </div>

  <!-- Navigation Controls -->
  <div class="nav-controls">
    <button class="nav-btn" id="prev-btn" title="Previous Slide (Left Arrow)">❮</button>
    <button class="nav-btn" id="next-btn" title="Next Slide (Right Arrow / Space)">❯</button>
    <button class="nav-btn" id="fullscreen-btn" title="Toggle Fullscreen (F)">⛶</button>
  </div>

  <div class="key-hint">
    <span>← / →</span> Navigate
    <span>Space</span> Next
    <span>F</span> Fullscreen
  </div>

  <script>
    const slides = document.querySelectorAll('.slide');
    const progressBar = document.getElementById('progress-bar');
    let currentIndex = 0;

    function showSlide(index) {
      if (index < 0) index = 0;
      if (index >= slides.length) index = slides.length - 1;

      slides.forEach((slide, idx) => {
        slide.classList.remove('active', 'prev');
        if (idx === index) {
          slide.classList.add('active');
        } else if (idx < index) {
          slide.classList.add('prev');
        }
      });

      currentIndex = index;
      const progress = ((currentIndex + 1) / slides.length) * 100;
      progressBar.style.width = progress + '%';
    }

    document.getElementById('next-btn').addEventListener('click', () => showSlide(currentIndex + 1));
    document.getElementById('prev-btn').addEventListener('click', () => showSlide(currentIndex - 1));

    document.getElementById('fullscreen-btn').addEventListener('click', () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch(() => {});
      } else {
        document.exitFullscreen().catch(() => {});
      }
    });

    document.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'PageDown') {
        e.preventDefault();
        showSlide(currentIndex + 1);
      } else if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
        e.preventDefault();
        showSlide(currentIndex - 1);
      } else if (e.key === 'Home') {
        showSlide(0);
      } else if (e.key === 'End') {
        showSlide(slides.length - 1);
      } else if (e.key.toLowerCase() === 'f') {
        if (!document.fullscreenElement) {
          document.documentElement.requestFullscreen().catch(() => {});
        } else {
          document.exitFullscreen().catch(() => {});
        }
      }
    });

    // Touch swipe support
    let touchStartX = 0;
    document.addEventListener('touchstart', e => { touchStartX = e.changedTouches[0].screenX; });
    document.addEventListener('touchend', e => {
      let touchEndX = e.changedTouches[0].screenX;
      if (touchStartX - touchEndX > 50) showSlide(currentIndex + 1);
      if (touchEndX - touchStartX > 50) showSlide(currentIndex - 1);
    });

    showSlide(0);
  </script>
</body>
</html>
`;

const htmlFilePath = path.join(__dirname, '..', 'DJ_MART_Presentation.html');
fs.writeFileSync(htmlFilePath, htmlContent, 'utf8');
console.log(`Interactive presentation written to: ${htmlFilePath} (${(htmlContent.length / 1024).toFixed(1)} KB)`);

// Also compile to presentation PDF via Edge
const edgePaths = [
  'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
  'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe'
];
const edgePath = edgePaths.find(p => fs.existsSync(p));

if (edgePath) {
  const targetPdf = path.join(__dirname, '..', 'DJ_MART_Project_Presentation.pdf');
  const brainPdf = 'C:\\Users\\djnir\\.gemini\\antigravity\\brain\\6bc14fa7-10f4-4a85-94d1-594581264fbb\\DJ_MART_Project_Presentation.pdf';

  console.log('Compiling presentation to PDF using Edge headless...');
  const cmd = `"${edgePath}" --headless --disable-gpu --run-all-compositor-stages-before-draw --print-to-pdf="${targetPdf}" "${htmlFilePath}"`;
  try {
    execSync(cmd, { stdio: 'inherit' });
    console.log(`Presentation PDF compiled at: ${targetPdf}`);
    if (fs.existsSync(targetPdf)) {
      fs.copyFileSync(targetPdf, brainPdf);
      console.log(`Copied PDF to Brain Artifact: ${brainPdf}`);
    }
  } catch (err) {
    console.error('Error compiling PDF:', err);
  }
}
