/**
 * DJ Mart AI Shopping Assistant Client
 * Features: Markdown parsing, interactive product links, quick suggestion chips, conversation history.
 */
document.addEventListener('DOMContentLoaded', function () {
    const chatBtn = document.getElementById('chatFloatingBtn');
    const chatDrawer = document.getElementById('chatDrawer');
    const chatCloseBtn = document.getElementById('chatCloseBtn');
    const chatClearBtn = document.getElementById('chatClearBtn');
    const chatForm = document.getElementById('chatForm');
    const chatInput = document.getElementById('chatInput');
    const chatMessages = document.getElementById('chatMessages');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const quickChips = document.querySelectorAll('.chat-chip');

    if (!chatBtn || !chatDrawer) return;

    function getContextPath() {
        const contextMeta = document.querySelector('meta[name="context-path"]');
        return contextMeta ? (contextMeta.getAttribute('content') || '') : (document.body.dataset.contextPath || '');
    }

    // Toggle drawer visibility
    chatBtn.addEventListener('click', function () {
        const isHidden = chatDrawer.style.display === 'none' || chatDrawer.style.display === '';
        chatDrawer.style.display = isHidden ? 'flex' : 'none';
        if (isHidden && chatInput) {
            chatInput.focus();
        }
    });

    if (chatCloseBtn) {
        chatCloseBtn.addEventListener('click', function () {
            chatDrawer.style.display = 'none';
        });
    }

    if (chatClearBtn) {
        chatClearBtn.addEventListener('click', function () {
            chatMessages.innerHTML = '';
            appendBotMessage('Conversation cleared. How can I assist you with DJ Mart products, prices, or orders today?');
        });
    }

    // Quick suggestion chips
    quickChips.forEach(chip => {
        chip.addEventListener('click', function () {
            const query = this.getAttribute('data-msg');
            if (query && chatInput) {
                chatInput.value = query;
                sendMessage(query);
            }
        });
    });

    // Simple markdown renderer for links, bold, italics, bullets
    function renderMarkdown(text) {
        const ctx = getContextPath();
        let formatted = text
            // Escape HTML entities
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            // Links: [Text](/path)
            .replace(/\[([^\]]+)\]\(([^)]+)\)/g, function(match, label, href) {
                const targetUrl = href.startsWith('/') ? ctx + href : href;
                return '<a href="' + targetUrl + '" class="chat-link" target="_self">' + label + '</a>';
            })
            // Bold **text**
            .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
            // Italic *text*
            .replace(/\*([^*]+)\*/g, '<em>$1</em>')
            // Code `code`
            .replace(/`([^`]+)`/g, '<code class="chat-code">$1</code>')
            // Linebreaks
            .replace(/\n/g, '<br/>');

        return formatted;
    }

    function appendUserMessage(text) {
        const msgDiv = document.createElement('div');
        msgDiv.className = 'chat-msg user';
        msgDiv.textContent = text;
        chatMessages.appendChild(msgDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return msgDiv;
    }

    function appendBotMessage(text) {
        const msgDiv = document.createElement('div');
        msgDiv.className = 'chat-msg bot';
        msgDiv.innerHTML = renderMarkdown(text);
        chatMessages.appendChild(msgDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return msgDiv;
    }

    async function sendMessage(message) {
        const cleanMessage = message.trim();
        if (!cleanMessage) return;

        appendUserMessage(cleanMessage);
        if (chatInput) chatInput.value = '';

        const typingIndicator = document.createElement('div');
        typingIndicator.className = 'chat-msg bot chat-typing';
        typingIndicator.innerHTML = '<em>Searching DJ Mart live catalog...</em>';
        chatMessages.appendChild(typingIndicator);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        if (chatSendBtn) chatSendBtn.disabled = true;

        try {
            const contextPath = getContextPath();
            const csrfMeta = document.querySelector('meta[name="csrf-token"]');
            const csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : '';

            const headers = {
                'Content-Type': 'application/json'
            };
            if (csrfToken) {
                headers['X-CSRF-Token'] = csrfToken;
            }

            const response = await fetch(contextPath + '/api/chat', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ message: cleanMessage })
            });

            const data = await response.json();
            if (typingIndicator.parentNode) {
                chatMessages.removeChild(typingIndicator);
            }

            if (response.ok && data.success) {
                const reply = (data.data && data.data.reply) ? data.data.reply : data.message;
                appendBotMessage(reply);
            } else {
                const errMsg = (data && data.message) ? data.message : 'I encountered an issue processing your query. Please try again.';
                appendBotMessage(errMsg);
            }
        } catch (err) {
            if (typingIndicator.parentNode) {
                chatMessages.removeChild(typingIndicator);
            }
            appendBotMessage('Unable to reach assistant. Please check your connection.');
        } finally {
            if (chatSendBtn) chatSendBtn.disabled = false;
            if (chatInput) chatInput.focus();
        }
    }

    // Form submission
    if (chatForm) {
        chatForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const message = chatInput ? chatInput.value.trim() : '';
            if (message) {
                sendMessage(message);
            }
        });
    }
});
