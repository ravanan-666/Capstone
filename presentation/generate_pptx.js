// presentation/generate_pptx.js
// Generates the professional 16-slide PowerPoint presentation for DJ MART

const PptxGenJS = require('./node_modules/pptxgenjs');
const path = require('path');
const fs = require('fs');

console.log('Initializing PPTX generation for DJ MART...');

const pptx = new PptxGenJS();
pptx.defineLayout({ name: 'CUSTOM_16_9', width: 13.333, height: 7.5 });
pptx.layout = 'CUSTOM_16_9';

// Color Palette Constants
const COLORS = {
  OBSIDIAN: '0F172A',
  DEEP_BG: '0B1120',
  CARD_DARK: '1E293B',
  TEXT_LIGHT: 'FFFFFF',
  TEXT_MUTED_LIGHT: '94A3B8',
  GOLD: 'C5A880',
  GOLD_DARK: '9A7B4F',
  GOLD_LIGHT: 'E8D5B5',
  IVORY_BG: 'F8FAFC',
  CARD_LIGHT: 'FFFFFF',
  TEXT_DARK: '0F172A',
  TEXT_MUTED_DARK: '64748B',
  BORDER_LIGHT: 'E2E8F0',
  BORDER_GOLD: 'C5A880',
  SUCCESS: '16A34A',
  ACCENT_BLUE: '2563EB'
};

const screenshotDir = path.join(__dirname, 'screenshots');
const logoPath = path.join(__dirname, '..', 'src', 'main', 'webapp', 'static', 'images', 'dj_mart_logo.jpg');

// Helper: Add Standard Header
function addHeader(slide, category, title, subtitle, isDark = false) {
  // Category pill / tracker
  slide.addText(category.toUpperCase(), {
    x: 0.8,
    y: 0.45,
    w: 8.0,
    h: 0.3,
    fontSize: 10,
    fontFace: 'Segoe UI',
    bold: true,
    color: isDark ? COLORS.GOLD : COLORS.GOLD_DARK,
    letterSpacing: 2
  });

  // Main Title
  slide.addText(title, {
    x: 0.8,
    y: 0.75,
    w: 10.0,
    h: 0.55,
    fontSize: 24,
    fontFace: 'Georgia',
    bold: true,
    color: isDark ? COLORS.TEXT_LIGHT : COLORS.TEXT_DARK
  });

  // Subtitle
  slide.addText(subtitle, {
    x: 0.8,
    y: 1.3,
    w: 10.0,
    h: 0.35,
    fontSize: 12,
    fontFace: 'Segoe UI',
    color: isDark ? COLORS.TEXT_MUTED_LIGHT : COLORS.TEXT_MUTED_DARK
  });

  // Gold accent bar
  slide.addShape(pptx.shapes.RECTANGLE, {
    x: 0.8,
    y: 1.7,
    w: 1.5,
    h: 0.04,
    fill: { color: COLORS.GOLD },
    line: { color: COLORS.GOLD }
  });
}

// Helper: Add Standard Footer
function addFooter(slide, currentSlide, totalSlides = 16, isDark = false) {
  // Left: Project Name
  slide.addText('DJ MART — E-Commerce Website', {
    x: 0.8,
    y: 7.0,
    w: 4.5,
    h: 0.3,
    fontSize: 9,
    fontFace: 'Segoe UI',
    color: isDark ? COLORS.TEXT_MUTED_LIGHT : COLORS.TEXT_MUTED_DARK
  });

  // Center: Developed with Antigravity
  slide.addText('Developed using Antigravity', {
    x: 4.8,
    y: 7.0,
    w: 3.7,
    h: 0.3,
    fontSize: 9,
    fontFace: 'Segoe UI',
    align: 'center',
    bold: true,
    color: isDark ? COLORS.GOLD : COLORS.GOLD_DARK
  });

  // Right: Slide Number
  slide.addText(`${String(currentSlide).padStart(2, '0')} / ${totalSlides}`, {
    x: 10.5,
    y: 7.0,
    w: 2.0,
    h: 0.3,
    fontSize: 9,
    fontFace: 'Segoe UI',
    align: 'right',
    bold: true,
    color: isDark ? COLORS.TEXT_MUTED_LIGHT : COLORS.TEXT_MUTED_DARK
  });
}

// ==========================================
// SLIDE 1: TITLE
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.DEEP_BG };

  // Decorative gold corner accent
  slide.addShape(pptx.shapes.RECTANGLE, { x: 0, y: 0, w: 13.333, h: 0.12, fill: { color: COLORS.GOLD } });

  // Official Logo if present
  if (fs.existsSync(logoPath)) {
    slide.addImage({ path: logoPath, x: 0.9, y: 0.8, w: 2.4, h: 1.0, sizing: { type: 'contain' } });
  }

  // Brand Badge
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.9,
    y: 2.0,
    w: 3.4,
    h: 0.35,
    fill: { color: '1E293B' },
    line: { color: COLORS.GOLD, width: 1 },
    rectRadius: 0.1
  });
  slide.addText('COLLEGE PROJECT PRESENTATION & VIVA', {
    x: 0.9,
    y: 2.0,
    w: 3.4,
    h: 0.35,
    fontSize: 9,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD,
    align: 'center'
  });

  // Main Title & Subtitle
  slide.addText('DJ MART', {
    x: 0.85,
    y: 2.5,
    w: 7.0,
    h: 1.1,
    fontSize: 54,
    fontFace: 'Georgia',
    bold: true,
    color: COLORS.GOLD
  });

  slide.addText('E-Commerce Website', {
    x: 0.9,
    y: 3.65,
    w: 7.0,
    h: 0.6,
    fontSize: 28,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.TEXT_LIGHT
  });

  slide.addText('"Smart, Simple & Convenient Online Shopping"', {
    x: 0.9,
    y: 4.35,
    w: 6.8,
    h: 0.45,
    fontSize: 15,
    fontFace: 'Georgia',
    italic: true,
    color: COLORS.GOLD_LIGHT
  });

  // Tool badge: Developed using Antigravity
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.9,
    y: 5.1,
    w: 3.8,
    h: 0.45,
    fill: { color: '162238' },
    line: { color: '38BDF8', width: 1 },
    rectRadius: 0.1
  });
  slide.addText('⚡ Developed using Antigravity', {
    x: 0.9,
    y: 5.1,
    w: 3.8,
    h: 0.45,
    fontSize: 11,
    fontFace: 'Segoe UI',
    bold: true,
    color: '38BDF8',
    align: 'center'
  });

  // Right Side: Student & Academic Info Card
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 7.8,
    y: 1.8,
    w: 4.7,
    h: 4.7,
    fill: { color: COLORS.CARD_DARK },
    line: { color: '334155', width: 1.5 },
    rectRadius: 0.15
  });

  slide.addText('PROJECT DETAILS', {
    x: 8.2,
    y: 2.1,
    w: 3.9,
    h: 0.35,
    fontSize: 11,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD,
    letterSpacing: 2
  });

  const projectDetails = [
    { label: 'Project Name', val: 'DJ MART E-Commerce' },
    { label: 'Student Name', val: 'Candidate / Student Name' },
    { label: 'Register Number', val: '[Register Number]' },
    { label: 'Department', val: 'Dept. of Computer Science & Engg.' },
    { label: 'Degree & Year', val: 'B.E. / B.Tech (Final Year)' },
    { label: 'Institution', val: '[Engineering College / University]' },
    { label: 'Academic Year', val: '2025 – 2026' }
  ];

  let curY = 2.6;
  projectDetails.forEach(item => {
    slide.addText(item.label.toUpperCase(), {
      x: 8.2,
      y: curY,
      w: 3.9,
      h: 0.22,
      fontSize: 8.5,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_LIGHT
    });
    slide.addText(item.val, {
      x: 8.2,
      y: curY + 0.2,
      w: 3.9,
      h: 0.3,
      fontSize: 11,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_LIGHT
    });
    curY += 0.55;
  });

  addFooter(slide, 1, 16, true);
}

