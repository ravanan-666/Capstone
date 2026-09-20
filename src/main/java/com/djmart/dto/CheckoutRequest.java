package com.djmart.dto;

import java.io.Serializable;

/**
 * DTO for placing an order during checkout with mock payment confirmation.
 */
public class CheckoutRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shippingAddress;
    private boolean paymentConfirmed;

    public CheckoutRequest() {
    }

    public CheckoutRequest(String shippingAddress, boolean paymentConfirmed) {
        this.shippingAddress = shippingAddress;
        this.paymentConfirmed = paymentConfirmed;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public boolean isPaymentConfirmed() {
        return paymentConfirmed;
    }

    public void setPaymentConfirmed(boolean paymentConfirmed) {
        this.paymentConfirmed = paymentConfirmed;
    }

    @Override
    public String toString() {
        return "CheckoutRequest{" +
                "paymentConfirmed=" + paymentConfirmed +
                '}';
    }
}
