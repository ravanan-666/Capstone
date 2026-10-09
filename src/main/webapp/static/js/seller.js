/**
 * Seller Studio Client Engine
 * Manages modal transitions, product editing form pre-fill, and order status updates.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Add Product Modal
    const addModal = document.getElementById('addProductModal');
    const openAddBtn = document.getElementById('openAddProductModalBtn');
    const closeAddBtn = document.getElementById('closeAddModalBtn');
    const cancelAddBtn = document.getElementById('cancelAddModalBtn');

    if (openAddBtn && addModal) {
        openAddBtn.addEventListener('click', () => {
            addModal.classList.add('active');
        });
    }

    [closeAddBtn, cancelAddBtn].forEach(btn => {
        if (btn && addModal) {
            btn.addEventListener('click', () => {
                addModal.classList.remove('active');
            });
        }
    });

    // Edit Product Modal
    const editModal = document.getElementById('editProductModal');
    const closeEditBtn = document.getElementById('closeEditModalBtn');
    const cancelEditBtn = document.getElementById('cancelEditModalBtn');

    document.querySelectorAll('.edit-product-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!editModal) return;

            document.getElementById('editProductId').value = btn.dataset.id || '';
            document.getElementById('editProductName').value = btn.dataset.name || '';
            document.getElementById('editProductCategory').value = btn.dataset.category || '';
            document.getElementById('editProductPrice').value = btn.dataset.price || '';
            document.getElementById('editProductStock').value = btn.dataset.stock || '';
            document.getElementById('editProductDescription').value = btn.dataset.description || '';
            document.getElementById('editProductImage').value = btn.dataset.image || '';

            editModal.classList.add('active');
        });
    });

    [closeEditBtn, cancelEditBtn].forEach(btn => {
        if (btn && editModal) {
            btn.addEventListener('click', () => {
                editModal.classList.remove('active');
            });
        }
    });

    // Close modals on backdrop click
    [addModal, editModal].forEach(modal => {
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('active');
                }
            });
        }
    });

    // Close on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (addModal) addModal.classList.remove('active');
            if (editModal) editModal.classList.remove('active');
        }
    });

    // AJAX Order Status Updates
    document.querySelectorAll('.order-status-update-form').forEach(form => {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const actionUrl = form.getAttribute('action');
            const select = form.querySelector('select[name="status"]');
            const newStatus = select ? select.value : null;

            if (!actionUrl || !newStatus) return;

            apiClient.post(actionUrl, { status: newStatus })
                .then(res => {
                    if (res.success) {
                        DjMartToast.success('Order status updated successfully');
                        setTimeout(() => window.location.reload(), 800);
                    } else {
                        DjMartToast.error(res.message || 'Status transition failed');
                    }
                })
                .catch(err => {
                    DjMartToast.error(err.message || 'Status update failed');
                });
        });
    });
});
