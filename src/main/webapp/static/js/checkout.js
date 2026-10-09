/**
 * Checkout Client Engine
 * Orchestrates shipping address validation, payment selector switching,
 * double-submission prevention, and authoritative transaction dispatch.
 */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('checkoutForm');
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    const placeOrderBtnText = document.getElementById('placeOrderBtnText');
    const placeOrderSpinner = document.getElementById('placeOrderSpinner');

    const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
    const cardSection = document.getElementById('cardDetailsSection');
    const upiSection = document.getElementById('upiDetailsSection');
    const codSection = document.getElementById('codDetailsSection');

    // Handle Payment Method Tab Selection
    paymentRadios.forEach(radio => {
        radio.addEventListener('change', () => {
            // Update selected class on parent label cards
            document.querySelectorAll('.payment-method-card').forEach(card => card.classList.remove('selected'));
            radio.closest('.payment-method-card').classList.add('selected');

            // Toggle corresponding detail box
            if (radio.value === 'CARD') {
                if (cardSection) cardSection.style.display = 'block';
                if (upiSection) upiSection.style.display = 'none';
                if (codSection) codSection.style.display = 'none';
            } else if (radio.value === 'UPI') {
                if (cardSection) cardSection.style.display = 'none';
                if (upiSection) upiSection.style.display = 'block';
                if (codSection) codSection.style.display = 'none';
            } else if (radio.value === 'COD') {
                if (cardSection) cardSection.style.display = 'none';
                if (upiSection) upiSection.style.display = 'none';
                if (codSection) codSection.style.display = 'block';
            }
        });
    });

    if (form) {
        form.addEventListener('submit', (e) => {
            const street = (document.getElementById('streetAddress')?.value || '').trim();
            const city = (document.getElementById('city')?.value || '').trim();
            const state = (document.getElementById('state')?.value || '').trim();
            const postalCode = (document.getElementById('postalCode')?.value || '').trim();

            if (!street || street.length < 5) {
                e.preventDefault();
                DjMartToast.error('Please specify a valid street address (minimum 5 characters)');
                document.getElementById('streetAddress')?.focus();
                return;
            }

            if (!city) {
                e.preventDefault();
                DjMartToast.error('Please specify your delivery city');
                document.getElementById('city')?.focus();
                return;
            }

            if (!state) {
                e.preventDefault();
                DjMartToast.error('Please specify your delivery state');
                document.getElementById('state')?.focus();
                return;
            }

            if (!postalCode) {
                e.preventDefault();
                DjMartToast.error('Please enter a valid postal PIN code');
                document.getElementById('postalCode')?.focus();
                return;
            }

            // Concatenate full address
            const fullAddress = `${street}, ${city}, ${state} - ${postalCode}`;
            const hiddenAddress = document.getElementById('fullShippingAddress');
            if (hiddenAddress) {
                hiddenAddress.value = fullAddress;
            }

            // Set loading state
            if (placeOrderBtn) {
                placeOrderBtn.disabled = true;
                if (placeOrderBtnText) placeOrderBtnText.style.display = 'none';
                if (placeOrderSpinner) placeOrderSpinner.style.display = 'inline';
            }
        });
    }
});