// ==========================================
// SLIDE 2: INTRODUCTION
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Project Overview', 'Introduction', 'A Modern Online Shopping Platform for Effortless Retail Convenience');

  const introCards = [
    {
      title: 'Online Product Browsing',
      desc: 'Intuitive catalog exploration enabling users to effortlessly discover luxury, electronic, fashion, and everyday products from any device.',
      icon: '🛍️'
    },
    {
      title: 'Real-Time Product Discovery',
      desc: 'Instant keyword search coupled with dynamic category filtering allowing shoppers to find target items in milliseconds.',
      icon: '🔍'
    },
    {
      title: 'Comprehensive Product Details',
      desc: 'Rich product presentation featuring high-resolution images, transparent pricing in ₹, live inventory badges, and full descriptions.',
      icon: '📋'
    },
    {
      title: 'Interactive Shopping Cart',
      desc: 'Seamless quantity adjustments (+/-), instant price recalculation, item removal, and persistent session storage without page reloads.',
      icon: '🛒'
    },
    {
      title: 'Streamlined Online Ordering',
      desc: 'Frictionless checkout experience with complete delivery address capture, payment preference, and instant order confirmation.',
      icon: '📦'
    },
    {
      title: 'User-Friendly & Responsive UI',
      desc: 'A premium luxury design aesthetic crafted with mobile-first responsiveness, ensuring a delightful experience on smartphones, tablets, and desktops.',
      icon: '✨'
    }
  ];

  introCards.forEach((card, idx) => {
    const col = idx % 3;
    const row = Math.floor(idx / 3);
    const x = 0.8 + col * 3.95;
    const y = 2.0 + row * 2.3;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 3.75,
      h: 2.1,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1.2 },
      rectRadius: 0.1
    });

    slide.addText(`${card.icon}  ${card.title}`, {
      x: x + 0.25,
      y: y + 0.25,
      w: 3.25,
      h: 0.45,
      fontSize: 13,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(card.desc, {
      x: x + 0.25,
      y: y + 0.75,
      w: 3.25,
      h: 1.15,
      fontSize: 10,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 16
    });
  });

  addFooter(slide, 2, 16);
}

// ==========================================
// SLIDE 3: PROBLEM STATEMENT
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Market Challenges & Need', 'Problem Statement', 'Addressing Traditional Retail Constraints with a Frictionless Digital Solution');

  // Left Box: Traditional Shopping Problems
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.8,
    y: 2.0,
    w: 5.7,
    h: 4.7,
    fill: { color: 'FEF2F2' },
    line: { color: 'FCA5A5', width: 1.5 },
    rectRadius: 0.12
  });

  slide.addText('⚠️ TRADITIONAL SHOPPING PROBLEMS', {
    x: 1.1,
    y: 2.2,
    w: 5.1,
    h: 0.35,
    fontSize: 12,
    fontFace: 'Segoe UI',
    bold: true,
    color: '991B1B'
  });

  const problems = [
    { title: 'Mandatory Store Visits:', desc: 'Requires physical commute, fuel costs, traffic congestion, and parking hassles.' },
    { title: 'Time-Consuming Process:', desc: 'Hours spent walking physical aisles and standing in long billing queues.' },
    { title: 'Difficult Product Comparison:', desc: 'Hard to compare alternative brands, technical specifications, and fair pricing.' },
    { title: 'Restricted Operating Hours:', desc: 'Inaccessible outside standard retail hours; no late-night or holiday access.' },
    { title: 'Uncertain Inventory Visibility:', desc: 'Customers travel to stores only to discover desired products are out of stock.' }
  ];

  let pY = 2.65;
  problems.forEach(p => {
    slide.addText(`• ${p.title} ${p.desc}`, {
      x: 1.1,
      y: pY,
      w: 5.1,
      h: 0.68,
      fontSize: 9.5,
      fontFace: 'Segoe UI',
      color: '7F1D1D',
      lineSpacing: 15
    });
    pY += 0.76;
  });

  // Right Box: The DJ Mart Digital Solution
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 6.8,
    y: 2.0,
    w: 5.7,
    h: 4.7,
    fill: { color: 'F0FDF4' },
    line: { color: '86EFAC', width: 1.5 },
    rectRadius: 0.12
  });

  slide.addText('✅ THE DJ MART DIGITAL SOLUTION', {
    x: 7.1,
    y: 2.2,
    w: 5.1,
    h: 0.35,
    fontSize: 12,
    fontFace: 'Segoe UI',
    bold: true,
    color: '166534'
  });

  const solutions = [
    { title: 'Universal 24/7 Access:', desc: 'Shop from anywhere, anytime using any smartphone, tablet, or laptop.' },
    { title: 'Sub-Second Checkout:', desc: 'Browse, add to cart, and place orders in under two minutes without queues.' },
    { title: 'Transparent Comparison:', desc: 'Instant access to prices, specifications, high-res images, and reviews.' },
    { title: 'Always Open & Accessible:', desc: 'Uninterrupted shopping experience available round-the-clock, 365 days a year.' },
    { title: 'Live Inventory Visibility:', desc: 'Real-time stock indicators prevent ordering out-of-stock items.' }
  ];

  let sY = 2.65;
  solutions.forEach(s => {
    slide.addText(`✔ ${s.title} ${s.desc}`, {
      x: 7.1,
      y: sY,
      w: 5.1,
      h: 0.68,
      fontSize: 9.5,
      fontFace: 'Segoe UI',
      color: '14532D',
      lineSpacing: 15
    });
    sY += 0.76;
  });

  addFooter(slide, 3, 16);
}

