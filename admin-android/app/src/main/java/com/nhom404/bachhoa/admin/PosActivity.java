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
                        Toast.makeText(this, "Thanh toán thành công! Đã trừ tồn kho theo lô FEFO.", Toast.LENGTH_LONG).show();

                        cartList.clear();
                        cartAdapter.notifyDataSetChanged();
                        calculateTotalPrice();
                        cardSummary.setVisibility(View.GONE);
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
}