package com.nhom404.bachhoa.admin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity implements OrdersAdapter.OnOrderActionListener {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<DeliveryOrder> orderItems = new ArrayList<>();
    private final String[] shipperCandidates = {
            "NV01 - Minh Khoa",
            "NV02 - Thanh Tùng",
            "NV03 - Hoàng Nam"
    };

    private LinearLayout layoutLoading;
    private LinearLayout layoutErrorOrEmpty;
    private TextView tvOrdersStatusMessage;
    private RecyclerView rvOrders;
    private OrdersAdapter ordersAdapter;

    private Runnable pendingLoadRunnable;
    private boolean simulateNetworkErrorOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.ordersRoot),
                (view, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                }
        );

        layoutLoading = findViewById(R.id.layoutLoading);
        layoutErrorOrEmpty = findViewById(R.id.layoutErrorOrEmpty);
        tvOrdersStatusMessage = findViewById(R.id.tvOrdersStatusMessage);
        rvOrders = findViewById(R.id.rvOrders);

        Button btnRefresh = findViewById(R.id.btnRefreshOrders);
        Button btnRetry = findViewById(R.id.btnRetryOrders);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrdersAdapter(this);
        rvOrders.setAdapter(ordersAdapter);

        btnRefresh.setOnClickListener(v -> loadOrders());
        btnRetry.setOnClickListener(v -> loadOrders());

        seedSampleOrders();
        loadOrders();
    }

    private void seedSampleOrders() {
        orderItems.clear();
        orderItems.add(new DeliveryOrder(
                "DH001",
                "Nguyễn Minh Anh",
                "0908111222",
                "12 Quang Trung, Gò Vấp, TP.HCM",
                "2x Mì Hảo Hảo, 3x Nước ngọt Coca Cola",
                39000,
                DeliveryOrder.STATUS_PENDING,
                ""
        ));
        orderItems.add(new DeliveryOrder(
                "DH002",
                "Lê Hoàng Phúc",
                "0919333444",
                "45 Phan Văn Trị, Gò Vấp, TP.HCM",
                "1x Bánh snack Oishi, 4x Sữa tươi",
                94000,
                DeliveryOrder.STATUS_CONFIRMED,
                "NV01 - Minh Khoa"
        ));
        orderItems.add(new DeliveryOrder(
                "DH003",
                "Trần Thu Hà",
                "0937555666",
                "88 Nguyễn Oanh, Gò Vấp, TP.HCM",
                "5x Gạo thơm 2kg, 1x Dầu ăn 1L",
                215000,
                DeliveryOrder.STATUS_DELIVERING,
                "NV02 - Thanh Tùng"
        ));
    }

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutErrorOrEmpty.setVisibility(View.GONE);
        rvOrders.setVisibility(View.GONE);
    }

    private void showSuccessState() {
        layoutLoading.setVisibility(View.GONE);
        if (orderItems.isEmpty()) {
            layoutErrorOrEmpty.setVisibility(View.VISIBLE);
            tvOrdersStatusMessage.setText("Hiện chưa có đơn giao hàng nào");
            rvOrders.setVisibility(View.GONE);
        } else {
            layoutErrorOrEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
            ordersAdapter.setOrders(orderItems);
        }
    }

    private void showErrorState(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutErrorOrEmpty.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        tvOrdersStatusMessage.setText(message);
    }

    private void loadOrders() {
        if (pendingLoadRunnable != null) {
            mainHandler.removeCallbacks(pendingLoadRunnable);
        }
        showLoadingState();

        pendingLoadRunnable = () -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (simulateNetworkErrorOnce) {
                simulateNetworkErrorOnce = false;
                showErrorState("Lỗi kết nối mạng khi tải danh sách đơn giao. Vui lòng thử lại.");
                return;
            }
            showSuccessState();
        };

        mainHandler.postDelayed(pendingLoadRunnable, 400L);
    }

    @Override
    public void onViewDetail(DeliveryOrder order) {
        String formattedMoney = String.format(Locale.getDefault(), "%,d đ", order.getTotalAmount())
                .replace(',', '.');
        String shipper = (order.getAssignedShipper() == null || order.getAssignedShipper().trim().isEmpty())
                ? "Chưa phân công"
                : order.getAssignedShipper();

        String detailText = "Mã đơn: " + order.getOrderCode()
                + "\nKhách hàng: " + order.getCustomerName()
                + "\nĐiện thoại: " + order.getCustomerPhone()
                + "\nĐịa chỉ giao: " + order.getAddress()
                + "\nSản phẩm: " + order.getItemsSummary()
                + "\nTổng tiền: " + formattedMoney
                + "\nNhân viên giao: " + shipper
                + "\nTrạng thái hiện tại: " + order.getStatus();

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết đơn #" + order.getOrderCode())
                .setMessage(detailText)
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onAssignShipper(DeliveryOrder order, int position) {
        int checkedIndex = 0;
        if (order.getAssignedShipper() != null) {
            for (int i = 0; i < shipperCandidates.length; i++) {
                if (shipperCandidates[i].equals(order.getAssignedShipper())) {
                    checkedIndex = i;
                    break;
                }
            }
        }

        final int[] selectedIndex = {checkedIndex};
        new AlertDialog.Builder(this)
                .setTitle("Gán nhân viên giao • #" + order.getOrderCode())
                .setSingleChoiceItems(shipperCandidates, checkedIndex, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Xác nhận gán", (dialog, which) -> {
                    String chosenShipper = shipperCandidates[selectedIndex[0]];
                    order.setAssignedShipper(chosenShipper);
                    if (position >= 0 && position < orderItems.size()) {
                        ordersAdapter.notifyItemChanged(position);
                    } else {
                        ordersAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(this, "Đã gán " + chosenShipper + " cho #" + order.getOrderCode(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onAdvanceStatus(DeliveryOrder order, int position) {
        String nextStatus = order.getNextWorkflowStatus();
        if (nextStatus == null) {
            Toast.makeText(this, "Đơn hàng đã ở trạng thái cuối", Toast.LENGTH_SHORT).show();
            return;
        }

        if (DeliveryOrder.STATUS_PENDING.equals(order.getStatus())
                && (order.getAssignedShipper() == null || order.getAssignedShipper().trim().isEmpty())) {
            order.setAssignedShipper(shipperCandidates[0]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận cập nhật trạng thái")
                .setMessage("Chuyển đơn #" + order.getOrderCode()
                        + " từ [" + order.getStatus() + "] sang [" + nextStatus + "]?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    order.setStatus(nextStatus);
                    if (position >= 0 && position < orderItems.size()) {
                        ordersAdapter.notifyItemChanged(position);
                    } else {
                        ordersAdapter.notifyDataSetChanged();
                    }
                    Snackbar.make(
                            rvOrders,
                            "Đơn #" + order.getOrderCode() + " đã chuyển sang " + nextStatus,
                            Snackbar.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (pendingLoadRunnable != null) {
            mainHandler.removeCallbacks(pendingLoadRunnable);
        }
        super.onDestroy();
    }
}