package com.tathanhloc.lokistore.models;

public class OrderStatistics {
    private String date;
    private double revenue;

    public OrderStatistics(String date, double revenue) {
        this.date = date;
        this.revenue = revenue;
    }
    //Getter and Setter

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
