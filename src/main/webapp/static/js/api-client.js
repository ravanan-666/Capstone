/**
 * DjMart API Client
 * Centralized fetch client handling standard response envelopes and error normalization.
 */
const ApiClient = {
  /**
   * Performs an HTTP request and parses the standard API response envelope.
   *
   * @param {string} url Endpoint URL
   * @param {object} options Request options (method, headers, body)
   * @returns {Promise<any>} Resolves with data or rejects with error object
   */
  async request(url, options = {}) {
    const defaultHeaders = {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...(options.headers || {})
      }
    };

    if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
      config.body = JSON.stringify(config.body);
    }

    try {
      const response = await fetch(url, config);
      const envelope = await response.json();

      if (!response.ok || !envelope.success) {
        const error = envelope.error || {
          code: 'HTTP_' + response.status,
          message: 'An unexpected server error occurred'
        };
        error.status = response.status;
        throw error;
      }

      return envelope.data;
    } catch (err) {
      console.error(`[ApiClient Error] ${options.method || 'GET'} ${url}:`, err);
      throw err;
    }
  },

  get(url, headers = {}) {
    return this.request(url, { method: 'GET', headers });
  },

  post(url, body, headers = {}) {
    return this.request(url, { method: 'POST', body, headers });
  },

  put(url, body, headers = {}) {
    return this.request(url, { method: 'PUT', body, headers });
  },

  delete(url, headers = {}) {
    return this.request(url, { method: 'DELETE', headers });
  }
};
