// generator/ch6_to_8.js
// Chapter 6: Functions Explanation, Chapter 7: Variables/State/DTOs, Chapter 8: UI Components

module.exports = {
  getChapter6: () => `
    <div class="page-break"></div>
    <h1>6. Functions &amp; Methods Deep-Dive</h1>

    <p>
      This chapter details the core functions, algorithms, and business logic methods powering transactions, catalog queries, security controls, and client interactions across DJ Mart.
    </p>

    <h2>6.1 Core Functions Matrix</h2>
    <table>
      <thead>
        <tr>
          <th>Function / Method</th>
          <th>Location (Class / File)</th>
          <th>Input &amp; Output Contract</th>
          <th>Execution Lifecycle &amp; Invoker</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><code>placeOrder(user, req)</code></td>
          <td><code>OrderServiceImpl.java</code></td>
          <td>Input: <code>User, CheckoutRequest</code><br>Output: <code>OrderResponse</code></td>
          <td>Triggered by <code>OrderServlet</code> when customer submits checkout form to complete atomic order transaction.</td>
        </tr>
        <tr>
          <td><code>decrementStock(conn, id, qty)</code></td>
          <td><code>ProductDAOImpl.java</code></td>
          <td>Input: <code>Connection, Long, int</code><br>Output: <code>boolean (affected rows)</code></td>
          <td>Invoked inside the atomic order transaction by <code>OrderServiceImpl</code> to decrement stock.</td>
        </tr>
        <tr>
          <td><code>hashPassword(plain)</code></td>
          <td><code>PasswordUtil.java</code></td>
          <td>Input: <code>String plainText</code><br>Output: <code>String (BCrypt hash)</code></td>
          <td>Invoked by <code>AuthServiceImpl</code> during customer registration or password modification.</td>
        </tr>
        <tr>
          <td><code>verifyPassword(plain, hash)</code></td>
          <td><code>PasswordUtil.java</code></td>
          <td>Input: <code>String plain, String hash</code><br>Output: <code>boolean</code></td>
          <td>Invoked by <code>AuthServiceImpl</code> during customer authentication to verify candidate credentials.</td>
        </tr>
        <tr>
          <td><code>generateToken()</code></td>
          <td><code>SecurityUtil.java</code></td>
          <td>Input: <i>None</i><br>Output: <code>String (Secure Hex 64-char)</code></td>
          <td>Invoked upon session instantiation and rotation to generate cryptographically random anti-CSRF tokens.</td>
        </tr>
        <tr>
          <td><code>updateCartItem(prodId, qty)</code></td>
          <td><code>cart.js (Client-side)</code></td>
          <td>Input: <code>Number productId, Number qty</code><br>Output: <code>Promise&lt;CartResponse&gt;</code></td>
          <td>Triggered on cart view when customer clicks "+" or "-" quantity modification buttons.</td>
        </tr>
      </tbody>
    </table>

    <h2>6.2 In-Depth Method Analysis</h2>

    <h3>1. placeOrder() — Atomic Checkout Order Placement</h3>
    <ul>
      <li><strong>Purpose:</strong> Validates user cart items, audits inventory availability, creates order records, and clears cart within an atomic transaction.</li>
      <li><strong>Input:</strong> Authenticated buyer entity (<code>User</code>), shipping address &amp; payment method (<code>CheckoutRequest</code>).</li>
      <li><strong>Output:</strong> Complete immutable snapshot of the created order (<code>OrderResponse</code>).</li>
      <li><strong>Execution Steps:</strong>
        <ol>
          <li>Validates user shopping cart. If empty, throws <code>ValidationException</code>.</li>
          <li>Fetches fresh server-side prices and inventory from the database. If available stock is insufficient, throws <code>InsufficientStockException</code>.</li>
          <li>Authoritatively computes subtotal and order grand total on the backend, preventing client-side price tampering.</li>
          <li>Begins SQL Transaction with <code>connection.setAutoCommit(false)</code>, decrements product inventory, inserts order, inserts line items, empties cart, and commits.</li>
        </ol>
      </li>
    </ul>

    <h3>2. authenticate() — Secure Customer Authentication</h3>
    <ul>
      <li><strong>Purpose:</strong> Verifies customer credentials and issues an authenticated session.</li>
      <li><strong>Input:</strong> <code>String email, String password</code>.</li>
      <li><strong>Output:</strong> Authenticated domain entity (<code>User</code>) or throws <code>AuthenticationException</code>.</li>
      <li><strong>Execution Steps:</strong> Locates user record via <code>UserDAO.findByEmail()</code>. If found, computes salt comparison with <code>PasswordUtil.verifyPassword()</code>. On success, regenerates session ID to prevent session fixation.</li>
    </ul>
  `,

  getChapter7: () => `
    <div class="page-break"></div>
    <h1>7. Variables, State, Models &amp; DTOs</h1>

    <p>
      This chapter details how domain state, session parameters, relational entities, and Data Transfer Objects (DTOs) are structured across DJ Mart.
    </p>

    <h2>7.1 Domain Entities &amp; Models</h2>
    <table>
      <thead>
        <tr>
          <th>Model / Entity</th>
          <th>Java Type</th>
          <th>Purpose &amp; Scope</th>
          <th>Source of Truth</th>
          <th>Where Consumed</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><code>User</code></td>
          <td>Entity Class</td>
          <td>Encapsulates user identity (id, name, email, role, createdAt).</td>
          <td>Relational <code>users</code> table via <code>UserDAO</code>.</td>
          <td>Stored in <code>HttpSession</code> under session attribute for RBAC filter checks and auditing.</td>
        </tr>
        <tr>
          <td><code>Product</code></td>
          <td>Entity Class</td>
          <td>Encapsulates product catalog details (id, name, price, stockQty, category, brand, sku, imageUrl).</td>
          <td>Relational <code>products</code> table.</td>
          <td>Catalog exploration, product details, cart, and seller management.</td>
        </tr>
        <tr>
          <td><code>Order</code></td>
          <td>Entity Class</td>
          <td>Encapsulates order status, total amount, shipping address, and buyer ID.</td>
          <td>Relational <code>orders</code> table.</td>
          <td>Order history, merchant fulfillment, admin oversight.</td>
        </tr>
        <tr>
          <td><code>OrderStatus</code></td>
          <td>Java Enum</td>
          <td>Represents order lifecycle states: <code>PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED</code>.</td>
          <td>Enforced by domain logic in Service layer.</td>
          <td>Order tracking views, status badges, administrative transitions.</td>
        </tr>
        <tr>
          <td><code>csrfToken</code></td>
          <td><code>String (Hex)</code></td>
          <td>Cryptographic token defending against Cross-Site Request Forgery.</td>
          <td>Generated by <code>SecurityUtil.generateSecureToken()</code>.</td>
          <td>Embedded in HTML meta tags, form inputs, and request headers.</td>
        </tr>
      </tbody>
    </table>

    <h2>7.2 Data Transfer Objects (DTOs)</h2>
    <div class="callout callout-info">
      <div class="callout-title">Architectural Value of DTOs</div>
      DTOs decouple internal database schemas from external API contracts. Sensitive attributes (such as password hashes) are never leaked to client views or JSON serialization streams.
    </div>

    <ul>
      <li><code>CheckoutRequest</code>: Encapsulates customer shipping address, postal code, and payment method payload.</li>
      <li><code>CartResponse</code>: Encapsulates active cart line items, total item count, and calculated subtotal.</li>
      <li><code>ApiError</code>: Standardized JSON payload delivered on client errors (<code>{ "success": false, "message": "..." }</code>).</li>
    </ul>
  `,

  getChapter8: () => `
    <div class="page-break"></div>
    <h1>8. UI Components &amp; Layouts</h1>

    <p>
      The user interface of DJ Mart delivers a luxury boutique experience characterized by crisp typography, subtle transitions, and clean layout geometry.
    </p>

    <h2>8.1 UI Components Matrix</h2>
    <table>
      <thead>
        <tr>
          <th>UI Component</th>
          <th>Purpose &amp; Description</th>
          <th>User Interaction</th>
          <th>Underlying Implementation</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>Brand Logo Header</strong></td>
          <td>Official DJ Mart identity and home navigation anchor.</td>
          <td>Click to navigate directly to marketplace homepage.</td>
          <td><code>&lt;a href="\${pageContext.request.contextPath}/"&gt;</code> in header.jsp</td>
        </tr>
        <tr>
          <td><strong>Search &amp; Filter Bar</strong></td>
          <td>Dynamic search and multi-facet filtering (categories, price bounds, sorting).</td>
          <td>Input search terms, select category dropdown, adjust price bounds.</td>
          <td><code>ProductServlet.doGet()</code> &rarr; <code>products.js</code> AJAX pipeline</td>
        </tr>
        <tr>
          <td><strong>Product Card</strong></td>
          <td>Card showcasing product image, brand, title, verified INR price, discount badge, and Add to Cart action.</td>
          <td>Click image/title for details, or click Add to Cart / Buy Now.</td>
          <td><code>products.js</code> &amp; <code>buyer/products.jsp</code></td>
        </tr>
        <tr>
          <td><strong>Cart Floating Badge</strong></td>
          <td>Real-time count badge rendered adjacent to the Cart navigation link.</td>
          <td>Displays live count of active items in customer cart.</td>
          <td><code>main.js: updateCartBadge(count)</code> DOM manipulation</td>
        </tr>
        <tr>
          <td><strong>Checkout Address Form</strong></td>
          <td>Validated form capturing shipping destination, phone number, and payment preference.</td>
          <td>Fill shipping fields and submit order placement.</td>
          <td><code>checkout.js: validateForm()</code> &rarr; POST <code>/api/v1/checkout</code></td>
        </tr>
        <tr>
          <td><strong>Toast Notifications</strong></td>
          <td>Non-blocking notification banner communicating success and error feedback.</td>
          <td>Displays briefly and automatically dismisses.</td>
          <td><code>toast.js: showToast(msg, 'success' | 'error')</code></td>
        </tr>
        <tr>
          <td><strong>AI Chat Assistant Drawer</strong></td>
          <td>Sliding conversational drawer querying live database prices and budgets.</td>
          <td>Send queries, click quick suggestion pills, browse product links.</td>
          <td><code>chat.js</code> &rarr; POST <code>/api/chat</code> &rarr; <code>DatabaseAwareChatEngine</code></td>
        </tr>
      </tbody>
    </table>
  `
};