// ==========================================
// SLIDE 4: PROJECT OBJECTIVES
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Project Scope & Goals', 'Project Objectives', 'Key Engineering & Business Goals Established for DJ Mart');

  const objectives = [
    { num: '01', title: 'Develop Modern Web App', desc: 'Build a secure, full-stack e-commerce platform using Antigravity AI engineering.' },
    { num: '02', title: 'Intuitive Product Browsing', desc: 'Deliver an elegant catalog structure across diverse luxury, tech, and lifestyle goods.' },
    { num: '03', title: 'Real-Time Search & Query', desc: 'Implement rapid keyword search with instantaneous product filtering.' },
    { num: '04', title: 'Structured Categorization', desc: 'Organize catalog into distinct categories (Electronics, Fashion, Home, Books).' },
    { num: '05', title: 'Dynamic Cart Management', desc: 'Enable adding items, quantity updates (+/-), and real-time total recalculation.' },
    { num: '06', title: 'Frictionless Checkout', desc: 'Provide an easy order process with address validation and order confirmation.' },
    { num: '07', title: 'Responsive User Interface', desc: 'Create a mobile-first visual design that adapts effortlessly across all screen sizes.' },
    { num: '08', title: 'Elevate Shopping Experience', desc: 'Offer a distinctive, luxury brand identity that surpasses generic templates.' }
  ];

  objectives.forEach((obj, idx) => {
    const col = idx % 4;
    const row = Math.floor(idx / 4);
    const x = 0.8 + col * 2.95;
    const y = 2.0 + row * 2.3;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 2.8,
      h: 2.1,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1.2 },
      rectRadius: 0.1
    });

    slide.addText(obj.num, {
      x: x + 0.2,
      y: y + 0.15,
      w: 1.0,
      h: 0.35,
      fontSize: 16,
      fontFace: 'Georgia',
      bold: true,
      color: COLORS.GOLD_DARK
    });

    slide.addText(obj.title, {
      x: x + 0.2,
      y: y + 0.55,
      w: 2.4,
      h: 0.45,
      fontSize: 11,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(obj.desc, {
      x: x + 0.2,
      y: y + 1.05,
      w: 2.4,
      h: 0.9,
      fontSize: 9,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 15
    });
  });

  addFooter(slide, 4, 16);
}

// ==========================================
// SLIDE 5: TECHNOLOGIES & TOOLS USED
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Engineering Stack', 'Technologies & Tools Used', 'Only Technologies Actually Implemented & Verified in DJ Mart');

  const techStack = [
    {
      category: 'AI & DEVELOPMENT ENVIRONMENT',
      name: 'Google Antigravity',
      role: 'Core Development Platform',
      details: 'Advanced agentic AI coding assistant utilized for full-stack architecture design, Java code implementation, debugging, and verification.',
      badgeColor: '0284C7',
      badgeBg: 'E0F2FE'
    },
    {
      category: 'FRONTEND TECHNOLOGIES',
      name: 'HTML5, CSS3 & Vanilla JS',
      role: 'Client-Side Interface',
      details: 'Custom luxury design system with CSS custom properties (variables), native ES6+ JavaScript API client, and zero heavy frontend dependencies.',
      badgeColor: 'D97706',
      badgeBg: 'FEF3C7'
    },
    {
      category: 'SERVER-SIDE PRESENTATION',
      name: 'Jakarta Server Pages (JSP)',
      role: 'View Layer & Templates',
      details: 'Jakarta JSP 3.1 & JSTL 3.0 standard tag library for clean, secure server-rendered pages with XSS escaping and modular header/footer components.',
      badgeColor: '16A34A',
      badgeBg: 'DCFCE7'
    },
    {
      category: 'BACKEND ARCHITECTURE',
      name: 'Jakarta Servlet 6.0 & Java 17',
      role: 'Server Controllers & Services',
      details: 'High-performance Java enterprise web container architecture implementing Layered MVC (Servlets, Filter Chain, Services, and DAOs).',
      badgeColor: 'DC2626',
      badgeBg: 'FEE2E2'
    },
    {
      category: 'DATABASE & RELATIONAL STORAGE',
      name: 'H2 Relational Database Engine',
      role: 'Embedded SQL Data Store',
      details: 'Embedded SQL database (jdbc:h2:./data/DJ Mart) with ACID transactions, foreign keys, CHECK constraints, and automated schema migration.',
      badgeColor: '7C3AED',
      badgeBg: 'EDE9FE'
    },
    {
      category: 'CONNECTION POOL & SECURITY',
      name: 'HikariCP 5.1 & jBCrypt Security',
      role: 'Performance & Cryptography',
      details: 'Blazing-fast connection pooling via HikariCP, salted BCrypt password hashing, and CSRF synchronizer token protection across all state mutations.',
      badgeColor: '0D9488',
      badgeBg: 'CCFBF1'
    }
  ];

  techStack.forEach((tech, idx) => {
    const col = idx % 3;
    const row = Math.floor(idx / 3);
    const x = 0.8 + col * 3.95;
    const y = 2.0 + row * 2.35;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 3.75,
      h: 2.15,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1.2 },
      rectRadius: 0.1
    });

    slide.addText(tech.category, {
      x: x + 0.25,
      y: y + 0.2,
      w: 3.25,
      h: 0.25,
      fontSize: 8,
      fontFace: 'Segoe UI',
      bold: true,
      color: tech.badgeColor,
      letterSpacing: 1
    });

    slide.addText(tech.name, {
      x: x + 0.25,
      y: y + 0.45,
      w: 3.25,
      h: 0.35,
      fontSize: 13,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(tech.role, {
      x: x + 0.25,
      y: y + 0.82,
      w: 3.25,
      h: 0.25,
      fontSize: 9.5,
      fontFace: 'Segoe UI',
      italic: true,
      color: COLORS.GOLD_DARK
    });

    slide.addText(tech.details, {
      x: x + 0.25,
      y: y + 1.12,
      w: 3.25,
      h: 0.9,
      fontSize: 8.8,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 14
    });
  });

  addFooter(slide, 5, 16);
}

