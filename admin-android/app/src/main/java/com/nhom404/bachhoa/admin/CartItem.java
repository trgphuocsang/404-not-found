package com.nhom404.bachhoa.admin;

public class CartItem {
    private String id;
    private String name;
    private double price;
    private int quantity;

    // Hàm khởi tạo (Constructor)
    public CartItem(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Các hàm lấy dữ liệu (Getters)
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // Các hàm gán dữ liệu (Setters)
    public void setQuantity(int quantity) { this.quantity = quantity; }
}