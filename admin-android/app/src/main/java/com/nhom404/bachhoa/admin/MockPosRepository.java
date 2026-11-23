package com.nhom404.bachhoa.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MockPosRepository implements PosRepository {
    private final Map<String, PosProduct> sampleProducts = new HashMap<>();
    private int orderCounter = 1;

    public MockPosRepository() {
        sampleProducts.put("8934567890123", new PosProduct(
                "8934567890123", "Sữa tươi tiệt trùng 1L", 32000L, "LO-01", "2026-12-10"));
        sampleProducts.put("8934567890130", new PosProduct(
                "8934567890130", "Bánh quy bơ hộp 200g", 28000L, "LO-04", "2027-01-15"));
        sampleProducts.put("8934567890147", new PosProduct(
                "8934567890147", "Nước rửa chén 750ml", 25000L, "LO-02", "2027-06-01"));
    }

    @Override
    public void findProductByBarcode(String barcode, PosCallback<PosProduct> callback) {
        if (barcode == null || barcode.trim().isEmpty()) {
            callback.onError("Mã vạch trống");
            return;
        }
        PosProduct found = sampleProducts.get(barcode.trim());
        if (found != null) {
            callback.onSuccess(found);
        } else {
            callback.onError("Không tìm thấy sản phẩm với mã: " + barcode.trim());
        }
    }

    @Override
    public void checkoutCart(List<InvoiceResult.LineItem> items, PosCallback<InvoiceResult> callback) {
        if (items == null || items.isEmpty()) {
            callback.onError("Giỏ hàng đang trống");
            return;
        }

        long subtotal = 0L;
        List<InvoiceResult.LineItem> copiedItems = new ArrayList<>();
        for (InvoiceResult.LineItem item : items) {
            long lineTotal = item.getLineTotal() > 0
                    ? item.getLineTotal()
                    : item.getUnitPrice() * item.getQuantity();
            subtotal += lineTotal;
            copiedItems.add(new InvoiceResult.LineItem(
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    lineTotal
            ));
        }

        long discount = 0L;
        long total = Math.max(0L, subtotal - discount);

        String code = String.format(Locale.getDefault(), "HD%03d", orderCounter++);
        Date nowDate = new Date();
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(nowDate);
        String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(nowDate);

        callback.onSuccess(new InvoiceResult(
                code,
                dateStr,
                timeStr,
                subtotal,
                discount,
                total,
                copiedItems
        ));
    }
}