// generator/ch18_to_21.js
// Chapter 18: Beginner Mapping, Chapter 19: Complete User Flow, Chapter 20: Connection Diagram, Chapter 21: Mental Model

module.exports = {
  getChapter18: () => `
    <div class="page-break"></div>
    <h1>18. Beginner's Modification Guide</h1>

    <p>
      For developers onboarding to the DJ Mart codebase, the reference table below outlines which exact files to modify when implementing common changes and enhancements.
    </p>

    <table>
      <thead>
        <tr>
          <th>Feature / Customization Target</th>
          <th>Primary File to Edit</th>
          <th>File Path in Codebase</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>Brand Logo &amp; Assets</strong></td>
          <td><code>dj_mart_logo.jpg</code></td>
          <td><code>src/main/webapp/static/images/dj_mart_logo.jpg</code></td>
        </tr>
        <tr>
          <td><strong>Browser Favicon</strong></td>
          <td><code>favicon.ico</code></td>
          <td><code>src/main/webapp/favicon.ico</code></td>
        </tr>
        <tr>
          <td><strong>Theme Colors &amp; Styles</strong></td>
          <td><code>style.css</code> (CSS Variables <code>:root</code>)</td>
          <td><code>src/main/webapp/static/css/style.css</code></td>
        </tr>
        <tr>
          <td><strong>Homepage Hero Banner</strong></td>
          <td><code>index.jsp</code></td>
          <td><code>src/main/webapp/index.jsp</code></td>
        </tr>
        <tr>
          <td><strong>Navbar Navigation Links</strong></td>
          <td><code>header.jsp</code></td>
          <td><code>src/main/webapp/WEB-INF/views/common/header.jsp</code></td>
        </tr>
        <tr>
          <td><strong>Footer Information &amp; Links</strong></td>
          <td><code>footer.jsp</code></td>
          <td><code>src/main/webapp/WEB-INF/views/common/footer.jsp</code></td>
        </tr>
        <tr>
          <td><strong>Database Connection Configuration</strong></td>
          <td><code>DatabaseConfig.java</code></td>
          <td><code>src/main/java/com/djmart/config/DatabaseConfig.java</code></td>
        </tr>
        <tr>
          <td><strong>Product Categories &amp; Catalog</strong></td>
          <td><code>seed.sql</code> &amp; <code>products.jsp</code></td>
          <td><code>src/main/resources/db/seed.sql</code>, <code>WEB-INF/views/buyer/products.jsp</code></td>
        </tr>
        <tr>
          <td><strong>Order Total &amp; Pricing Calculation</strong></td>
          <td><code>OrderServiceImpl.java</code></td>
          <td><code>src/main/java/com/djmart/service/impl/OrderServiceImpl.java</code></td>
        </tr>
        <tr>
          <td><strong>Session Timeout Configuration</strong></td>
          <td><code>web.xml</code> (<code>&lt;session-timeout&gt;</code>)</td>
          <td><code>src/main/webapp/WEB-INF/web.xml</code></td>
        </tr>
        <tr>
          <td><strong>AI Chatbot Engine &amp; Prompts</strong></td>
          <td><code>DatabaseAwareChatEngine.java</code> / <code>GeminiChatProvider.java</code></td>
          <td><code>src/main/java/com/djmart/service/ai/</code></td>
        </tr>
      </tbody>
    </table>
  `,

  getChapter19: () => `
    <div class="page-break"></div>
    <h1>19. End-to-End User Journeys</h1>

    <p>
      The complete end-to-end customer and merchant interaction lifecycles are mapped out below step-by-step.
    </p>

    <h2>19.1 Complete Buyer Journey</h2>
    <div class="flowchart-container" style="text-align: left; font-size: 9.5pt;">
      <strong>Step 1: Arrival</strong> &#10140; Customer accesses the storefront (<code>index.jsp</code>), greets the luxury hero banner and trending catalog selections.<br>
      <strong>Step 2: Catalog Discovery</strong> &#10140; Navigates to the Shop view (<code>/products</code>), filters by category, price range, or conducts live search queries.<br>
      <strong>Step 3: Product Inspection</strong> &#10140; Inspects individual product details, specifications, real-time INR pricing, and verified customer reviews.<br>
      <strong>Step 4: Add to Cart</strong> &#10140; Clicks "Add to Cart"; JavaScript invokes <code>POST /api/v1/cart/items</code> and updates the cart badge instantaneously.<br>
      <strong>Step 5: Cart Review</strong> &#10140; Navigates to <code>/cart</code>, adjusts item quantities with live subtotal re-computation.<br>
      <strong>Step 6: Checkout</strong> &#10140; Proceeds to <code>/checkout</code>, specifies shipping destination, and selects payment method.<br>
      <strong>Step 7: Transaction Processing</strong> &#10140; Backend conducts atomic inventory validation, decrements stock, creates order snapshot, and clears the cart.<br>
      <strong>Step 8: Confirmation &amp; Tracking</strong> &#10140; Buyer receives confirmed order receipt and tracks delivery progression in their order history.<br>
      <strong>Step 9: Post-Purchase Review</strong> &#10140; Verified buyer submits a rating and feedback via <code>POST /api/v1/reviews</code>.
    </div>

    <h2>19.2 Complete Merchant / Seller Journey</h2>
    <div class="flowchart-container" style="text-align: left; font-size: 9.5pt;">
      <strong>Step 1: Merchant Authentication</strong> &#10140; Seller signs in with credentials and accesses the dedicated management console (<code>/seller/dashboard</code>).<br>
      <strong>Step 2: Performance Telemetry</strong> &#10140; Reviews aggregate sales revenue, active catalog listings, and low-inventory warning badges.<br>
      <strong>Step 3: Product Management</strong> &#10140; Publishes new inventory listings with verified market pricing, stock counts, and high-resolution media URLs.<br>
      <strong>Step 4: Order Fulfillment</strong> &#10140; Monitors incoming customer orders and updates shipment fulfillment status from <code>CONFIRMED</code> to <code>SHIPPED</code>.
    </div>
  `,

  getChapter20: () => `
    <div class="page-break"></div>
    <h1>20. Component Connection Architecture</h1>

    <p>
      The structural dependency and call graph across the DJ Mart application layers is illustrated below:
    </p>

    <div class="flowchart-container" style="line-height: 2;">
      <div class="flow-node">UI View (index.jsp / products.jsp / cart.jsp)</div>
      <div class="flow-down-arrow">&#9660; [DOM Events / AJAX Actions]</div>
      <div class="flow-node">Frontend JavaScript (main.js / cart.js / checkout.js)</div>
      <div class="flow-down-arrow">&#9660; [Fetch API + X-CSRF-TOKEN]</div>
      <div class="flow-node" style="background: #FEF3C7;">Security Filters (EncodingFilter &#10140; SecurityHeaders &#10140; CsrfFilter &#10140; AuthFilter)</div>
      <div class="flow-down-arrow">&#9660; [Sanitized &amp; Authenticated Request]</div>
      <div class="flow-node" style="background: #E0F2FE;">Servlet Controllers (ProductServlet / CartServlet / OrderServlet)</div>
      <div class="flow-down-arrow">&#9660; [Service Delegation]</div>
      <div class="flow-node" style="background: #DCFCE7;">Business Services (ProductServiceImpl / OrderServiceImpl / CartServiceImpl)</div>
      <div class="flow-down-arrow">&#9660; [DAO Invocations]</div>
      <div class="flow-node" style="background: #F3E8FF;">Data Access Objects (ProductDAOImpl / OrderDAOImpl / UserDAOImpl)</div>
      <div class="flow-down-arrow">&#9660; [JDBC PreparedStatement]</div>
      <div class="flow-node" style="background: #FFE4E6;">HikariCP Connection Pool &#10140; H2 / PostgreSQL Relational Database Engine</div>
    </div>
  `,

  getChapter21: () => `
    <div class="page-break"></div>
    <h1>21. Beginner Mental Model</h1>

    <div class="callout callout-success">
      <div class="callout-title">The Luxury Department Store Analogy</div>
      To develop an intuitive conceptual understanding of the DJ Mart software architecture, imagine a world-class luxury department store!
    </div>

    <table>
      <thead>
        <tr>
          <th>Software Component</th>
          <th>Department Store Analogy</th>
          <th>Real-World Responsibility</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>Client Browser</strong></td>
          <td>Store Entrance &amp; Aisles</td>
          <td>The physical environment where customers explore products and submit purchase requests.</td>
        </tr>
        <tr>
          <td><strong>JSP, HTML5 &amp; CSS3</strong></td>
          <td>Showcases, Lighting &amp; Aesthetic Décor</td>
          <td>Presenting merchandise cleanly, displaying pricing, and creating an inviting luxury ambiance.</td>
        </tr>
        <tr>
          <td><strong>Client JavaScript</strong></td>
          <td>Interactive Kiosks &amp; Counters</td>
          <td>Handling immediate customer actions (adding items to cart, live quantity recalculation) seamlessly.</td>
        </tr>
        <tr>
          <td><strong>Apache Tomcat</strong></td>
          <td>Building Infrastructure &amp; Power</td>
          <td>The operational foundation supporting all services, request routing, and web communications.</td>
        </tr>
        <tr>
          <td><strong>Security Filters</strong></td>
          <td>Store Security &amp; Concierge Checkpoint</td>
          <td>Screening requests for malicious input, verifying tokens, and validating VIP access credentials.</td>
        </tr>
        <tr>
          <td><strong>Servlets (Controllers)</strong></td>
          <td>Reception Desks</td>
          <td>Receiving customer requests, validating required parameters, and delegating to proper department managers.</td>
        </tr>
        <tr>
          <td><strong>Service Layer</strong></td>
          <td>Department Floor Managers</td>
          <td>Enforcing business policies: verifying stock availability, calculating order totals, and scheduling shipments.</td>
        </tr>
        <tr>
          <td><strong>DAOs (Data Access)</strong></td>
          <td>Warehouse Inventory Clerks</td>
          <td>Safely fetching merchandise or storing transaction records in response to manager instructions.</td>
        </tr>
        <tr>
          <td><strong>Relational Database</strong></td>
          <td>High-Security Vault &amp; Ledgers</td>
          <td>The tamper-resistant central repository where financial transactions, account data, and catalogs reside.</td>
        </tr>
      </tbody>
    </table>

    <div class="callout callout-info" style="margin-top: 25px;">
      <div class="callout-title">Conclusion</div>
      With this architectural mental model in mind, tracking the flow of any feature or debugging an issue across the DJ Mart stack becomes intuitive, logical, and rapid!
    </div>
  `
};
