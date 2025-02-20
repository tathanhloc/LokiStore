package com.tathanhloc.lokistore.models;

public class CategoryStatistics {
    private String categoryName;
    private double revenue;
    private int quantity;

    private double percentage;


    public CategoryStatistics(String categoryName, double revenue) {
        this.categoryName = categoryName;
        this.revenue = revenue;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
