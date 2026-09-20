package com.djmart.dto;

import com.djmart.model.Order;
import com.djmart.model.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO representing a complete order view including line items.
 */
public class OrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long buyerId;
    private String buyerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> items = new ArrayList<>();
    private Timestamp createdAt;

    public OrderResponse() {
    }

    public static OrderResponse fromOrder(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyerId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id=" + id +
                ", buyerId=" + buyerId +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", itemsCount=" + items.size() +
                '}';
    }
}
