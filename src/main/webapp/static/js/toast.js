/**
 * DJ Mart Toast Notifications
 * Lightweight, accessible notification toasts matching the classical luxury design.
 */
const DjMartToast = {
  getContainer() {
    let container = document.getElementById('toastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toastContainer';
      container.className = 'toast-container';
      container.setAttribute('aria-live', 'polite');
      container.setAttribute('aria-atomic', 'true');
      document.body.appendChild(container);
    }
    return container;
  },

  show(message, type = 'info', duration = 4000) {
    const container = this.getContainer();
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.setAttribute('role', 'status');

    const msgSpan = document.createElement('span');
    msgSpan.textContent = message;
    toast.appendChild(msgSpan);

    const closeBtn = document.createElement('button');
    closeBtn.className = 'btn-close';
    closeBtn.innerHTML = '&times;';
    closeBtn.style.background = 'none';
    closeBtn.style.border = 'none';
    closeBtn.style.cursor = 'pointer';
    closeBtn.style.fontSize = '1.2rem';
    closeBtn.style.lineHeight = '1';
    closeBtn.style.color = '#888';
    closeBtn.setAttribute('aria-label', 'Close notification');
    closeBtn.onclick = () => {
      toast.remove();
    };
    toast.appendChild(closeBtn);

    container.appendChild(toast);

    if (duration > 0) {
      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
      }, duration);
    }
  },

  success(msg) {
    this.show(msg, 'success');
  },

  error(msg) {
    this.show(msg, 'error', 5000);
  },

  warning(msg) {
    this.show(msg, 'warning');
  },

  info(msg) {
    this.show(msg, 'info');
  }
};

// Backward compatibility alias
const RajamaniToast = DjMartToast;
