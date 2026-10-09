/**
 * DJ Mart Main Application JavaScript
 * Orchestrates navigation drawer toggle and initial session cart badge synchronization.
 */
document.addEventListener('DOMContentLoaded', () => {
  // Mobile Navigation Drawer Toggle
  const toggleBtn = document.getElementById('mobileMenuToggle');
  const navContainer = document.getElementById('navContainer');

  if (toggleBtn && navContainer) {
    toggleBtn.addEventListener('click', () => {
      const isOpen = navContainer.classList.toggle('open');
      toggleBtn.setAttribute('aria-expanded', isOpen);
    });

    document.addEventListener('click', (e) => {
      if (!toggleBtn.contains(e.target) && !navContainer.contains(e.target)) {
        navContainer.classList.remove('open');
        toggleBtn.setAttribute('aria-expanded', 'false');
      }
    });
  }

  // Sync Navbar Cart Count from Server
  async function syncCartBadge() {
    const badge = document.getElementById('navCartBadge');
    if (!badge) return;

    try {
      const cart = await ApiClient.get('/api/cart');
      if (cart && cart.totalItems > 0) {
        badge.textContent = cart.totalItems;
        badge.style.display = 'inline-block';
      } else {
        badge.style.display = 'none';
      }
    } catch (ignored) {
      // User may be unauthenticated guest, ignore silently
    }
  }

  syncCartBadge();
});
