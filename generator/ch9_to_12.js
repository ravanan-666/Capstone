// generator/ch9_to_12.js
// Chapter 9: Button Flows, Chapter 10: API Docs, Chapter 11: Database Docs, Chapter 12: Libraries & Dependencies

module.exports = {
  getChapter9: () => `
    <div class="page-break"></div>
    <h1>9. Button & User Interaction Flow</h1>

    <p>
      When a user interacts with various buttons across the DJ Mart web application, a precise sequence of execution steps occurs from the browser viewport down to the database persistence layer. The core flows are detailed below.
    </p>

    <h2>9.1 Detailed Execution Flow of 9 Core Button Interactions</h2>

    <h3>1. Login Button Flow</h3>
    <div class="flowchart-container">
      <div class="flow-node">User Click (Login Button)</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">Form Validation (HTML5 / JS)</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">POST /auth/login</div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node">AuthServlet.doPost()</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">BCrypt Password Verify</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">HttpSession Creation</div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #DCFCE7;">Redirect to Home or Role Dashboard</div>
    </div>

    <h3>2. Add to Cart Button Flow</h3>
    <div class="flowchart-container">
      <div class="flow-node">Add to Cart Click</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">cart.js: addToCart()</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">POST /api/v1/cart/items</div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node">CartServlet.doPost()</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">CartDAO: INSERT/UPDATE</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">JSON CartResponse</div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #DCFCE7;">Header Badge Update (DOM) &amp; Toast Notification ("Item added!")</div>
    </div>

    <h3>3. Place Order Button Flow</h3>
    <div class="flowchart-container">
      <div class="flow-node">Place Order Click</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">checkout.js Validation</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">POST /api/v1/checkout</div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node">OrderServlet &#10140; OrderService</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">Stock Verification</div>
      <div class="flow-arrow">&#10140;</div>
      <div class="flow-node">ACID Transaction: Stock Decrement + Order Insert + Cart Clear</div>
      <div class="flow-down-arrow">&#9660;</div>
      <div class="flow-node" style="background: #DCFCE7;">Redirect to Order Confirmation Page</div>
    </div>

    <h3>4. Cart Quantity Increment / Decrement Flow</h3>
    <p>When a user clicks "+" or "-" on the shopping cart page: <code>cart.js</code> &#10140; <code>PUT /api/v1/cart/items/{id}</code> &#10140; <code>CartServlet</code> &#10140; <code>CartDAO.updateQuantity()</code> &#10140; New item subtotals and cart total are calculated and updated in the DOM live without a full page reload.</p>

    <h3>5. Cancel Order Flow</h3>
    <p>When a buyer clicks "Cancel Order" in their order history: <code>POST /api/v1/orders/{id}/cancel</code> &#10140; <code>OrderService</code> verifies the order is in <code>PENDING</code> or <code>CONFIRMED</code> status &#10140; Status updates to <code>CANCELLED</code>, and purchased line items are atomically restocked into product inventory.</p>

    <h3>6. Review Submission Flow</h3>
    <p>When a customer selects a star rating (1-5 Stars), enters feedback, and clicks submit: <code>POST /api/v1/reviews</code> &#10140; Verified Buyer validation confirms product purchase &#10140; <code>ReviewDAO</code> persists the review &#10140; The product's average rating is recalculated.</p>

    <h3>7. Seller Add Product Flow</h3>
    <p>When a seller fills out the catalog form and clicks "Save Product": <code>POST /api/v1/seller/products</code> &#10140; <code>ProductDAO.create()</code> &#10140; Product is inserted into database catalog and appears immediately in the seller's inventory table.</p>

    <h3>8. Seller Order Status Update Flow</h3>
    <p>When a seller transitions an order from "CONFIRMED" to "SHIPPED": <code>POST /api/v1/seller/orders/{id}/status</code> &#10140; IDOR ownership check ensures seller owns items in the order before updating status.</p>

    <h3>9. Admin Role Update Flow</h3>
    <p>When an administrator promotes a user from BUYER to SELLER: <code>POST /api/v1/admin/users/{id}/role</code> &#10140; <code>UserDAO.updateRole()</code> &#10140; The user's role is updated in the database with instant UI feedback.</p>
  `,

  getChapter10: () => `
    <div class="page-break"></div>
    <h1>10. RESTful API Documentation</h1>

    <p>
      DJ Mart communicates between frontend views and backend services via high-performance <strong>JSON-based</strong> RESTful APIs. All core endpoints are documented below.
    </p>

    <h2>10.1 Core REST API Endpoints</h2>
    <table>
      <thead>
        <tr>
          <th>Method</th>
          <th>Endpoint URL</th>
          <th>Purpose &amp; Security</th>
          <th>Input Payload / Query Params</th>
          <th>Output (JSON)</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><span class="badge badge-get">GET</span></td>
          <td><code>/api/v1/products</code></td>
          <td>Retrieve product catalog with filtering, sorting, and search. Public access.</td>
          <td><code>?category=Electronics&amp;sort=price_asc</code></td>
          <td><code>{ "products": [...], "total": 24 }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-get">GET</span></td>
          <td><code>/api/v1/cart</code></td>
          <td>Retrieve all items currently in the authenticated user's cart.</td>
          <td><i>None (Session User ID)</i></td>
          <td><code>{ "items": [...], "subtotal": 29990.00 }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-post">POST</span></td>
          <td><code>/api/v1/cart/items</code></td>
          <td>Add an item to shopping cart (CSRF protected).</td>
          <td><code>{ "productId": 3, "quantity": 1 }</code></td>
          <td><code>{ "success": true, "cartCount": 3 }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-put">PUT</span></td>
          <td><code>/api/v1/cart/items/{id}</code></td>
          <td>Update quantity of a specific cart item.</td>
          <td><code>{ "quantity": 2 }</code></td>
          <td><code>{ "success": true, "itemSubtotal": 59980.00 }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-delete">DELETE</span></td>
          <td><code>/api/v1/cart/items/{id}</code></td>
          <td>Remove a specific item from the shopping cart.</td>
          <td><i>URL Path Variable {id}</i></td>
          <td><code>{ "success": true, "message": "Item removed" }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-post">POST</span></td>
          <td><code>/api/v1/checkout</code></td>
          <td>Execute checkout and generate a confirmed order transaction.</td>
          <td><code>{ "shippingAddress": "...", "paymentMethod": "COD" }</code></td>
          <td><code>{ "success": true, "orderId": 104 }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-post">POST</span></td>
          <td><code>/api/v1/orders/{id}/cancel</code></td>
          <td>Cancel an existing order and restock inventory (Buyer Auth).</td>
          <td><i>URL Path Variable {id}</i></td>
          <td><code>{ "success": true, "status": "CANCELLED" }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-post">POST</span></td>
          <td><code>/api/v1/seller/products</code></td>
          <td>Create a new product listing (Seller Auth).</td>
          <td><code>{ "name": "Smart Watch", "price": 4999.00, "stock": 15 }</code></td>
          <td><code>{ "success": true, "productId": 45 }</code></td>
        </tr>
        <tr>
          <td><span class="badge badge-post">POST</span></td>
          <td><code>/api/v1/admin/users/{id}/role</code></td>
          <td>Update a user's system permissions and role (Admin Auth).</td>
          <td><code>{ "role": "SELLER" }</code></td>
          <td><code>{ "success": true, "message": "Role updated successfully" }</code></td>
        </tr>
      </tbody>
    </table>
  `,

  getChapter11: () => `
    <div class="page-break"></div>
    <h1>11. Database Documentation</h1>

    <p>
      DJ Mart utilizes a dual-compatible relational persistence engine powered by <strong>H2 Database</strong> for zero-configuration local embedded execution (<code>jdbc:h2:./data/djmart</code>) and <strong>PostgreSQL</strong> for cloud production. Full ACID compliance, strict foreign key constraints, and automatic migration tracking ensure enterprise data integrity.
    </p>

    <h2>11.1 Entity Relationship Diagram (ERD Representation)</h2>
    <div class="flowchart-container" style="text-align: left; font-family: monospace; font-size: 8.5pt;">
      USERS (1) &#9472;&#9472;&#9472;&#9472;&lt; PRODUCTS (N) [Seller Products]<br>
      USERS (1) &#9472;&#9472;&#9472;&#9472;&lt; ORDERS (N)   [Buyer Orders]<br>
      USERS (1) &#9472;&#9472;&#9472;&#9472;&lt; CART_ITEMS (N) [Buyer Cart]<br>
      USERS (1) &#9472;&#9472;&#9472;&#9472;&lt; REVIEWS (N)  [Buyer Reviews]<br>
      <br>
      ORDERS (1) &#9472;&#9472;&#9472;&lt; ORDER_ITEMS (N) &gt;&#9472;&#9472;&#9472; (1) PRODUCTS<br>
      CART_ITEMS (N) &gt;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; (1) PRODUCTS<br>
      REVIEWS (N) &gt;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; (1) PRODUCTS
    </div>

    <h2>11.2 Core Database Tables and Schema Specification</h2>
    <table>
      <thead>
        <tr>
          <th>Table Name</th>
          <th>Key Columns</th>
          <th>Constraints &amp; Integrity</th>
          <th>Description &amp; Purpose</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><code>users</code></td>
          <td><code>id, name, email, password_hash, role, created_at</code></td>
          <td><code>PRIMARY KEY(id), UNIQUE(email), CHECK(role IN ('BUYER','SELLER','ADMIN'))</code></td>
          <td>Persists customer and merchant credentials, salted password hashes, and access roles.</td>
        </tr>
        <tr>
          <td><code>products</code></td>
          <td><code>id, seller_id, name, price, stock_qty, category, image_url</code></td>
          <td><code>FK(seller_id REFERENCES users), CHECK(price &gt;= 0), CHECK(stock_qty &gt;= 0)</code></td>
          <td>Stores the merchant catalog, current INR pricing, stock levels, and media links.</td>
        </tr>
        <tr>
          <td><code>orders</code></td>
          <td><code>id, buyer_id, status, total_amount, shipping_address, created_at</code></td>
          <td><code>FK(buyer_id REFERENCES users), CHECK(status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'))</code></td>
          <td>Records order header information, shipping address, total billed amount, and lifecycle state.</td>
        </tr>
        <tr>
          <td><code>order_items</code></td>
          <td><code>id, order_id, product_id, quantity, unit_price</code></td>
          <td><code>FK(order_id), FK(product_id), CHECK(quantity &gt; 0)</code></td>
          <td>Captures line item snapshots per order including quantity and unit price at time of purchase.</td>
        </tr>
        <tr>
          <td><code>cart_items</code></td>
          <td><code>id, user_id, product_id, quantity</code></td>
          <td><code>FK(user_id), FK(product_id), UNIQUE(user_id, product_id)</code></td>
          <td>Maintains active shopping cart selections per authenticated buyer.</td>
        </tr>
        <tr>
          <td><code>reviews</code></td>
          <td><code>id, product_id, user_id, rating, comment</code></td>
          <td><code>FK(product_id), FK(user_id), CHECK(rating BETWEEN 1 AND 5), UNIQUE(product_id, user_id)</code></td>
          <td>Stores verified buyer feedback and 1 to 5 star ratings per product.</td>
        </tr>
      </tbody>
    </table>
  `,

  getChapter12: () => `
    <div class="page-break"></div>
    <h1>12. Libraries & Dependencies</h1>

    <p>
      All third-party libraries and runtime dependencies are managed via Apache Maven in <code>pom.xml</code>. Below is an overview of each library and its role in the DJ Mart architecture.
    </p>

    <table>
      <thead>
        <tr>
          <th>Dependency Coordinates</th>
          <th>Version</th>
          <th>Architectural Role &amp; Purpose</th>
          <th>System Impact Without It</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><code>jakarta.servlet:jakarta.servlet-api</code></td>
          <td>6.0.0</td>
          <td>Core servlet engine API for Servlets, HTTP Filters, and Context Listeners on Tomcat 10.1.</td>
          <td>Compilation failure; web container cannot execute backend controller endpoints.</td>
        </tr>
        <tr>
          <td><code>jakarta.servlet.jsp.jstl</code></td>
          <td>3.0.0</td>
          <td>Standard tag library for server-rendered UI iteration (<code>&lt;c:forEach&gt;</code>) and logic (<code>&lt;c:if&gt;</code>).</td>
          <td>Dynamic rendering fails; JSPs cannot evaluate expressions or display database collections.</td>
        </tr>
        <tr>
          <td><code>com.h2database:h2</code></td>
          <td>2.2.224</td>
          <td>Lightweight, fast embedded relational SQL database engine with zero external setup.</td>
          <td>Requires external DBMS installation and administration prior to local development.</td>
        </tr>
        <tr>
          <td><code>org.postgresql:postgresql</code></td>
          <td>42.7.3</td>
          <td>Production-grade JDBC driver for connecting to cloud-hosted PostgreSQL instances.</td>
          <td>Unable to connect to managed cloud PostgreSQL databases (Render, Neon, Supabase).</td>
        </tr>
        <tr>
          <td><code>com.zaxxer:HikariCP</code></td>
          <td>5.1.0</td>
          <td>High-performance, ultra-low-latency JDBC connection pooling engine.</td>
          <td>Severely degraded response times due to creating a new TCP connection on every request.</td>
        </tr>
        <tr>
          <td><code>org.mindrot:jbcrypt</code></td>
          <td>0.4</td>
          <td>Cryptographically secure, salted one-way hashing for user authentication passwords.</td>
          <td>Unsafe plain-text or weak hash password storage, exposing user accounts to compromise.</td>
        </tr>
        <tr>
          <td><code>com.google.code.gson:gson</code></td>
          <td>2.10.1</td>
          <td>High-speed JSON serialization and deserialization for RESTful APIs and AI chat responses.</td>
          <td>Manual string parsing required for JSON API communication, causing fragility and bugs.</td>
        </tr>
      </tbody>
    </table>
  `
};
