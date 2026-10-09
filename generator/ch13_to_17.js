// generator/ch13_to_17.js
// Chapter 13: CSS & Design, Chapter 14: Responsive, Chapter 15: Security, Chapter 16: Error Handling, Chapter 17: Issues & Improvements

module.exports = {
  getChapter13: () => `
    <div class="page-break"></div>
    <h1>13. CSS &amp; Luxury Design System</h1>

    <p>
      DJ Mart is crafted with a distinctive luxury design aesthetic, delivering an elevated, premium shopping experience reminiscent of world-class design houses and flagship boutique marketplaces.
    </p>

    <h2>13.1 Color Palette &amp; Typography</h2>
    <p>All design tokens and theme values are declared as CSS custom variables in <code>src/main/webapp/static/css/style.css</code>:</p>

    <table>
      <thead>
        <tr>
          <th>Design Variable</th>
          <th>Hex / Value</th>
          <th>Usage &amp; Visual Intent</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><code>--color-obsidian</code></td>
          <td><code>#0F172A</code> (Deep Obsidian Slate)</td>
          <td>Primary typography, brand headers, and prominent high-contrast buttons.</td>
        </tr>
        <tr>
          <td><code>--color-gold-accent</code></td>
          <td><code>#C5A880 / #9A7B4F</code> (Warm Champagne Gold)</td>
          <td>Discount badges, accent borders, pricing highlights, and trust indicators.</td>
        </tr>
        <tr>
          <td><code>--color-ivory-bg</code></td>
          <td><code>#FBF9F5 / #FAFAF8</code> (Ivory Off-White)</td>
          <td>Soft, non-glare canvas background providing premium visual contrast.</td>
        </tr>
        <tr>
          <td><code>--font-display</code></td>
          <td><code>'Playfair Display', serif</code></td>
          <td>Refined editorial serif for hero headlines, section titles, and badges.</td>
        </tr>
        <tr>
          <td><code>--font-body</code></td>
          <td><code>'Plus Jakarta Sans', sans-serif</code></td>
          <td>Clean, highly legible geometric sans-serif for product specifications and body copy.</td>
        </tr>
      </tbody>
    </table>

    <h2>13.2 Micro-Interactions &amp; Hover Effects</h2>
    <ul>
      <li><strong>Card Elevation:</strong> Hovering over a product card initiates a smooth <code>translateY(-4px)</code> lift paired with a subtle, diffused warm shadow.</li>
      <li><strong>Button Transitions:</strong> Interactive elements use a standardized <code>all 0.25s cubic-bezier(...)</code> transition curve, ensuring polished feedback without distracting animations.</li>
    </ul>
  `,

  getChapter14: () => `
    <div class="page-break"></div>
    <h1>14. Mobile &amp; Responsive Adaptation</h1>

    <p>
      DJ Mart is engineered around a <strong>Mobile-First Responsive Design</strong> philosophy, guaranteeing fluid visual fidelity across smartphones, tablets, laptops, and ultra-wide desktop monitors.
    </p>

    <h2>14.1 Breakpoint Strategy</h2>
    <p>All viewport adaptations are cleanly organized in <code>style.css</code> using standardized CSS media queries:</p>
    <ul>
      <li><strong>Mobile Devices (<code>&lt; 768px</code>):</strong>
        <ul>
          <li>Navbar switches to an accessible slide-out mobile drawer with touch-friendly spacing.</li>
          <li>Product catalog transitions to a compact 1 or 2-column grid layout.</li>
          <li>All tap targets satisfy accessibility standards with minimum dimensions of <strong>44&times;44 pixels</strong>.</li>
        </ul>
      </li>
      <li><strong>Tablets &amp; Medium Displays (<code>768px - 1024px</code>):</strong> 2 to 3 column catalog grid with dynamic container margins.</li>
      <li><strong>Desktop &amp; Large Displays (<code>&gt; 1024px</code>):</strong> Full-width luxury hero layout, 4-column product grid, and expansive filter sidebars.</li>
    </ul>
  `,

  getChapter15: () => `
    <div class="page-break"></div>
    <h1>15. Security Architecture (OWASP Protections)</h1>

    <p>
      Security is fundamental to e-commerce trust and financial integrity. DJ Mart is built with strict adherence to international <strong>OWASP Top 10</strong> security standards.
    </p>

    <h2>15.1 Core Defense Layers</h2>
    <table>
      <thead>
        <tr>
          <th>Security Threat</th>
          <th>DJ Mart Defense Mechanism</th>
          <th>Implementation Location</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>SQL Injection (SQLi)</strong></td>
          <td>100% parameter binding using parameterized <code>PreparedStatement</code> queries across all persistence calls; zero string concatenation.</td>
          <td>All <code>*DAOImpl.java</code> classes</td>
        </tr>
        <tr>
          <td><strong>Cross-Site Scripting (XSS)</strong></td>
          <td>All dynamic model data rendered in JSPs is safely HTML-escaped via JSTL <code>&lt;c:out&gt;</code> tags and contextual sanitization.</td>
          <td>All <code>.jsp</code> view templates</td>
        </tr>
        <tr>
          <td><strong>Cross-Site Request Forgery (CSRF)</strong></td>
          <td>Cryptographic 64-character hex synchronizer tokens generated per session and validated on state-changing POST/PUT/DELETE requests.</td>
          <td><code>CsrfFilter.java</code> &amp; <code>SecurityUtil.java</code></td>
        </tr>
        <tr>
          <td><strong>Credential Theft</strong></td>
          <td>Passwords are never stored in plain text. Robust <strong>jBCrypt</strong> hashing with 10 salt rounds protects all stored credentials.</td>
          <td><code>PasswordUtil.java</code></td>
        </tr>
        <tr>
          <td><strong>Broken Authentication &amp; RBAC</strong></td>
          <td><code>AuthFilter</code> validates active sessions and enforces strict Role-Based Access Control (BUYER, SELLER, ADMIN) per endpoint.</td>
          <td><code>AuthFilter.java</code></td>
        </tr>
        <tr>
          <td><strong>Insecure Direct Object Reference (IDOR)</strong></td>
          <td>All seller operations and order modifications verify tenant ownership using explicit <code>WHERE user_id = ?</code> database constraints.</td>
          <td><code>OrderServiceImpl.java</code>, <code>SellerServlet.java</code></td>
        </tr>
        <tr>
          <td><strong>Price Tampering</strong></td>
          <td>Client-submitted price fields are never accepted during checkout; order totals are computed strictly from database prices on the server.</td>
          <td><code>OrderServiceImpl.java: placeOrder()</code></td>
        </tr>
        <tr>
          <td><strong>Session Fixation</strong></td>
          <td>Upon successful authentication, <code>request.changeSessionId()</code> rotates the session ID; session cookies are marked <code>HttpOnly</code> and <code>SameSite=Lax</code>.</td>
          <td><code>AuthServlet.java</code> &amp; <code>web.xml</code></td>
        </tr>
      </tbody>
    </table>
  `,

  getChapter16: () => `
    <div class="page-break"></div>
    <h1>16. Error Handling Architecture</h1>

    <p>
      DJ Mart implements a structured, multi-tier exception handling architecture ensuring raw stack traces and internal server details are never exposed to clients.
    </p>

    <h2>16.1 Multi-Tier Exception Management</h2>
    <ul>
      <li><strong>Custom Domain Exceptions:</strong>
        <ul>
          <li><code>ValidationException</code> (400): Thrown for invalid client input or missing required parameters.</li>
          <li><code>AuthenticationException</code> (401): Thrown for invalid credentials or unauthenticated session access.</li>
          <li><code>AuthorizationException</code> (403): Thrown when accessing restricted administrative or seller resources.</li>
          <li><code>ResourceNotFoundException</code> (404): Thrown when requested products or orders do not exist in the database.</li>
          <li><code>InsufficientStockException</code> (409): Thrown when inventory is insufficient during checkout.</li>
        </ul>
      </li>
      <li><strong>Branded Error Pages:</strong> In unexpected system errors or route misses, Tomcat dispatches to branded, user-friendly <code>404.jsp</code> and <code>500.jsp</code> views.</li>
      <li><strong>Standardized REST API Error Schema:</strong> REST endpoints return consistent JSON error envelopes: <code>{ "success": false, "message": "..." }</code>.</li>
    </ul>
  `,

  getChapter17: () => `
    <div class="page-break"></div>
    <h1>17. Architecture Evaluation &amp; Future Roadmap</h1>

    <p>
      An architectural assessment of the DJ Mart codebase highlights several key strengths as well as scalable recommendations for future enterprise expansion:
    </p>

    <table>
      <thead>
        <tr>
          <th>Current Implementation</th>
          <th>Scalability Recommendation</th>
          <th>Strategic Value &amp; Impact</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>Embedded <strong>H2 Database</strong> for lightweight local and embedded execution.</td>
          <td>Migrate to managed <strong>PostgreSQL</strong> for cloud production deployments.</td>
          <td>Enables multi-node container clustering, horizontal read replicas, and enterprise backup guarantees.</td>
        </tr>
        <tr>
          <td>Curated CDN product media URLs.</td>
          <td>Integrate cloud object storage (e.g., <strong>AWS S3</strong> or <strong>Cloudflare R2</strong>) with direct upload signatures.</td>
          <td>Allows merchants to upload original high-resolution product photography securely from their dashboard.</td>
        </tr>
        <tr>
          <td>Cash on Delivery (COD) and simulation checkout flows.</td>
          <td>Integrate production payment gateways (e.g., <strong>Razorpay</strong> or <strong>Stripe</strong>).</td>
          <td>Supports real-time UPI, credit card, and net banking transactions with automated webhooks.</td>
        </tr>
        <tr>
          <td>Direct database reads on frequent catalog queries.</td>
          <td>Introduce distributed caching via <strong>Redis</strong> for catalog and session state.</td>
          <td>Yields sub-millisecond catalog response times and reduces database load during flash sale traffic spikes.</td>
        </tr>
      </tbody>
    </table>
  `
};
