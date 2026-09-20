/**
 * DjMart Product Details Controller
 * Handles image gallery thumbnail swapping, accessible quantity stepper limits,
 * Add to Cart API interactions, and direct Buy Now checkout navigation.
 */
document.addEventListener('DOMContentLoaded', () => {
  const mainProductImg = document.getElementById('mainProductImg');
  const galleryThumbnails = document.getElementById('galleryThumbnails');
  const decreaseQtyBtn = document.getElementById('decreaseQtyBtn');
  const increaseQtyBtn = document.getElementById('increaseQtyBtn');
  const productQtyInput = document.getElementById('productQty');
  const addToCartBtn = document.getElementById('detailsAddToCartBtn');
  const buyNowBtn = document.getElementById('detailsBuyNowBtn');

  // 1. Gallery Thumbnail Interaction
  if (galleryThumbnails && mainProductImg) {
    galleryThumbnails.addEventListener('click', (e) => {
      const thumb = e.target.closest('.gallery-thumbnail');
      if (!thumb) return;

      document.querySelectorAll('.gallery-thumbnail').forEach(t => t.classList.remove('active'));
      thumb.classList.add('active');

      const newSrc = thumb.dataset.imgSrc;
      if (newSrc) {
        mainProductImg.style.opacity = '0.5';
        mainProductImg.style.transition = 'opacity 0.15s ease';
        setTimeout(() => {
          mainProductImg.src = newSrc;
          mainProductImg.style.opacity = '1';
        }, 150);
      }
    });
  }

  // 2. Quantity Stepper Controls
  if (productQtyInput && decreaseQtyBtn && increaseQtyBtn) {
    const minQty = 1;
    const maxQty = parseInt(productQtyInput.getAttribute('max'), 10) || 999;

    function updateQtyButtons(val) {
      decreaseQtyBtn.disabled = val <= minQty;
      increaseQtyBtn.disabled = val >= maxQty;
    }

    decreaseQtyBtn.addEventListener('click', () => {
      let currentVal = parseInt(productQtyInput.value, 10) || minQty;
      if (currentVal > minQty) {
        currentVal -= 1;
        productQtyInput.value = currentVal;
        updateQtyButtons(currentVal);
      }
    });

    increaseQtyBtn.addEventListener('click', () => {
      let currentVal = parseInt(productQtyInput.value, 10) || minQty;
      if (currentVal < maxQty) {
        currentVal += 1;
        productQtyInput.value = currentVal;
        updateQtyButtons(currentVal);
      }
    });

    updateQtyButtons(parseInt(productQtyInput.value, 10) || minQty);
  }

  // 3. Add to Cart
  if (addToCartBtn) {
    addToCartBtn.addEventListener('click', async () => {
      const productId = parseInt(addToCartBtn.dataset.productId, 10);
      const quantity = parseInt(productQtyInput.value, 10) || 1;

      const origText = addToCartBtn.textContent;
      addToCartBtn.disabled = true;
      addToCartBtn.textContent = 'Adding to Bag...';

      try {
        const updatedCart = await ApiClient.post('/api/cart/add', {
          productId,
          quantity
        });

        DjMartToast.success(`Added ${quantity} item${quantity > 1 ? 's' : ''} to your shopping bag.`);
        updateNavbarBadge(updatedCart ? updatedCart.totalItems : 0);
      } catch (err) {
        if (err.status === 401) {
          const contextPath = ApiClient.getContextPath();
          window.location.href = `${contextPath}/auth/login?redirect=${encodeURIComponent(window.location.pathname)}`;
        } else {
          DjMartToast.error(err.message || 'Unable to add item to bag.');
        }
      } finally {
        addToCartBtn.disabled = false;
        addToCartBtn.textContent = origText;
      }
    });
  }

  // 4. Buy Now
  if (buyNowBtn) {
    buyNowBtn.addEventListener('click', async () => {
      const productId = parseInt(buyNowBtn.dataset.productId, 10);
      const quantity = parseInt(productQtyInput.value, 10) || 1;

      const origText = buyNowBtn.textContent;
      buyNowBtn.disabled = true;
      buyNowBtn.textContent = 'Securing item...';

      try {
        await ApiClient.post('/api/cart/add', {
          productId,
          quantity
        });

        const contextPath = ApiClient.getContextPath();
        window.location.href = `${contextPath}/cart`;
      } catch (err) {
        if (err.status === 401) {
          const contextPath = ApiClient.getContextPath();
          window.location.href = `${contextPath}/auth/login?redirect=${encodeURIComponent(window.location.pathname)}`;
        } else {
          DjMartToast.error(err.message || 'Unable to proceed with Buy Now.');
          buyNowBtn.disabled = false;
          buyNowBtn.textContent = origText;
        }
      }
    });
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
});
