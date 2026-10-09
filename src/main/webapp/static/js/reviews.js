/**
 * Product Reviews & Rating Client Engine
 * Handles star selector micro-interactions, review pagination fetching,
 * and AJAX review submission.
 */
document.addEventListener('DOMContentLoaded', () => {
    const reviewsList = document.getElementById('reviewsList');
    if (!reviewsList) return;

    const productId = reviewsList.dataset.productId;
    const starContainer = document.getElementById('starRatingSelect');
    const ratingInput = document.getElementById('selectedRating');
    const reviewForm = document.getElementById('submitReviewForm');
    const submitBtn = document.getElementById('submitReviewBtn');

    // 1. Star Rating Selector Logic
    if (starContainer && ratingInput) {
        const stars = starContainer.querySelectorAll('.star-icon');

        stars.forEach(star => {
            star.addEventListener('click', () => {
                const rating = parseInt(star.dataset.rating, 10);
                ratingInput.value = rating;
                updateStarDisplay(rating);
            });

            star.addEventListener('mouseenter', () => {
                const rating = parseInt(star.dataset.rating, 10);
                updateStarDisplay(rating);
            });
        });

        starContainer.addEventListener('mouseleave', () => {
            const currentRating = parseInt(ratingInput.value, 10) || 5;
            updateStarDisplay(currentRating);
        });

        function updateStarDisplay(rating) {
            stars.forEach(s => {
                const r = parseInt(s.dataset.rating, 10);
                if (r <= rating) {
                    s.classList.add('active');
                } else {
                    s.classList.remove('active');
                }
            });
        }
    }

    // 2. Fetch and Render Reviews
    function loadReviews() {
        if (!productId) return;

        apiClient.get(`/api/reviews/product/${productId}?page=1&size=20`)
            .then(res => {
                if (res.success && res.data) {
                    renderReviews(res.data.content || res.data.data || []);
                } else {
                    reviewsList.innerHTML = '<p style="color: var(--color-text-muted); text-align: center;">No reviews available.</p>';
                }
            })
            .catch(err => {
                console.error('Failed to load reviews:', err);
                reviewsList.innerHTML = '<p style="color: var(--color-text-muted); text-align: center;">Unable to load reviews at this time.</p>';
            });
    }

    function renderReviews(reviews) {
        if (!reviews || reviews.length === 0) {
            reviewsList.innerHTML = `
                <div class="checkout-section-card" style="text-align: center; padding: 2rem; color: var(--color-text-muted);">
                    <p style="margin: 0;">Be the first client to submit a verified assessment for this article.</p>
                </div>
            `;
            return;
        }

        const html = reviews.map(rev => {
            const stars = '★'.repeat(rev.rating) + '☆'.repeat(5 - rev.rating);
            const dateStr = rev.createdAt ? new Date(rev.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }) : '';
            const author = rev.userName || 'Verified Buyer';
            const comment = rev.comment ? escapeHtml(rev.comment) : '<em>No written commentary provided.</em>';

            return `
                <div class="review-card">
                    <div class="review-header">
                        <div>
                            <span class="review-author">${escapeHtml(author)}</span>
                            <span style="color: var(--color-success); font-size: 0.75rem; margin-left: 0.5rem;">✓ Verified Purchase</span>
                        </div>
                        <span class="review-date">${dateStr}</span>
                    </div>
                    <div style="color: #f59e0b; font-size: 1rem; margin-bottom: 0.5rem;">
                        ${stars}
                    </div>
                    <div class="review-comment">
                        ${comment}
                    </div>
                </div>
            `;
        }).join('');

        reviewsList.innerHTML = html;
    }

    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // 3. Submit Review via AJAX
    if (reviewForm) {
        reviewForm.addEventListener('submit', (e) => {
            e.preventDefault();

            const rating = parseInt(ratingInput?.value, 10) || 5;
            const comment = (document.getElementById('reviewComment')?.value || '').trim();

            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Submitting...';
            }

            apiClient.post('/api/reviews', {
                productId: parseInt(productId, 10),
                rating: rating,
                comment: comment
            })
            .then(res => {
                if (res.success) {
                    DjMartToast.success('Thank you! Your assessment has been recorded.');
                    reviewForm.reset();
                    if (ratingInput) ratingInput.value = '5';
                    if (starContainer) {
                        starContainer.querySelectorAll('.star-icon').forEach(s => s.classList.add('active'));
                    }
                    loadReviews();
                } else {
                    DjMartToast.error(res.message || 'Unable to submit review');
                }
            })
            .catch(err => {
                DjMartToast.error(err.message || 'Review submission failed');
            })
            .finally(() => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = 'Submit Assessment';
                }
            });
        });
    }

    // Initial load
    loadReviews();
});
