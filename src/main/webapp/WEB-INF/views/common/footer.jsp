</main>
<footer class="site-footer">
    <div class="container" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1.5rem;">
        <div style="display: flex; align-items: center; gap: 1.25rem;">
            <a href="${pageContext.request.contextPath}/" aria-label="DJ Mart Homepage">
                <img src="${pageContext.request.contextPath}/static/images/dj_mart_logo.jpg" alt="DJ Mart" class="footer-logo-img">
            </a>
            <div>
                <p style="margin: 0; font-weight: 600;">&copy; 2026 DJ Mart Inc. All rights reserved.</p>
                <p style="margin: 0.25rem 0 0; font-size: 0.85rem; color: #90a4ae;">Quality products at verified prices</p>
            </div>
        </div>
        <div>
            <span style="font-size: 0.85rem; color: #b0bec5;">Built with Java Servlets, JDBC &amp; Tomcat</span>
        </div>
    </div>
</footer>

<!-- AI Concierge Floating Button & Drawer -->
<button id="chatFloatingBtn" class="chat-floating-btn" aria-label="Open AI Concierge" title="Ask DJ Mart Assistant">💬</button>

<div id="chatDrawer" class="chat-drawer" style="display: none;">
    <div class="chat-header">
        <div style="display: flex; align-items: center; gap: 10px;">
            <img src="${pageContext.request.contextPath}/static/images/dj_mart_logo.jpg" alt="DJ Mart" style="height: 28px; width: auto; border-radius: 4px; object-fit: contain;">
            <div>
                <strong style="font-size: 0.95rem; display: block; line-height: 1.2;">DJ Mart AI Assistant</strong>
                <span style="font-size: 0.72rem; color: #4ade80;">● Live Catalog Connected</span>
            </div>
        </div>
        <div style="display: flex; align-items: center; gap: 6px;">
            <button id="chatClearBtn" class="chat-clear-btn" title="Clear Conversation" style="background: none; border: none; color: #94a3b8; font-size: 0.8rem; cursor: pointer; padding: 2px 6px;">Clear</button>
            <button id="chatCloseBtn" class="chat-close-btn" aria-label="Close Chat">&times;</button>
        </div>
    </div>
    <div class="chat-quick-suggestions" id="chatQuickSuggestions">
        <button type="button" class="chat-chip" data-msg="Show me products under ₹1,000">Under ₹1,000</button>
        <button type="button" class="chat-chip" data-msg="What is the price of Sony headphones?">Sony Price</button>
        <button type="button" class="chat-chip" data-msg="Is Keychron keyboard in stock?">Check Stock</button>
        <button type="button" class="chat-chip" data-msg="Show me electronics">Electronics</button>
        <button type="button" class="chat-chip" data-msg="What is your shipping and return policy?">Policies</button>
    </div>
    <div id="chatMessages" class="chat-messages">
        <div class="chat-msg bot">
            Hello! I am your <strong>DJ Mart AI Shopping Assistant</strong>. Ask me about our verified catalog in INR (₹), live stock, specific product prices, comparisons, or shipping policies!
        </div>
    </div>
    <form id="chatForm" class="chat-input-bar">
        <input type="text" id="chatInput" placeholder="Ask about prices, stock, budget..." maxlength="500" autocomplete="off" />
        <button type="submit" id="chatSendBtn" class="chat-send-btn" aria-label="Send Message">&#10148;</button>
    </form>
</div>

<script src="${pageContext.request.contextPath}/static/js/api-client.js"></script>
<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
<script src="${pageContext.request.contextPath}/static/js/chat.js"></script>
</body>
</html>
