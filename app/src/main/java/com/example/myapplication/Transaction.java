package com.example.myapplication;

import java.io.Serializable;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;


public class Transaction implements Serializable {
    private String id;
    private String productId;
    private String productName;
    private String type; // "in" or "out"
    private int quantity;
    private String notes;
    private long timestamp;

    public Transaction() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
    }

    public Transaction(String productId, String productName, String type,
                       int quantity, String notes) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isStockIn() {
        return "in".equals(type);
    }
}
