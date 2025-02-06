package com.tathanhloc.lokistore.models;

public class Category {
    private int id;
    private String categoryCode;
    private String categoryName;

    public Category() {
    }

    public Category(int id, String categoryCode, String categoryName) {
        this.id = id;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        // Được sử dụng cho Spinner hiển thị danh mục
        return categoryName;
    }
}