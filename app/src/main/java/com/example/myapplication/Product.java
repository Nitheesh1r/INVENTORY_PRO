package com.example.myapplication;

import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private String sku;
    private String category;
    private int quantity;
    private int minStock;
    private double price;
    private String supplier;
    private long createdAt;

    public Product() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.createdAt = System.currentTimeMillis();
    }

    public Product(String name, String sku, String category, int quantity,
                   int minStock, double price, String supplier) {
        this();
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.quantity = quantity;
        this.minStock = minStock;
        this.price = price;
        this.supplier = supplier;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isLowStock() {
        return quantity <= minStock;
    }

    public double getTotalValue() {
        return quantity * price;
    }
}

