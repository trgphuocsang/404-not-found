    package com.nhom404.bachhoa.admin;

    import java.util.List;

    public interface PosRepository {
        void findProductByBarcode(String barcode, PosCallback<PosProduct> callback);

        void checkoutCart(List<InvoiceResult.LineItem> items, PosCallback<InvoiceResult> callback);
    }