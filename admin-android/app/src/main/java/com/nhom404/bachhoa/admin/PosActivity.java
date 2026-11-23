package com.nhom404.bachhoa.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PosActivity extends AppCompatActivity {
    private final PosRepository posRepository = new MockPosRepository();
    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartList;

    private Button btnCart, btnCheckout, btnScan;
    private EditText edtSearch;
    private CardView cardSummary;
    private TextView tvTotalPrice;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents().trim();
                    if (edtSearch != null) {
                        edtSearch.setText(scannedCode);
                        edtSearch.setSelection(scannedCode.length());
                    }
                    handleScannedBarcode(scannedCode);
                } else {
                    Toast.makeText(this, "Đã hủy quét mã", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pos);

        btnCart = findViewById(R.id.btnCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        cardSummary = findViewById(R.id.cardSummary);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvCartItems = findViewById(R.id.rvCartItems);

        int scanId = getResources().getIdentifier("btnScan", "id", getPackageName());
        if (scanId == 0) {
            scanId = getResources().getIdentifier("btnScanBarcode", "id", getPackageName());
        }
        if (scanId != 0) {
            btnScan = findViewById(scanId);
        }

        int searchId = getResources().getIdentifier("edtSearch", "id", getPackageName());
        if (searchId != 0) {
            edtSearch = findViewById(searchId);
        }

        ViewCompat.setOnApplyWindowInsetsListener(cardSummary, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    v.getPaddingBottom() + systemBars.bottom
            );
            return insets;
        });

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();

        // Dữ liệu mẫu kiểm tra giỏ hàng
        cartList.add(new CartItem("SP01", "Mì Hảo Hảo chua cay", 4500, 2));
        cartList.add(new CartItem("SP02", "Nước ngọt Coca Cola", 10000, 3));
        cartList.add(new CartItem("SP03", "Bánh snack Oishi", 6000, 1));

        cartAdapter = new CartAdapter(cartList, this::calculateTotalPrice);
        rvCartItems.setAdapter(cartAdapter);

        calculateTotalPrice();

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> startBarcodeScan());
        } else {
            Toast.makeText(this, "Chưa tìm thấy nút btnScan trong activity_pos.xml", Toast.LENGTH_SHORT).show();
        }

        btnCart.setOnClickListener(v -> {
            if (cardSummary.getVisibility() == View.GONE) {
                cardSummary.setVisibility(View.VISIBLE);
            } else {
                cardSummary.setVisibility(View.GONE);
            }
        });

        btnCheckout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalAmount = 0;
            for (CartItem item : cartList) {
                totalAmount += item.getPrice() * item.getQuantity();
            }

            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thanh toán")
                    .setMessage("Tổng tiền: " + String.format(Locale.getDefault(), "%,.0f đ", totalAmount) + "\n\nTiến hành hoàn tất đơn hàng?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        List<InvoiceResult.LineItem> invoiceItems = new ArrayList<>();
                        for (CartItem item : cartList) {
                            long unitPrice = (long) item.getPrice();
                            int qty = item.getQuantity();
                            long lineTotal = unitPrice * qty;
                            invoiceItems.add(new InvoiceResult.LineItem(
                                    item.getName(),
                                    qty,
                                    unitPrice,
                                    lineTotal
                            ));
                        }

                        posRepository.checkoutCart(invoiceItems, new PosCallback<InvoiceResult>() {
                            @Override
                            public void onSuccess(InvoiceResult invoice) {
                                Toast.makeText(PosActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                showInvoiceDialog(invoice, () -> {
                                    cartList.clear();
                                    cartAdapter.notifyDataSetChanged();
                                    calculateTotalPrice();
                                    cardSummary.setVisibility(View.GONE);
                                });
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(PosActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void startBarcodeScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Đưa mã vạch sản phẩm vào khung hình");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        barcodeLauncher.launch(options);
    }

    private void handleScannedBarcode(String code) {
        for (int i = 0; i < cartList.size(); i++) {
            CartItem item = cartList.get(i);
            if (code.equalsIgnoreCase(item.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                cartAdapter.notifyItemChanged(i);
                calculateTotalPrice();
                cardSummary.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Đã tăng số lượng: " + item.getName(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // thêm tạm để test luồng
        cartList.add(new CartItem(code, "SP mã " + code, 10000, 1));
        cartAdapter.notifyItemInserted(cartList.size() - 1);
        calculateTotalPrice();
        cardSummary.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Đã quét mã: " + code, Toast.LENGTH_SHORT).show();
    }

    private void calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        btnCart.setText("Giỏ hàng (" + cartList.size() + ")");
    }

    private String formatMoneyD(long amount) {
        if (amount == 0L) {
            return "0d";
        }
        return String.format(Locale.getDefault(), "%,dd", amount).replace(',', '.') + "d";
    }

    private String buildInvoiceText(InvoiceResult invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================\n");
        sb.append("          CUA HANG BACH HOA\n");
        sb.append("          HOA DON BAN HANG\n");
        sb.append("            DA THANH TOAN\n");
        sb.append("====================================\n");
        sb.append("Ma hoa don      : ").append(invoice.getInvoiceCode()).append("\n");
        sb.append("Ngay lap        : ").append(invoice.getCreatedDate()).append("\n");
        sb.append("Gio thanh toan  : ").append(invoice.getCreatedTime()).append("\n");
        sb.append("------------------------------------\n");
        sb.append("CHI TIET DON HANG\n");
        sb.append(String.format(Locale.US, "%-20s %2s %10s\n", "San pham", "SL", "Thanh tien"));
        sb.append("------------------------------------\n");

        if (invoice.getItems() != null) {
            for (InvoiceResult.LineItem item : invoice.getItems()) {
                String name = item.getProductName();
                if (name == null) {
                    name = "";
                }
                if (name.length() > 20) {
                    name = name.substring(0, 19) + ".";
                }
                sb.append(String.format(Locale.US, "%-20s %2d %10s\n",
                        name,
                        item.getQuantity(),
                        formatMoneyD(item.getLineTotal())));
            }
        }

        sb.append("------------------------------------\n");
        sb.append(String.format(Locale.US, "%-16s: %17s\n",
                "Tam tinh", formatMoneyD(invoice.getSubtotalAmount())));
        sb.append(String.format(Locale.US, "%-16s: %17s\n",
                "Giam gia", formatMoneyD(invoice.getDiscountAmount())));
        sb.append(String.format(Locale.US, "%-16s: %17s\n",
                "TONG THANH TOAN", formatMoneyD(invoice.getTotalAmount())));
        sb.append("====================================\n");
        sb.append("         CAM ON QUY KHACH!\n");
        sb.append("    Hen gap lai quy khach lan sau.\n");
        sb.append("====================================\n");
        return sb.toString();
    }

    private java.io.File exportInvoiceToTxt(InvoiceResult invoice) {
        java.io.File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && !dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        String fileName = "HoaDon_" + invoice.getInvoiceCode() + ".txt";
        java.io.File file = new java.io.File(dir, fileName);
        String content = buildInvoiceText(invoice);

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            fos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            fos.flush();
            Toast.makeText(this, "Đã xuất hóa đơn: " + file.getName(), Toast.LENGTH_LONG).show();
            return file;
        } catch (java.io.IOException e) {
            Toast.makeText(this, "Xuất hóa đơn thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void shareInvoiceText(InvoiceResult invoice) {
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hóa đơn " + invoice.getInvoiceCode());
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, buildInvoiceText(invoice));
        startActivity(android.content.Intent.createChooser(shareIntent, "Xuất / Chia sẻ hóa đơn"));
    }

    private void showInvoiceDialog(InvoiceResult invoice, Runnable onCloseResetCart) {
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.TextView tvInvoice = new android.widget.TextView(this);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        tvInvoice.setPadding(pad, pad, pad, pad);
        tvInvoice.setTextIsSelectable(true);
        tvInvoice.setTypeface(android.graphics.Typeface.MONOSPACE);
        tvInvoice.setText(buildInvoiceText(invoice));
        scrollView.addView(tvInvoice);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Hóa đơn " + invoice.getInvoiceCode())
                .setView(scrollView)
                .setCancelable(false)
                .setPositiveButton("Xuất file (.txt)", null)
                .setNeutralButton("Chia sẻ", null)
                .setNegativeButton("Đóng", (d, which) -> {
                    if (onCloseResetCart != null) {
                        onCloseResetCart.run();
                    }
                    d.dismiss();
                })
                .create();

        dialog.setOnShowListener(d -> {
            Button btnExport = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btnExport != null) {
                btnExport.setOnClickListener(v -> exportInvoiceToTxt(invoice));
            }

            Button btnShare = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (btnShare != null) {
                btnShare.setOnClickListener(v -> shareInvoiceText(invoice));
            }
        });

        dialog.show();
    }
}