// ==========================================
// SLIDE 6: SYSTEM ARCHITECTURE
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.DEEP_BG };
  addHeader(slide, 'System Design & Structure', 'System Architecture', 'Multi-Tier Layered MVC Architecture & Complete Request Lifecycle', true);

  const archNodes = [
    { name: '1. USER / CLIENT BROWSER', sub: 'HTML5, CSS3, Vanilla JS (Fetch API)', color: '38BDF8', x: 0.8 },
    { name: '2. APACHE TOMCAT 10.1', sub: 'Jakarta EE 6 Web Container', color: 'F59E0B', x: 3.2 },
    { name: '3. FILTER CHAIN', sub: 'Encoding, Security, CSRF & Auth', color: 'EF4444', x: 5.6 },
    { name: '4. SERVLET CONTROLLERS', sub: 'Product, Cart, Order, Auth Servlets', color: '10B981', x: 8.0 },
    { name: '5. SERVICE & DAO LAYER', sub: 'OrderService, ProductDAO, HikariCP', color: '8B5CF6', x: 10.4 }
  ];

  archNodes.forEach((node, i) => {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: node.x,
      y: 2.1,
      w: 2.1,
      h: 1.3,
      fill: { color: COLORS.CARD_DARK },
      line: { color: node.color, width: 1.5 },
      rectRadius: 0.08
    });

    slide.addText(node.name, {
      x: node.x + 0.1,
      y: 2.25,
      w: 1.9,
      h: 0.45,
      fontSize: 9,
      fontFace: 'Segoe UI',
      bold: true,
      align: 'center',
      color: COLORS.TEXT_LIGHT
    });

    slide.addText(node.sub, {
      x: node.x + 0.1,
      y: 2.7,
      w: 1.9,
      h: 0.55,
      fontSize: 8,
      fontFace: 'Segoe UI',
      align: 'center',
      color: COLORS.TEXT_MUTED_LIGHT
    });

    // Arrow to next
    if (i < archNodes.length - 1) {
      slide.addText('➔', {
        x: node.x + 2.1,
        y: 2.5,
        w: 0.3,
        h: 0.4,
        fontSize: 14,
        fontFace: 'Segoe UI',
        bold: true,
        align: 'center',
        color: COLORS.GOLD
      });
    }
  });

  // Downward connection to Database
  slide.addText('▼  [HikariCP Connection Pool]', {
    x: 8.5,
    y: 3.5,
    w: 4.0,
    h: 0.35,
    fontSize: 10,
    fontFace: 'Segoe UI',
    bold: true,
    align: 'center',
    color: COLORS.GOLD
  });

  // Database Tier Box
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 8.3,
    y: 3.9,
    w: 4.2,
    h: 2.6,
    fill: { color: '172554' },
    line: { color: '60A5FA', width: 1.5 },
    rectRadius: 0.1
  });

  slide.addText('🗄️ H2 RELATIONAL DATABASE (SQL ENGINE)', {
    x: 8.5,
    y: 4.1,
    w: 3.8,
    h: 0.35,
    fontSize: 11,
    fontFace: 'Segoe UI',
    bold: true,
    color: '93C5FD'
  });

  slide.addText('• Schema: users, products, orders, order_items, cart_items, reviews\n• Embedded Storage: jdbc:h2:./data/DJ Mart;AUTO_SERVER=TRUE\n• Data Integrity: Strict Foreign Keys & CHECK constraints\n• ACID Transactions: Order placement stock decrement rollback protection\n• High-Throughput: HikariCP connection pool with 10 max pool size', {
    x: 8.5,
    y: 4.5,
    w: 3.8,
    h: 1.8,
    fontSize: 8.5,
    fontFace: 'Segoe UI',
    color: COLORS.TEXT_LIGHT,
    lineSpacing: 15
  });

  // Left Architectural Details Box
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.8,
    y: 3.9,
    w: 7.2,
    h: 2.6,
    fill: { color: COLORS.CARD_DARK },
    line: { color: '334155', width: 1 },
    rectRadius: 0.1
  });

  slide.addText('LAYER RESPONSIBILITIES & SEPARATION OF CONCERNS', {
    x: 1.1,
    y: 4.1,
    w: 6.6,
    h: 0.3,
    fontSize: 11,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD
  });

  const layerDesc = [
    { layer: 'Presentation Layer (JSP & JS):', desc: 'Renders dynamic HTML using JSTL and initiates asynchronous JSON API requests.' },
    { layer: 'Security Layer (Jakarta Filters):', desc: 'Validates UTF-8 encoding, security headers, CSRF synchronizer tokens, and RBAC authentication.' },
    { layer: 'Controller Layer (Servlets):', desc: 'Intercepts HTTP requests, validates inputs, invokes services, and routes to JSPs or JSON.' },
    { layer: 'Business Logic Layer (Services):', desc: 'Enforces business rules, inventory availability, price calculation, and transactional consistency.' },
    { layer: 'Data Access Layer (DAOs):', desc: 'Executes parameterized JDBC PreparedStatements ensuring 100% defense against SQL injection.' }
  ];

  let lY = 4.45;
  layerDesc.forEach(l => {
    slide.addText(`• ${l.layer} ${l.desc}`, {
      x: 1.1,
      y: lY,
      w: 6.6,
      h: 0.36,
      fontSize: 8.5,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_LIGHT,
      lineSpacing: 13
    });
    lY += 0.38;
  });

  addFooter(slide, 6, 16, true);
}

// ==========================================
// SLIDE 7: DJ MART – HOME PAGE
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'User Interface Showcase', 'DJ Mart – Home Page', 'Elegant Landing & Product Discovery Experience with Real-Time Navigation');

  const homeImg = path.join(screenshotDir, 'slide7_home.png');
  if (fs.existsSync(homeImg)) {
    // Browser Mockup Frame
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 0.8,
      y: 1.95,
      w: 7.2,
      h: 4.75,
      fill: { color: 'FFFFFF' },
      line: { color: COLORS.BORDER_LIGHT, width: 2 },
      rectRadius: 0.1
    });

    slide.addImage({
      path: homeImg,
      x: 0.85,
      y: 2.0,
      w: 7.1,
      h: 4.65,
      sizing: { type: 'contain' }
    });
  }

  // Right Side: UI Callout Explanations
  const callouts = [
    {
      title: 'Sticky Luxury Navbar',
      desc: 'Features official DJ Mart branding, primary navigation links, category dropdowns, dynamic cart counter badge, and user authentication menu.'
    },
    {
      title: 'Integrated Search Bar',
      desc: 'Allows users to instantly search products by keywords with real-time query handling.'
    },
    {
      title: 'Hero Promotional Banner',
      desc: 'High-impact editorial hero section ("Timeless Elegance, Modern Luxury") establishing an elevated shopping atmosphere.'
    },
    {
      title: 'Curated Category Shortcuts',
      desc: 'Direct access cards for Electronics, Fashion, Home & Kitchen, and Books & Stationery.'
    },
    {
      title: 'Featured Product Grid',
      desc: 'Showcases luxury items with high-res photography, currency pricing in ₹, star ratings, and one-click "Add to Cart" triggers.'
    }
  ];

  let cY = 1.95;
  callouts.forEach((c, idx) => {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 8.3,
      y: cY,
      w: 4.2,
      h: 0.88,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1 },
      rectRadius: 0.08
    });

    slide.addText(`0${idx + 1}. ${c.title}`, {
      x: 8.45,
      y: cY + 0.1,
      w: 3.9,
      h: 0.25,
      fontSize: 10.5,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.GOLD_DARK
    });

    slide.addText(c.desc, {
      x: 8.45,
      y: cY + 0.35,
      w: 3.9,
      h: 0.45,
      fontSize: 8.5,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 13
    });

    cY += 0.96;
  });

  addFooter(slide, 7, 16);
}

