/**
 * DJ Mart Shopping Bag Controller
 * Enforces server-authoritative pricing, stock validation, dynamic quantity updates,
 * item removal, and empty state toggling.
 */
document.addEventListener('DOMContentLoaded', () => {
  const cartActiveLayout = document.getElementById('cartActiveLayout');
  const cartEmptyState = document.getElementById('cartEmptyState');
  const cartRowsContainer = document.getElementById('cartRowsContainer');
  const cartItemsCount = document.getElementById('cartItemsCount');
  const summarySubtotal = document.getElementById('summarySubtotal');
  const summaryTotal = document.getElementById('summaryTotal');
  const clearCartBtn = document.getElementById('clearCartBtn');

  let isUpdating = false;

  // 1. Update Cart UI with Authoritative Server Response
  function updateCartUI(cart) {
    if (!cart || !cart.items || cart.items.length === 0) {
      if (cartActiveLayout) cartActiveLayout.style.display = 'none';
      if (cartEmptyState) cartEmptyState.style.display = 'block';
      updateNavbarBadge(0);
      return;
    }

    if (cartActiveLayout) cartActiveLayout.style.display = 'grid';
    if (cartEmptyState) cartEmptyState.style.display = 'none';

    if (cartItemsCount) cartItemsCount.textContent = cart.totalItems;

    const formattedTotal = Number(cart.grandTotal).toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });

    if (summarySubtotal) summarySubtotal.textContent = `₹${formattedTotal}`;
    if (summaryTotal) summaryTotal.textContent = `₹${formattedTotal}`;

    updateNavbarBadge(cart.totalItems);

    // Re-render rows
    if (cartRowsContainer) {
      cartRowsContainer.innerHTML = cart.items.map(item => {
        const itemImg = item.productImageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&auto=format&fit=crop';
        const formattedUnitPrice = Number(item.unitPrice).toLocaleString('en-IN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });
        const formattedSubtotal = Number(item.subtotal).toLocaleString('en-IN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });

        return `
          <div class="cart-item-row" data-cart-item-id="${item.id}">
            <img src="${escapeHtml(itemImg)}" alt="${escapeHtml(item.productName)}" class="cart-item-image">
            <div>
              <div class="cart-item-title">${escapeHtml(item.productName)}</div>
              <div class="cart-item-price">Unit: ₹${formattedUnitPrice}</div>
            </div>
            <div>
              <div class="quantity-selector-wrap">
                <button type="button"
                        class="qty-btn cart-qty-decrease"
                        data-cart-item-id="${item.id}"
                        aria-label="Decrease quantity"
                        ${item.quantity <= 1 ? 'disabled' : ''}>
                  &minus;
                </button>
                <input type="number"
                       class="qty-input cart-qty-input"
                       data-cart-item-id="${item.id}"
                       value="${item.quantity}"
                       min="1"
                       readonly
                       aria-label="Quantity">
                <button type="button"
                        class="qty-btn cart-qty-increase"
                        data-cart-item-id="${item.id}"
                        aria-label="Increase quantity">
                  &plus;
                </button>
              </div>
            </div>
            <div class="cart-item-subtotal">₹${formattedSubtotal}</div>
            <div>
              <button type="button"
                      class="cart-item-remove-btn cart-remove-item"
                      data-cart-item-id="${item.id}"
                      aria-label="Remove ${escapeHtml(item.productName)} from cart">
                Remove
              </button>
            </div>
          </div>
        `;
      }).join('');
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function updateNavbarBadge(count) {
    const badge = document.getElementById('navCartBadge');
    if (badge) {
      if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-block';
      } else {
        badge.style.display = 'none';
      }
    }
  }

  // 2. Quantity Decrement & Increment Event Delegation
  if (cartRowsContainer) {
    cartRowsContainer.addEventListener('click', async (e) => {
      if (isUpdating) return;

      const decBtn = e.target.closest('.cart-qty-decrease');
      const incBtn = e.target.closest('.cart-qty-increase');
      const removeBtn = e.target.closest('.cart-remove-item');

      if (decBtn) {
        const cartItemId = parseInt(decBtn.dataset.cartItemId, 10);
        const row = decBtn.closest('.cart-item-row');
        const input = row.querySelector('.cart-qty-input');
        const currentQty = parseInt(input.value, 10);

        if (currentQty > 1) {
          await updateQuantity(cartItemId, currentQty - 1);
        }
      } else if (incBtn) {
        const cartItemId = parseInt(incBtn.dataset.cartItemId, 10);
        const row = incBtn.closest('.cart-item-row');
        const input = row.querySelector('.cart-qty-input');
        const currentQty = parseInt(input.value, 10);

        await updateQuantity(cartItemId, currentQty + 1);
      } else if (removeBtn) {
        const cartItemId = parseInt(removeBtn.dataset.cartItemId, 10);
        await removeItem(cartItemId);
      }
    });
  }

  async function updateQuantity(cartItemId, newQty) {
    isUpdating = true;
    try {
      const updatedCart = await ApiClient.post('/api/cart/update', {
        cartItemId,
        quantity: newQty
      });
      updateCartUI(updatedCart);
      DjMartToast.info('Bag quantity updated.');
    } catch (err) {
      DjMartToast.error(err.message || 'Unable to update quantity.');
      // Refresh current cart to reset UI to server state
      refreshCart();
    } finally {
      isUpdating = false;
    }
  }

  async function removeItem(cartItemId) {
    isUpdating = true;
    try {
      const updatedCart = await ApiClient.post('/api/cart/remove', {
        cartItemId
      });
      updateCartUI(updatedCart);
      DjMartToast.success('Item removed from shopping bag.');
    } catch (err) {
      DjMartToast.error(err.message || 'Could not remove item.');
    } finally {
      isUpdating = false;
    }
  }

  async function refreshCart() {
    try {
      const cart = await ApiClient.get('/api/cart');
      updateCartUI(cart);
    } catch (err) {
      console.warn('Could not refresh cart:', err);
    }
  }

  // 3. Clear Cart
  if (clearCartBtn) {
    clearCartBtn.addEventListener('click', async () => {
      if (isUpdating) return;
      isUpdating = true;
      try {
        const emptyCart = await ApiClient.post('/api/cart/clear');
        updateCartUI(emptyCart);
        DjMartToast.info('Shopping bag cleared.');
      } catch (err) {
        DjMartToast.error(err.message || 'Unable to clear bag.');
      } finally {
        isUpdating = false;
      }
    });
  }
});
