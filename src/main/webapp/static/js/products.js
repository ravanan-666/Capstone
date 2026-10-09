/**
 * DJ Mart Products Catalog Controller
 * Manages debounced search, category/price filters, sorting, backend pagination,
 * URL query state synchronization, and dynamic product card rendering.
 */
document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('searchInput');
  const categorySelect = document.getElementById('categorySelect');
  const minPriceInput = document.getElementById('minPriceInput');
  const maxPriceInput = document.getElementById('maxPriceInput');
  const sortSelect = document.getElementById('sortSelect');
  const clearFiltersBtn = document.getElementById('clearFiltersBtn');
  const clearFiltersEmptyBtn = document.getElementById('clearFiltersEmptyBtn');
  const productsGrid = document.getElementById('productsGrid');
  const emptyCatalog = document.getElementById('emptyCatalog');
  const currentCount = document.getElementById('currentCount');
  const loadingIndicator = document.getElementById('loadingIndicator');
  const paginationNav = document.getElementById('paginationNav');
  const prevPageBtn = document.getElementById('prevPageBtn');
  const nextPageBtn = document.getElementById('nextPageBtn');
  const pageNumbersContainer = document.getElementById('pageNumbersContainer');

  let debounceTimer = null;
  let activeAbortController = null;
  let currentPage = 1;
  const pageSize = 12;

  // 1. Initialize State from Current URL Query Params
  function initFromUrl() {
    const params = new URLSearchParams(window.location.search);
    if (params.has('search')) searchInput.value = params.get('search');
    if (params.has('category')) categorySelect.value = params.get('category');
    if (params.has('minPrice')) minPriceInput.value = params.get('minPrice');
    if (params.has('maxPrice')) maxPriceInput.value = params.get('maxPrice');
    if (params.has('sort')) sortSelect.value = params.get('sort');
    if (params.has('page')) currentPage = parseInt(params.get('page'), 10) || 1;
  }

  // 2. Fetch Catalog Results from Backend
  async function fetchProducts(page = 1, updateUrl = true) {
    currentPage = page;

    if (activeAbortController) {
      activeAbortController.abort();
    }
    activeAbortController = new AbortController();

    const params = new URLSearchParams();
    const searchVal = searchInput ? searchInput.value.trim() : '';
    const catVal = categorySelect ? categorySelect.value.trim() : '';
    const minVal = minPriceInput ? minPriceInput.value.trim() : '';
    const maxVal = maxPriceInput ? maxPriceInput.value.trim() : '';
    const sortVal = sortSelect ? sortSelect.value.trim() : 'newest';

    if (searchVal) params.append('search', searchVal);
    if (catVal) params.append('category', catVal);
    if (minVal) params.append('minPrice', minVal);
    if (maxVal) params.append('maxPrice', maxVal);
    if (sortVal) params.append('sort', sortVal);
    params.append('page', page);
    params.append('size', pageSize);

    if (updateUrl) {
      const newUrl = `${window.location.pathname}?${params.toString()}`;
      window.history.pushState({ page, searchVal, catVal, minVal, maxVal, sortVal }, '', newUrl);
    }

    if (loadingIndicator) loadingIndicator.style.display = 'block';

    try {
      const data = await ApiClient.get(`/api/products?${params.toString()}`, {
        signal: activeAbortController.signal
      });

      renderProducts(data);
    } catch (err) {
      if (err.name !== 'AbortError') {
        DjMartToast.error(err.message || 'Unable to load products. Please check your connection.');
      }
    } finally {
      if (loadingIndicator) loadingIndicator.style.display = 'none';
    }
  }

  // 3. Render Product Cards into DOM
  function renderProducts(pageResult) {
    if (!productsGrid) return;
    const items = (pageResult && pageResult.data) ? pageResult.data : [];
    const total = pageResult ? pageResult.totalElements : 0;

    if (currentCount) currentCount.textContent = total;

    if (items.length === 0) {
      productsGrid.innerHTML = '';
      if (emptyCatalog) emptyCatalog.style.display = 'block';
      if (paginationNav) paginationNav.style.display = 'none';
      return;
    }

    if (emptyCatalog) emptyCatalog.style.display = 'none';

    const contextPath = ApiClient.getContextPath();
    const placeholderImg = ApiClient.getPlaceholderImage();
    const html = items.map(p => {
      const img = ApiClient.resolveImageUrl(p.imageUrl);
      const ratingVal = (p.averageRating && p.averageRating > 0) ? p.averageRating.toFixed(1) : 'New';
      const reviewCount = p.reviewCount || 0;
      const formattedPrice = Number(p.price).toLocaleString('en-IN', { maximumFractionDigits: 0 });

      let stockBadge = '<span class="badge badge-in-stock">In Stock</span>';
      if (p.stockQty <= 0) {
        stockBadge = '<span class="badge badge-out-of-stock">Out of Stock</span>';
      } else if (p.stockQty <= 5) {
        stockBadge = `<span class="badge badge-low-stock">Only ${p.stockQty} left</span>`;
      }

      const brandHtml = p.brand ? `<div class="product-brand">${escapeHtml(p.brand)}</div>` : '';
      let discountHtml = '';
      if (p.originalPrice && Number(p.originalPrice) > Number(p.price)) {
        const origFormatted = Number(p.originalPrice).toLocaleString('en-IN', { maximumFractionDigits: 0 });
        const discountPct = p.discountPercent || Math.round(((p.originalPrice - p.price) / p.originalPrice) * 100);
        discountHtml = `
          <span class="product-original-price">₹${origFormatted}</span>
          <span class="product-discount-badge">${discountPct}% OFF</span>
        `;
      }

      let actionHtml = '';
      if (p.stockQty <= 0) {
        actionHtml = '<button type="button" class="btn btn-outline btn-block" disabled>Unavailable</button>';
      } else {
        actionHtml = `
          <button type="button" class="btn btn-primary btn-block add-to-cart-btn" data-product-id="${p.id}" aria-label="Add ${escapeHtml(p.name)} to Cart" style="flex: 1;">Add to Cart</button>
          <a href="${contextPath}/products/${p.id}" class="btn btn-outline btn-buy-now" style="padding: 0.625rem 0.75rem; font-size: 0.85rem;" title="View & Buy Now">Buy &rarr;</a>
        `;
      }

      return `
        <article class="product-card" data-product-id="${p.id}">
          <div class="product-card-image-wrap">
            <a href="${contextPath}/products/${p.id}">
              <img src="${escapeHtml(img)}" alt="${escapeHtml(p.name)}" class="product-card-image" loading="lazy" onerror="if(this.src!=='${escapeHtml(placeholderImg)}'){this.onerror=null;this.src='${escapeHtml(placeholderImg)}';}">
            </a>
          </div>
          <div class="product-card-content">
            ${brandHtml}
            <div class="product-category">${escapeHtml(p.category)}</div>
            <h3 class="product-title">
              <a href="${contextPath}/products/${p.id}">${escapeHtml(p.name)}</a>
            </h3>
            <div class="product-rating">
              <span>★</span>
              <span>${ratingVal}</span>
              <span class="product-rating-count">(${reviewCount})</span>
            </div>
            <div class="product-price-stock">
              <div class="product-price-wrap">
                <span class="product-price">₹${formattedPrice}</span>
                ${discountHtml}
              </div>
              ${stockBadge}
            </div>
            <div class="product-actions">
              ${actionHtml}
            </div>
          </div>
        </article>
      `;
    }).join('');

    productsGrid.innerHTML = html;
    renderPagination(pageResult);
  }

  // 4. Render Pagination Controls
  function renderPagination(pageResult) {
    if (!paginationNav || !pageNumbersContainer) return;

    const totalPages = pageResult ? pageResult.totalPages : 0;
    const page = pageResult ? pageResult.page : 1;

    if (totalPages <= 1) {
      paginationNav.style.display = 'none';
      return;
    }

    paginationNav.style.display = 'flex';
    if (prevPageBtn) prevPageBtn.disabled = page <= 1;
    if (nextPageBtn) nextPageBtn.disabled = page >= totalPages;

    let pagesHtml = '';
    for (let i = 1; i <= totalPages; i++) {
      pagesHtml += `
        <button type="button" class="page-btn ${page === i ? 'active' : ''}" data-page="${i}">
          ${i}
        </button>
      `;
    }
    pageNumbersContainer.innerHTML = pagesHtml;
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

  // 5. Event Listeners
  if (searchInput) {
    searchInput.addEventListener('input', () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        fetchProducts(1);
      }, 350);
    });
  }

  if (categorySelect) {
    categorySelect.addEventListener('change', () => fetchProducts(1));
  }

  if (minPriceInput) {
    minPriceInput.addEventListener('change', () => fetchProducts(1));
  }

  if (maxPriceInput) {
    maxPriceInput.addEventListener('change', () => fetchProducts(1));
  }

  if (sortSelect) {
    sortSelect.addEventListener('change', () => fetchProducts(1));
  }

  function resetFilters() {
    if (searchInput) searchInput.value = '';
    if (categorySelect) categorySelect.value = '';
    if (minPriceInput) minPriceInput.value = '';
    if (maxPriceInput) maxPriceInput.value = '';
    if (sortSelect) sortSelect.value = 'newest';
    fetchProducts(1);
  }

  if (clearFiltersBtn) clearFiltersBtn.addEventListener('click', resetFilters);
  if (clearFiltersEmptyBtn) clearFiltersEmptyBtn.addEventListener('click', resetFilters);

  // Pagination click delegation
  if (paginationNav) {
    paginationNav.addEventListener('click', (e) => {
      const pageBtn = e.target.closest('[data-page]');
      if (pageBtn) {
        const targetPage = parseInt(pageBtn.dataset.page, 10);
        if (targetPage && targetPage !== currentPage) {
          fetchProducts(targetPage);
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
        return;
      }

      if (e.target === prevPageBtn && currentPage > 1) {
        fetchProducts(currentPage - 1);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else if (e.target === nextPageBtn) {
        fetchProducts(currentPage + 1);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  // Add to Cart event delegation
  if (productsGrid) {
    productsGrid.addEventListener('click', async (e) => {
      const addBtn = e.target.closest('.add-to-cart-btn');
      if (!addBtn) return;

      const productId = addBtn.dataset.productId;
      if (!productId) return;

      const origText = addBtn.textContent;
      addBtn.disabled = true;
      addBtn.textContent = 'Adding...';

      try {
        const updatedCart = await ApiClient.post('/api/cart/add', {
          productId: parseInt(productId, 10),
          quantity: 1
        });

        DjMartToast.success('Item added to your shopping bag.');
        updateNavbarBadge(updatedCart ? updatedCart.totalItems : 0);
      } catch (err) {
        if (err.status === 401) {
          const contextPath = ApiClient.getContextPath();
          window.location.href = `${contextPath}/auth/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
        } else {
          DjMartToast.error(err.message || 'Could not add product to bag.');
        }
      } finally {
        addBtn.disabled = false;
        addBtn.textContent = origText;
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

  // Handle Browser Back/Forward navigation
  window.addEventListener('popstate', () => {
    initFromUrl();
    fetchProducts(currentPage, false);
  });
});
