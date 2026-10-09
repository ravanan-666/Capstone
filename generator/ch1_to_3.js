// generator/ch1_to_3.js
// Chapters 1, 2, and 3: Cover Page, Project Overview, Architecture Overview

module.exports = {
  getChapter1: () => `
    <div class="cover-container">
      <div class="cover-header">
        <div class="cover-logo-badge">Dj Mart</div>
        <p style="letter-spacing: 2px; color: #64748B; text-transform: uppercase; font-size: 9pt; margin: 0;">
          Luxury Multi-Seller Marketplace Platform
        </p>
      </div>

      <div class="cover-title-box">
        <div class="cover-main-title">
          Website / Web App – Comprehensive Code Explanation
        </div>
        <div class="cover-sub-title">
          Technical Architecture &amp; Implementation Guide
        </div>
        <p class="cover-description">
          DJ Mart is an enterprise-grade luxury e-commerce web application engineered using modern <strong>Jakarta EE 6</strong>, <strong>Java Servlets</strong>, <strong>JSP / JSTL</strong>, and <strong>Vanilla JavaScript</strong>. This document provides a complete, clear, and comprehensive breakdown of the codebase architecture, database persistence, security controls, and RESTful APIs.
        </p>
      </div>

      <div class="no-break" style="margin: 20px auto; max-width: 500px; text-align: left; background: #FFFFFF; border: 1px solid #CBD5E1; padding: 15px 20px; border-radius: 6px;">
        <div style="font-weight: bold; color: #0F172A; margin-bottom: 8px; border-bottom: 1px solid #E2E8F0; padding-bottom: 4px;">
          Project Metadata:
        </div>
        <table style="margin: 0; font-size: 8.5pt;">
          <tr><td><strong>Project Name:</strong></td><td>DJ Mart Luxury Marketplace</td></tr>
          <tr><td><strong>Architecture:</strong></td><td>Layered MVC (Servlet + Service + DAO)</td></tr>
          <tr><td><strong>Target Server:</strong></td><td>Apache Tomcat 10.1.x (Jakarta EE 6)</td></tr>
          <tr><td><strong>Database:</strong></td><td>H2 &amp; PostgreSQL Relational Database</td></tr>
          <tr><td><strong>Security:</strong></td><td>BCrypt + CSRF Tokens + RBAC Filter</td></tr>
          <tr><td><strong>Documentation Date:</strong></td><td>October 2026</td></tr>
        </table>
      </div>

      <div class="cover-footer">
        <div><strong>Technical Documentation</strong> | Version 2.0</div>
        <div>Anna University Capstone Standard (R2025)</div>
      </div>
    </div>
  `,

  getTableOfContents: () => `
    <div class="page-break"></div>
    <h1>Table of Contents</h1>
    <p style="color: #64748B; font-size: 9pt; margin-bottom: 20px;">
      The 21 comprehensive technical chapters contained in this documentation:
    </p>

    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">1.</span> Cover Page</span><span>Page 1</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">2.</span> Project Overview</span><span>Page 3</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">3.</span> Architecture Overview &amp; Diagrams</span><span>Page 5</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">4.</span> Folder &amp; File Structure</span><span>Page 8</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">5.</span> File-by-File Detailed Explanation</span><span>Page 12</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">6.</span> Functions &amp; Methods Deep-Dive</span><span>Page 22</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">7.</span> Variables, State, Models &amp; DTOs</span><span>Page 28</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">8.</span> UI Components &amp; Layouts</span><span>Page 33</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">9.</span> Button &amp; Interaction Flows</span><span>Page 38</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">10.</span> RESTful API Documentation</span><span>Page 43</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">11.</span> Database Schema &amp; CRUD Analysis</span><span>Page 48</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">12.</span> Libraries &amp; Dependencies (pom.xml)</span><span>Page 53</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">13.</span> CSS &amp; Luxury Design System</span><span>Page 56</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">14.</span> Mobile &amp; Responsive Adaptation</span><span>Page 60</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">15.</span> Security Architecture (OWASP Protections)</span><span>Page 63</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">16.</span> Error Handling Architecture</span><span>Page 67</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">17.</span> Code Issues &amp; Roadmap</span><span>Page 70</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">18.</span> Beginner's Modification Guide ("Where to Edit")</span><span>Page 73</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">19.</span> End-to-End User Journeys</span><span>Page 76</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">20.</span> Component Connection Architecture</span><span>Page 79</span></div>
    <div class="toc-item"><span class="toc-title"><span class="toc-chap-num">21.</span> Beginner Mental Model &amp; Principles</span><span>Page 82</span></div>
  `,

  getChapter2: () => `
    <div class="page-break"></div>
    <h1>2. Project Overview</h1>

    <p>
      <strong>DJ Mart</strong> is a complete luxury multi-seller e-commerce web application engineered to deliver an elevated, premium consumer shopping experience backed by high-speed, secure server-side commerce transactions.
    </p>

    <h2>2.1 Platform Purpose</h2>
    <p>
      Unlike single-vendor shops or generic e-commerce templates, DJ Mart is engineered with distinctive architectural advantages:
    </p>
    <ul>
      <li><strong>Multi-Seller Ecosystem:</strong> Multiple verified independent merchants can list, manage, and dispatch their inventories from isolated accounts.</li>
      <li><strong>Editorial &amp; Luxury Aesthetic:</strong> Modern luxury palette with high contrast slate, royal accents, and clean typography creating a premium brand atmosphere.</li>
      <li><strong>Authoritative Security &amp; Data Integrity:</strong> Industrial BCrypt password hashing, CSRF token validation, role-based access filters, and atomic database transactions.</li>
    </ul>

    <h2>2.2 Main Features &amp; User Roles</h2>
    <p>
      DJ Mart operates across three distinct user roles with full isolation and strict access governance:
    </p>

    <table>
      <thead>
        <tr>
          <th>User Role</th>
          <th>Access Permissions</th>
          <th>Key Capabilities</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><span class="badge badge-role">BUYER</span></td>
          <td>Public storefront, Cart, Checkout, Personal Orders</td>
          <td>
            • Catalog browsing, keyword search, price range filtering, category navigation.<br>
            • Cart operations (Add to Cart, quantity modification, item removal).<br>
            • Secure atomic checkout and order placement.<br>
            • Order history tracking and cancellation.<br>
            • Product reviews and ratings.<br>
            • Real AI conversational shopping assistant (live database queries).
          </td>
        </tr>
        <tr>
          <td><span class="badge badge-role">SELLER</span></td>
          <td>Seller Portal (/seller/*)</td>
          <td>
            • Product listing management (Create, Read, Update, Delete).<br>
            • Inventory control and low-stock indicators.<br>
            • Order status workflow updates (CONFIRMED &rarr; SHIPPED).
          </td>
        </tr>
        <tr>
          <td><span class="badge badge-role">ADMIN</span></td>
          <td>Administration Suite (/admin/*)</td>
          <td>
            • User management and role moderation.<br>
            • Sitewide order oversight and lifecycle management.<br>
            • Product catalog price and stock updates.<br>
            • Real-time sales analytics and GMV telemetry.
          </td>
        </tr>
      </tbody>
    </table>

    <h2>2.3 Technology Stack Overview</h2>
    <div class="callout callout-info">
      <div class="callout-title">Architecture Summary</div>
      DJ Mart is built on modern Java Enterprise foundations using <strong>Jakarta EE 6 Servlets</strong> and lightweight <strong>Vanilla JavaScript</strong>, avoiding bloated frameworks to deliver instant page rendering and rock-solid stability.
    </div>

    <ul>
      <li><strong>Frontend Layer:</strong> JavaServer Pages (JSP), Jakarta Standard Tag Library (JSTL 3.0), HTML5, CSS3 Custom Properties, Vanilla JavaScript (ES6+).</li>
      <li><strong>Backend Layer:</strong> Java 17 / Java 21 LTS, Jakarta Servlet 6.0, Filter Chain, Layered MVC (Controller &rarr; Service &rarr; DAO).</li>
      <li><strong>Database &amp; Connection:</strong> H2 &amp; PostgreSQL Relational Database (SQL), HikariCP Connection Pooling, ACID Transactions.</li>
      <li><strong>Security &amp; Utilities:</strong> jBCrypt (Password Hashing), Google Gson (JSON Parsing), SLF4J / Logback (Audit Logging).</li>
    </ul>
  `,

  getChapter3: () => `
    <div class="page-break"></div>
    <h1>3. Architecture Overview &amp; Request Flow</h1>

    <p>
      DJ Mart implements the industry-standard <strong>Layered MVC (Model-View-Controller)</strong> architecture pattern, ensuring clear separation of concerns, high testability, and enterprise maintainability.
    </p>

    <h2>3.1 High-Level Request Flow</h2>
    <p>
      The complete lifecycle of an HTTP request within the application:
    </p>

    <div class="flowchart-container">
      <div class="flow-node">1. Client Browser<br><small>HTML5, CSS3, Vanilla JS</small></div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">2. Apache Tomcat 10.1<br><small>HTTP / HTTPS Request</small></div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #FEF3C7; border-color: #D97706;">
        3. Filter Chain<br>
        <small>EncodingFilter &#10140; RequestLoggingFilter &#10140; SecurityHeadersFilter &#10140; CsrfFilter &#10140; AuthFilter</small>
      </div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #E0F2FE; border-color: #0284C7;">
        4. Servlet Controller Layer<br>
        <small>AuthServlet, ProductServlet, CartServlet, OrderServlet, SellerServlet, AdminServlet, ChatServlet</small>
      </div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #DCFCE7; border-color: #16A34A;">
        5. Business Service Layer<br>
        <small>AuthService, ProductService, CartService, OrderService, UserService, ReviewService, ChatService</small>
      </div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #F3E8FF; border-color: #9333EA;">
        6. Data Access Object (DAO) Layer<br>
        <small>UserDAO, ProductDAO, CartDAO, OrderDAO, OrderItemDAO, ReviewDAO, ChatDAO</small>
      </div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #FFE4E6; border-color: #E11D48;">
        7. Connection Pool &amp; Database Engine<br>
        <small>HikariDataSource &#10140; Connection &#10140; PreparedStatement &#10140; SQL Execution</small>
      </div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node">
        8. Response Generation<br>
        <small>HTML Page View (JSP/JSTL) or REST API JSON Response</small>
      </div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">9. DOM Update &amp; Toasts<br><small>Rendered on Client View</small></div>
    </div>

    <h2>3.2 Layer Responsibilities</h2>
    <table>
      <thead>
        <tr>
          <th>Layer</th>
          <th>Technology Used</th>
          <th>Primary Responsibility</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>Presentation (View)</strong></td>
          <td>JSP, JSTL, CSS, JS</td>
          <td>Renders user interfaces, captures inputs, executes debounced AJAX queries, and manages client state.</td>
        </tr>
        <tr>
          <td><strong>Security (Filter)</strong></td>
          <td>Jakarta Servlet Filter</td>
          <td>Inspects all incoming requests: UTF-8 encoding, CSRF token verification, and RBAC authentication guards.</td>
        </tr>
        <tr>
          <td><strong>Controller (Servlet)</strong></td>
          <td>Jakarta HttpServlet</td>
          <td>Accepts HTTP requests (GET/POST), validates input payloads, coordinates services, and dispatches views/JSON.</td>
        </tr>
        <tr>
          <td><strong>Service (Business Logic)</strong></td>
          <td>Java Interfaces &amp; Impl</td>
          <td>Executes domain business rules: inventory validation, authoritative pricing calculations, and transaction rollbacks.</td>
        </tr>
        <tr>
          <td><strong>Data Access (DAO)</strong></td>
          <td>JDBC PreparedStatements</td>
          <td>Direct communication with SQL database executing parameterized queries and mapping ResultSets to Java domain models.</td>
        </tr>
        <tr>
          <td><strong>Database Engine</strong></td>
          <td>H2 &amp; PostgreSQL</td>
          <td>Persistent relational storage with ACID guarantees, foreign keys, uniqueness constraints, and indexing.</td>
        </tr>
      </tbody>
    </table>
  `
};