// ==========================================
// SLIDE 8: PRODUCT BROWSING & PRODUCT DETAILS
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Catalog & Discovery', 'Product Browsing & Details', 'Comprehensive Catalog Filtering & Detailed Product Presentation');

  const productsImg = path.join(screenshotDir, 'slide8_products.png');
  const detailsImg = path.join(screenshotDir, 'slide8_product_details.png');

  // Left: Catalog Listing Screenshot
  if (fs.existsSync(productsImg)) {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 0.8,
      y: 1.95,
      w: 5.7,
      h: 3.8,
      fill: { color: 'FFFFFF' },
      line: { color: COLORS.BORDER_LIGHT, width: 1.5 },
      rectRadius: 0.08
    });

    slide.addImage({
      path: productsImg,
      x: 0.85,
      y: 2.0,
      w: 5.6,
      h: 3.7,
      sizing: { type: 'contain' }
    });

    slide.addText('CATALOG LISTING WITH FILTERS & SORT', {
      x: 0.8,
      y: 5.8,
      w: 5.7,
      h: 0.25,
      fontSize: 9,
      fontFace: 'Segoe UI',
      bold: true,
      align: 'center',
      color: COLORS.GOLD_DARK
    });
  }

  // Right: Product Details Screenshot
  if (fs.existsSync(detailsImg)) {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 6.8,
      y: 1.95,
      w: 5.7,
      h: 3.8,
      fill: { color: 'FFFFFF' },
      line: { color: COLORS.BORDER_LIGHT, width: 1.5 },
      rectRadius: 0.08
    });

    slide.addImage({
      path: detailsImg,
      x: 6.85,
      y: 2.0,
      w: 5.6,
      h: 3.7,
      sizing: { type: 'contain' }
    });

    slide.addText('EXPANDED PRODUCT DETAILS & REVIEWS VIEW', {
      x: 6.8,
      y: 5.8,
      w: 5.7,
      h: 0.25,
      fontSize: 9,
      fontFace: 'Segoe UI',
      bold: true,
      align: 'center',
      color: COLORS.GOLD_DARK
    });
  }

  // Bottom Explanatory Strip
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.8,
    y: 6.1,
    w: 11.7,
    h: 0.7,
    fill: { color: COLORS.CARD_LIGHT },
    line: { color: COLORS.BORDER_LIGHT, width: 1 },
    rectRadius: 0.08
  });

  slide.addText('KEY CAPABILITIES: • Multi-category filtering (Electronics, Fashion, Home)  • Price sorting (Low to High / High to Low)  • High-resolution photo gallery  • Live stock indicator badges  • Full technical specifications  • Direct "Add to Cart" action with instant feedback', {
    x: 1.0,
    y: 6.18,
    w: 11.3,
    h: 0.5,
    fontSize: 9,
    fontFace: 'Segoe UI',
    color: COLORS.TEXT_DARK
  });

  addFooter(slide, 8, 16);
}

// ==========================================
// SLIDE 9: SHOPPING CART
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Order Management', 'Shopping Cart', 'Interactive Cart Management, Dynamic Price Calculation & Order Review');

  const cartImg = path.join(screenshotDir, 'slide9_cart.png');
  if (fs.existsSync(cartImg)) {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 0.8,
      y: 1.95,
      w: 7.2,
      h: 4.75,
      fill: { color: 'FFFFFF' },
      line: { color: COLORS.BORDER_LIGHT, width: 2 },
      rectRadius: 0.1
    });

    slide.addImage({
      path: cartImg,
      x: 0.85,
      y: 2.0,
      w: 7.1,
      h: 4.65,
      sizing: { type: 'contain' }
    });
  }

  // Right Side: Cart Features & Flow
  const cartFeatures = [
    {
      title: 'Selected Product Overview',
      desc: 'Displays item thumbnails, titles, unit prices, and line-item totals clearly for buyer verification.'
    },
    {
      title: 'Dynamic Quantity Stepper',
      desc: 'Users can increment (+) or decrement (-) quantities. JavaScript communicates with /api/v1/cart/items to update subtotals without page reload.'
    },
    {
      title: 'Instant Item Removal',
      desc: 'One-click item deletion updates session cart state and header badge count instantly.'
    },
    {
      title: 'Comprehensive Order Summary',
      desc: 'Itemized price breakdown: cart subtotal, shipping estimates, tax calculation, and final grand total.'
    },
    {
      title: 'Direct Checkout Trigger',
      desc: 'Prominent "Proceed to Checkout" button guiding authenticated customers into fulfillment.'
    }
  ];

  let crtY = 1.95;
  cartFeatures.forEach((cf, idx) => {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 8.3,
      y: crtY,
      w: 4.2,
      h: 0.88,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1 },
      rectRadius: 0.08
    });

    slide.addText(`0${idx + 1}. ${cf.title}`, {
      x: 8.45,
      y: crtY + 0.1,
      w: 3.9,
      h: 0.25,
      fontSize: 10.5,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.GOLD_DARK
    });

    slide.addText(cf.desc, {
      x: 8.45,
      y: crtY + 0.35,
      w: 3.9,
      h: 0.45,
      fontSize: 8.5,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 13
    });

    crtY += 0.96;
  });

  addFooter(slide, 9, 16);
}

