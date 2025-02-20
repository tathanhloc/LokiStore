package com.tathanhloc.lokistore.models;

public class Order {
    private int orderId;
    private String orderCode;
    private int userId;
    private String orderDate;
    private String deliveryDate;
    private double totalAmount;
    private String note;
    private String status;

    public Order() {
        this.status = "PENDING";
    }

    public Order(int orderId, String orderCode, int userId, String orderDate,
                 String deliveryDate, double totalAmount, String note, String status) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.userId = userId;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.totalAmount = totalAmount;
        this.note = note;
        this.status = status != null ? status : "PENDING";
    }
    public Order(int orderId, String orderCode, int userId, String orderDate,
                 String deliveryDate, double totalAmount, String note) {
        this(orderId, orderCode, userId, orderDate, deliveryDate, totalAmount, note, "PENDING");
    }


    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    // Thêm getter và setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
