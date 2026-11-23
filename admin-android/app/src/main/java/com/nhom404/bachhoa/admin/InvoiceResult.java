package com.nhom404.bachhoa.admin;

import java.util.List;

public class InvoiceResult {
    public static class LineItem {
        private final String productName;
        private final int quantity;
        private final long unitPrice;
        private final long lineTotal;

        public LineItem(String productName, int quantity, long unitPrice, long lineTotal) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getUnitPrice() {
            return unitPrice;
        }

        public long getLineTotal() {
            return lineTotal;
        }
    }

    private final String invoiceCode;
    private final String createdDate;
    private final String createdTime;
    private final long subtotalAmount;
    private final long discountAmount;
    private final long totalAmount;
    private final List<LineItem> items;

    public InvoiceResult(String invoiceCode, String createdDate, String createdTime,
                         long subtotalAmount, long discountAmount, long totalAmount,
                         List<LineItem> items) {
        this.invoiceCode = invoiceCode;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.subtotalAmount = subtotalAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public long getSubtotalAmount() {
        return subtotalAmount;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public List<LineItem> getItems() {
        return items;
    }
}