// ==========================================
// SLIDE 10: CHECKOUT & ORDER PROCESS
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Fulfillment Lifecycle', 'Checkout & Order Process', 'Seamless Transition from Cart Review to Verified Order Confirmation');

  const checkoutImg = path.join(screenshotDir, 'slide10_checkout.png');
  if (fs.existsSync(checkoutImg)) {
    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: 0.8,
      y: 1.95,
      w: 6.2,
      h: 4.75,
      fill: { color: 'FFFFFF' },
      line: { color: COLORS.BORDER_LIGHT, width: 2 },
      rectRadius: 0.1
    });

    slide.addImage({
      path: checkoutImg,
      x: 0.85,
      y: 2.0,
      w: 6.1,
      h: 4.65,
      sizing: { type: 'contain' }
    });
  }

  // Right Side: Step-by-Step Order Flow
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 7.3,
    y: 1.95,
    w: 5.2,
    h: 4.75,
    fill: { color: COLORS.CARD_LIGHT },
    line: { color: COLORS.BORDER_LIGHT, width: 1.5 },
    rectRadius: 0.1
  });

  slide.addText('END-TO-END ORDER EXECUTION FLOW', {
    x: 7.5,
    y: 2.15,
    w: 4.8,
    h: 0.3,
    fontSize: 11,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD_DARK
  });

  const steps = [
    { step: '1', title: 'Browse Products', desc: 'Customer discovers products and views real-time specifications.' },
    { step: '2', title: 'Add to Cart', desc: 'Item added to session cart via asynchronous /api/v1/cart/items call.' },
    { step: '3', title: 'Review Cart', desc: 'Quantities verified, price subtotals confirmed by buyer.' },
    { step: '4', title: 'Delivery Details', desc: 'Shipping address, phone number, and fulfillment preferences entered.' },
    { step: '5', title: 'Payment Selection', desc: 'Payment method selected (Cash on Delivery / Instant Checkout).' },
    { step: '6', title: 'Place Order & Commit', desc: 'Server runs ACID transaction: validates stock, decrements inventory, creates order record, and empties cart.' }
  ];

  let stpY = 2.5;
  steps.forEach(st => {
    slide.addShape(pptx.shapes.OVAL, {
      x: 7.5,
      y: stpY + 0.05,
      w: 0.28,
      h: 0.28,
      fill: { color: COLORS.OBSIDIAN },
      line: { color: COLORS.GOLD, width: 1 }
    });

    slide.addText(st.step, {
      x: 7.5,
      y: stpY + 0.05,
      w: 0.28,
      h: 0.28,
      fontSize: 8.5,
      fontFace: 'Segoe UI',
      bold: true,
      align: 'center',
      color: COLORS.GOLD
    });

    slide.addText(st.title, {
      x: 7.9,
      y: stpY,
      w: 4.4,
      h: 0.25,
      fontSize: 10,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(st.desc, {
      x: 7.9,
      y: stpY + 0.22,
      w: 4.4,
      h: 0.4,
      fontSize: 8.2,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 12
    });

    stpY += 0.68;
  });

  addFooter(slide, 10, 16);
}

// ==========================================
// SLIDE 11: KEY FEATURES
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Platform Capabilities', 'Key Features of DJ Mart', 'Robust Features Implemented & Operational in Current Release');

  const features = [
    { title: 'User-Friendly & Classical UI', desc: 'Distinctive luxury brand visual identity with custom gold accents, deep obsidian typography, and zero generic template styling.', icon: '👑' },
    { title: 'Fast Keyword Product Search', desc: 'Search bar enabling real-time product discovery across names, categories, and descriptions.', icon: '⚡' },
    { title: 'Structured Product Categories', desc: 'Multi-category taxonomy organizing electronics, fashion, home essentials, and stationery.', icon: '📂' },
    { title: 'Full Product Details View', desc: 'Dedicated views with high-res photos, detailed specifications, stock count, and price breakdowns.', icon: '🔍' },
    { title: 'Dynamic Interactive Cart', desc: 'AJAX-powered quantity adjustments (+/-), live total recalculation, and single-click item removal.', icon: '🛒' },
    { title: 'Validated Checkout & Orders', desc: 'Comprehensive delivery address validation and safe order generation with status tracking.', icon: '📝' },
    { title: 'Mobile-First Responsive Layout', desc: 'Fluid media queries, slide-out mobile drawer, and touch-optimized buttons (44x44px min).', icon: '📱' },
    { title: 'Enterprise Security Defenses', desc: '100% PreparedStatements against SQL injection, CSRF synchronizer tokens, and salted BCrypt passwords.', icon: '🛡️' }
  ];

  features.forEach((feat, idx) => {
    const col = idx % 4;
    const row = Math.floor(idx / 4);
    const x = 0.8 + col * 2.95;
    const y = 2.0 + row * 2.3;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 2.8,
      h: 2.1,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1.2 },
      rectRadius: 0.1
    });

    slide.addText(`${feat.icon}  ${feat.title}`, {
      x: x + 0.2,
      y: y + 0.2,
      w: 2.4,
      h: 0.45,
      fontSize: 11,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(feat.desc, {
      x: x + 0.2,
      y: y + 0.7,
      w: 2.4,
      h: 1.25,
      fontSize: 9,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 15
    });
  });

  addFooter(slide, 11, 16);
}

