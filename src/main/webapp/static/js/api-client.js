/**
 * DJ Mart API Client
 * Centralized fetch client handling standard response envelopes, CSRF token attachment,
 * context path resolution, and error normalization.
 */
const ApiClient = {
  getContextPath() {
    const meta = document.querySelector('meta[name="context-path"]');
    return meta ? meta.getAttribute('content') : '';
  },

  getCsrfToken() {
    const meta = document.querySelector('meta[name="csrf-token"]');
    return meta ? meta.getAttribute('content') : '';
  },

  /**
   * Performs an HTTP request and parses the standard API response envelope.
   *
   * @param {string} url Endpoint URL
   * @param {object} options Request options (method, headers, body, signal)
   * @returns {Promise<any>} Resolves with data or rejects with normalized error object
   */
  async request(url, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = {
      'Accept': 'application/json',
      ...(options.headers || {})
    };

    // Attach Content-Type for payloads
    if (options.body && !(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }

    // Attach CSRF synchronizer token on state-changing methods
    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
      const csrf = this.getCsrfToken();
      if (csrf) {
        headers['X-CSRF-Token'] = csrf;
      }
    }

    // Prepend context path if URL starts with slash and not context path
    const contextPath = this.getContextPath();
    let finalUrl = url;
    if (contextPath && url.startsWith('/') && !url.startsWith(contextPath)) {
      finalUrl = contextPath + url;
    }

    const config = {
      ...options,
      method,
      headers
    };

    if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
      config.body = JSON.stringify(config.body);
    }

    try {
      const response = await fetch(finalUrl, config);
      let envelope;
      const text = await response.text();
      try {
        envelope = text ? JSON.parse(text) : {};
      } catch (jsonErr) {
        envelope = { success: response.ok, message: text };
      }

      if (!response.ok || envelope.success === false) {
        const error = envelope.error || {
          code: envelope.errorCode || 'HTTP_' + response.status,
          message: envelope.message || 'An unexpected error occurred'
        };
        error.status = response.status;
        throw error;
      }

      return envelope.data !== undefined ? envelope.data : envelope;
    } catch (err) {
      if (err.name !== 'AbortError') {
        console.error(`[ApiClient Error] ${method} ${finalUrl}:`, err);
      }
      throw err;
    }
  },

  get(url, options = {}) {
    return this.request(url, { ...options, method: 'GET' });
  },

  post(url, body, options = {}) {
    return this.request(url, { ...options, method: 'POST', body });
  },

  put(url, body, options = {}) {
    return this.request(url, { ...options, method: 'PUT', body });
  },

  delete(url, options = {}) {
    return this.request(url, { ...options, method: 'DELETE' });
  },

  getPlaceholderImage() {
    const cp = this.getContextPath();
    return (cp ? cp : '') + '/static/images/placeholder.svg';
  },

  resolveImageUrl(url) {
    if (!url || typeof url !== 'string' || !url.trim()) {
      return this.getPlaceholderImage();
    }
    const trimmed = url.trim();
    if (trimmed.startsWith('http://') || trimmed.startsWith('https://') || trimmed.startsWith('data:')) {
      return trimmed;
    }
    const cp = this.getContextPath();
    if (trimmed.startsWith('/')) {
      return (cp && !trimmed.startsWith(cp)) ? (cp + trimmed) : trimmed;
    }
    return (cp ? cp + '/' : '/') + trimmed;
  }
};
