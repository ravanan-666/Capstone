package com.djmart.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO representing the buyer's entire cart including server-calculated totals.
 */
public class CartResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<CartItemResponse> items = new ArrayList<>();
    private int totalItems;
    private BigDecimal grandTotal;

    public CartResponse() {
        this.grandTotal = BigDecimal.ZERO;
    }

    public CartResponse(List<CartItemResponse> items) {
        setItems(items);
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotals();
    }

    public int getTotalItems() {
        return totalItems;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void recalculateTotals() {
        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemResponse item : items) {
            count += (item.getQuantity() != null ? item.getQuantity() : 0);
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
        }
        this.totalItems = count;
        this.grandTotal = total;
    }

    @Override
    public String toString() {
        return "CartResponse{" +
                "itemsCount=" + items.size() +
                ", totalItems=" + totalItems +
                ", grandTotal=" + grandTotal +
                '}';
    }
}
