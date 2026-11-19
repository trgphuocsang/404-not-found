package com.nhom404.bachhoa.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PosActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartList;

    private Button btnCart, btnCheckout;
    private CardView cardSummary;
    private TextView tvTotalPrice;

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

    private void calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        btnCart.setText("Giỏ hàng (" + cartList.size() + ")");
    }
}