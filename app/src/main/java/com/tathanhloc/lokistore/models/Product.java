package com.tathanhloc.lokistore.models;

import androidx.annotation.NonNull;

public class Product {
    private int id;
    private int categoryId;
    private String productCode;
    private String productName;
    private double price;
    private String description;
    private String imagePath;
    private int isActive = 1;


    public Product() {
    }



    public Product(int id, int categoryId, String productCode, String productName, double price, String description, String imagePath, int isActive) {
        this.id = id;
        this.categoryId = categoryId;
        this.productCode = productCode;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.imagePath = imagePath;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    @NonNull
    @Override
    public String toString() {
        return this.productName;
    }
}