// ==========================================
// SLIDE 12: RESPONSIVE DESIGN
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Cross-Device Compatibility', 'Responsive Design', 'Verified Adaptive Layout Across Desktop, Tablet, and Mobile Formats');

  const deskImg = path.join(screenshotDir, 'slide12_desktop.png');
  const tabImg = path.join(screenshotDir, 'slide12_tablet.png');
  const mobImg = path.join(screenshotDir, 'slide12_mobile.png');

  // Desktop Card
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.8,
    y: 1.95,
    w: 4.3,
    h: 3.8,
    fill: { color: 'FFFFFF' },
    line: { color: COLORS.BORDER_LIGHT, width: 1.5 },
    rectRadius: 0.08
  });
  if (fs.existsSync(deskImg)) {
    slide.addImage({ path: deskImg, x: 0.85, y: 2.0, w: 4.2, h: 2.7, sizing: { type: 'contain' } });
  }
  slide.addText('🖥️ DESKTOP DISPLAY (1440px+)', {
    x: 0.85,
    y: 4.8,
    w: 4.2,
    h: 0.25,
    fontSize: 10,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD_DARK
  });
  slide.addText('Full multi-column product grid, sticky horizontal navigation bar, expanded category filter drawer, and detailed cart sidebars.', {
    x: 0.85,
    y: 5.1,
    w: 4.2,
    h: 0.55,
    fontSize: 8.5,
    fontFace: 'Segoe UI',
    color: COLORS.TEXT_MUTED_DARK,
    lineSpacing: 13
  });

  // Tablet Card
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 5.3,
    y: 1.95,
    w: 3.8,
    h: 3.8,
    fill: { color: 'FFFFFF' },
    line: { color: COLORS.BORDER_LIGHT, width: 1.5 },
    rectRadius: 0.08
  });
  if (fs.existsSync(tabImg)) {
    slide.addImage({ path: tabImg, x: 5.35, y: 2.0, w: 3.7, h: 2.7, sizing: { type: 'contain' } });
  }
  slide.addText('📱 TABLET DISPLAY (768px – 1024px)', {
    x: 5.35,
    y: 4.8,
    w: 3.7,
    h: 0.25,
    fontSize: 10,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD_DARK
  });
  slide.addText('Optimized 2-to-3 column grid, fluid container widths, and collapsible category menus maintaining comfortable touch interaction.', {
    x: 5.35,
    y: 5.1,
    w: 3.7,
    h: 0.55,
    fontSize: 8.5,
    fontFace: 'Segoe UI',
    color: COLORS.TEXT_MUTED_DARK,
    lineSpacing: 13
  });

  // Mobile Card
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 9.3,
    y: 1.95,
    w: 3.2,
    h: 3.8,
    fill: { color: 'FFFFFF' },
    line: { color: COLORS.BORDER_LIGHT, width: 1.5 },
    rectRadius: 0.08
  });
  if (fs.existsSync(mobImg)) {
    slide.addImage({ path: mobImg, x: 9.35, y: 2.0, w: 3.1, h: 2.7, sizing: { type: 'contain' } });
  }
  slide.addText('📲 MOBILE DISPLAY (390px)', {
    x: 9.35,
    y: 4.8,
    w: 3.1,
    h: 0.25,
    fontSize: 10,
    fontFace: 'Segoe UI',
    bold: true,
    color: COLORS.GOLD_DARK
  });
  slide.addText('Single-column stacked catalog, slide-out hamburger navigation drawer, and minimum 44x44px touch targets.', {
    x: 9.35,
    y: 5.1,
    w: 3.1,
    h: 0.55,
    fontSize: 8.5,
    fontFace: 'Segoe UI',
    color: COLORS.TEXT_MUTED_DARK,
    lineSpacing: 13
  });

  // Bottom Summary Bar
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 0.8,
    y: 6.0,
    w: 11.7,
    h: 0.75,
    fill: { color: COLORS.CARD_LIGHT },
    line: { color: COLORS.BORDER_LIGHT, width: 1 },
    rectRadius: 0.08
  });

  slide.addText('CONSISTENT VISUAL IDENTITY ACROSS ALL SCREENS: The luxury aesthetic—including gold accents, serif typography, high-res photography, and smooth transitions—is preserved uniformly whether accessed from a 4K monitor, iPad, or mobile phone.', {
    x: 1.0,
    y: 6.12,
    w: 11.3,
    h: 0.55,
    fontSize: 9,
    fontFace: 'Segoe UI',
    color: COLORS.TEXT_DARK,
    lineSpacing: 14
  });

  addFooter(slide, 12, 16);
}

// ==========================================
// SLIDE 13: ADVANTAGES
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Value Proposition', 'Advantages of DJ Mart', 'Key Technical & Practical Benefits for Shoppers & Administrators');

  const advantages = [
    { title: 'Simplicity & Ease of Use', desc: 'Intuitive user interface requiring zero prior training or learning curve.', icon: '💡' },
    { title: 'Saves Valuable Shopping Time', desc: 'Eliminates travel, parking, and checkout lines; completes purchases in seconds.', icon: '⏱️' },
    { title: 'Convenient 24/7 Access', desc: 'Uninterrupted online availability allows shopping anytime from any location.', icon: '🌐' },
    { title: 'Transparent Information', desc: 'Live stock badges, clear pricing in ₹, and detailed technical specifications.', icon: '🔍' },
    { title: 'Lightweight & Blazing Fast', desc: 'Pure Vanilla JS and optimized Jakarta Servlets avoid heavy framework bloat.', icon: '🚀' },
    { title: 'Multi-Device Accessibility', desc: 'Flawless visual presentation and navigation across desktop, tablet, and mobile.', icon: '📱' },
    { title: 'Enterprise-Grade Security', desc: 'Salted BCrypt hashing, CSRF synchronizer tokens, and SQLi protection.', icon: '🔒' },
    { title: 'Modular & Highly Scalable', desc: 'Layered MVC structure enables simple integration of future enterprise modules.', icon: '📈' }
  ];

  advantages.forEach((adv, idx) => {
    const col = idx % 4;
    const row = Math.floor(idx / 4);
    const x = 0.8 + col * 2.95;
    const y = 2.0 + row * 2.3;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 2.8,
      h: 2.1,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1.2 },
      rectRadius: 0.1
    });

    slide.addText(`${adv.icon}  ${adv.title}`, {
      x: x + 0.2,
      y: y + 0.2,
      w: 2.4,
      h: 0.45,
      fontSize: 11,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(adv.desc, {
      x: x + 0.2,
      y: y + 0.7,
      w: 2.4,
      h: 1.25,
      fontSize: 9,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 15
    });
  });

  addFooter(slide, 13, 16);
}

// ==========================================
// SLIDE 14: FUTURE ENHANCEMENTS
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Project Roadmap', 'Future Enhancements', 'Strategic Roadmap for Future Enterprise Scale & Advanced Features');

  const enhancements = [
    { title: 'Live Payment Gateway', desc: 'Integration with Razorpay, Stripe, and UPI for automated real-time digital payments and instant webhooks.', icon: '💳' },
    { title: 'Real-Time Order Tracking', desc: 'Interactive parcel status tracker with live carrier integration and map-based delivery updates.', icon: '🚚' },
    { title: 'Customer Wishlist', desc: 'Ability for customers to bookmark luxury products for future purchases and receive price drop alerts.', icon: '❤️' },
    { title: 'AI-Based Recommendations', desc: 'Machine learning algorithms suggesting relevant products based on browsing history and affinities.', icon: '🤖' },
    { title: 'Advanced Seller Management', desc: 'Dedicated seller portal with automated inventory alerts, barcode scanning, and bulk CSV uploads.', icon: '📊' },
    { title: 'Automated Notifications', desc: 'Multi-channel SMS and Email alerts for order confirmation, shipping updates, and invoice delivery.', icon: '🔔' },
    { title: 'Native Mobile Applications', desc: 'Cross-platform mobile apps for iOS and Android built using Flutter or React Native for app store distribution.', icon: '📲' },
    { title: 'Predictive Business Analytics', desc: 'Deep business intelligence dashboards with sales forecasting, customer retention, and inventory churn metrics.', icon: '📈' }
  ];

  enhancements.forEach((enh, idx) => {
    const col = idx % 4;
    const row = Math.floor(idx / 4);
    const x = 0.8 + col * 2.95;
    const y = 2.0 + row * 2.3;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 2.8,
      h: 2.1,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: 'CBD5E1', width: 1.2 },
      rectRadius: 0.1
    });

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x: x + 0.2,
      y: y + 0.15,
      w: 1.3,
      h: 0.22,
      fill: { color: 'FEF3C7' },
      line: { color: 'F59E0B', width: 0.8 },
      rectRadius: 0.05
    });
    slide.addText('FUTURE ROADMAP', {
      x: x + 0.2,
      y: y + 0.15,
      w: 1.3,
      h: 0.22,
      fontSize: 7,
      fontFace: 'Segoe UI',
      bold: true,
      color: 'B45309',
      align: 'center'
    });

    slide.addText(`${enh.icon}  ${enh.title}`, {
      x: x + 0.2,
      y: y + 0.45,
      w: 2.4,
      h: 0.4,
      fontSize: 10.5,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(enh.desc, {
      x: x + 0.2,
      y: y + 0.9,
      w: 2.4,
      h: 1.05,
      fontSize: 8.8,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 14
    });
  });

  addFooter(slide, 14, 16);
}

