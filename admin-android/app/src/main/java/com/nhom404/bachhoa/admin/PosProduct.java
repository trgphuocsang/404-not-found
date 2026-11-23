package com.nhom404.bachhoa.admin;

public class PosProduct {
    private final String barcode;
    private final String name;
    private final long price;
    private final String nearestLotCode;
    private final String nearestExpiry;

    public PosProduct(String barcode, String name, long price, String nearestLotCode, String nearestExpiry) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.nearestLotCode = nearestLotCode;
        this.nearestExpiry = nearestExpiry;
    }

    public String getBarcode() { return barcode; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getNearestLotCode() { return nearestLotCode; }
    public String getNearestExpiry() { return nearestExpiry; }
}