// ==========================================
// SLIDE 15: CONCLUSION
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.IVORY_BG };
  addHeader(slide, 'Project Summary', 'Conclusion', 'Summary of Project Achievements & Technical Milestones');

  const takeaways = [
    {
      title: 'Successful Development Using Antigravity',
      desc: 'DJ MART successfully demonstrates the rapid, robust development of a modern e-commerce web application using Google Antigravity, achieving clean architecture and enterprise code quality.',
      icon: '🏆'
    },
    {
      title: 'Complete End-to-End Shopping Experience',
      desc: 'Delivered an operational shopping workflow from catalog exploration, real-time search, and category filtering to cart adjustments, address capture, and atomic order placement.',
      icon: '🎯'
    },
    {
      title: 'Demonstration of Modern Web Concepts',
      desc: 'Proved the effectiveness of Jakarta EE 6 Servlets, JSTL server rendering, Vanilla JS API clients, H2 SQL database transactions, and bank-grade OWASP security protections.',
      icon: '⚙️'
    },
    {
      title: 'Extensible & Production-Ready Foundation',
      desc: 'The layered MVC codebase adheres to industry separation-of-concerns principles, establishing a solid foundation primed for live payment gateways and multi-cloud deployment.',
      icon: '🚀'
    }
  ];

  takeaways.forEach((t, idx) => {
    const col = idx % 2;
    const row = Math.floor(idx / 2);
    const x = 0.8 + col * 5.95;
    const y = 2.0 + row * 2.35;

    slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
      x,
      y,
      w: 5.75,
      h: 2.15,
      fill: { color: COLORS.CARD_LIGHT },
      line: { color: COLORS.BORDER_LIGHT, width: 1.2 },
      rectRadius: 0.1
    });

    slide.addText(`${t.icon}  ${t.title}`, {
      x: x + 0.3,
      y: y + 0.25,
      w: 5.15,
      h: 0.35,
      fontSize: 13,
      fontFace: 'Segoe UI',
      bold: true,
      color: COLORS.TEXT_DARK
    });

    slide.addText(t.desc, {
      x: x + 0.3,
      y: y + 0.7,
      w: 5.15,
      h: 1.25,
      fontSize: 10,
      fontFace: 'Segoe UI',
      color: COLORS.TEXT_MUTED_DARK,
      lineSpacing: 17
    });
  });

  addFooter(slide, 15, 16);
}

// ==========================================
// SLIDE 16: FINAL SLIDE – THANK YOU
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: COLORS.DEEP_BG };

  slide.addShape(pptx.shapes.RECTANGLE, { x: 0, y: 0, w: 13.333, h: 0.12, fill: { color: COLORS.GOLD } });

  // Official Logo if present
  if (fs.existsSync(logoPath)) {
    slide.addImage({ path: logoPath, x: 5.16, y: 1.0, w: 3.0, h: 1.2, sizing: { type: 'contain' } });
  }

  slide.addText('THANK YOU', {
    x: 1.0,
    y: 2.4,
    w: 11.333,
    h: 1.0,
    fontSize: 52,
    fontFace: 'Georgia',
    bold: true,
    align: 'center',
    color: COLORS.GOLD
  });

  slide.addText('Questions & Answers', {
    x: 1.0,
    y: 3.5,
    w: 11.333,
    h: 0.6,
    fontSize: 26,
    fontFace: 'Segoe UI',
    bold: true,
    align: 'center',
    color: COLORS.TEXT_LIGHT
  });

  slide.addText('"Smart, Simple & Convenient Online Shopping"', {
    x: 1.0,
    y: 4.2,
    w: 11.333,
    h: 0.45,
    fontSize: 15,
    fontFace: 'Georgia',
    italic: true,
    align: 'center',
    color: COLORS.GOLD_LIGHT
  });

  // Center Info Card
  slide.addShape(pptx.shapes.ROUNDED_RECTANGLE, {
    x: 3.66,
    y: 4.85,
    w: 6.0,
    h: 1.6,
    fill: { color: COLORS.CARD_DARK },
    line: { color: '334155', width: 1.5 },
    rectRadius: 0.1
  });

  slide.addText('DJ MART — E-Commerce Website', {
    x: 3.86,
    y: 5.05,
    w: 5.6,
    h: 0.35,
    fontSize: 13,
    fontFace: 'Segoe UI',
    bold: true,
    align: 'center',
    color: COLORS.GOLD
  });

  slide.addText('Developed using Antigravity\nCollege Project Presentation & Viva\nAcademic Year 2025 – 2026', {
    x: 3.86,
    y: 5.45,
    w: 5.6,
    h: 0.85,
    fontSize: 10,
    fontFace: 'Segoe UI',
    align: 'center',
    color: COLORS.TEXT_MUTED_LIGHT,
    lineSpacing: 16
  });

  addFooter(slide, 16, 16, true);
}

// Write the PPTX file
const outputPath = path.join(__dirname, '..', 'DJ_MART_Project_Presentation.pptx');
const brainArtifactPath = 'C:\\Users\\djnir\\.gemini\\antigravity\\brain\\6bc14fa7-10f4-4a85-94d1-594581264fbb\\DJ_MART_Project_Presentation.pptx';

pptx.writeFile({ fileName: outputPath })
  .then(() => {
    console.log(`PowerPoint PPTX created successfully at: ${outputPath}`);
    const stats = fs.statSync(outputPath);
    console.log(`PPTX File Size: ${(stats.size / 1024).toFixed(1)} KB`);

    // Copy to brain artifacts folder
    fs.copyFileSync(outputPath, brainArtifactPath);
    console.log(`Copied PPTX to Brain Artifact: ${brainArtifactPath}`);
  })
  .catch(err => {
    console.error('Error generating PPTX:', err);
    process.exit(1);
  });
