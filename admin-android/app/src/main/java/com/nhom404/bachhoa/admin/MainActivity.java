package com.nhom404.bachhoa.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonObject;
import com.nhom404.bachhoa.admin.network.ApiClient;
import com.nhom404.bachhoa.admin.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ProgressBar progressSession;
    private TextView tvSessionStatus;
    private Button btnRetrySession;
    private LinearLayout menuContainer;

    private Call<JsonObject> sessionCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.hasSession()) {
            openLogin();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );

                    return insets;
                }
        );

        progressSession = findViewById(R.id.progressSession);
        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        btnRetrySession = findViewById(R.id.btnRetrySession);
        menuContainer = findViewById(R.id.menuContainer);

        btnRetrySession.setOnClickListener(view -> checkSession());

        findViewById(R.id.btnLogout).setOnClickListener(view -> {
            sessionManager.clearSession();
            openLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFinishing()) {
            return;
        }

        if (!sessionManager.hasSession()) {
            openLogin();
            return;
        }

        checkSession();
    }

    private void checkSession() {
        if (sessionCall != null) {
            sessionCall.cancel();
        }

        menuContainer.setVisibility(View.GONE);
        progressSession.setVisibility(View.VISIBLE);
        btnRetrySession.setVisibility(View.GONE);
        tvSessionStatus.setText(R.string.session_checking);

        // Ghi nhớ token để bỏ qua phản hồi thuộc phiên cũ.
        String requestToken = sessionManager.getAccessToken();

        sessionCall = ApiClient.getApiService(this).getCurrentUser();

        sessionCall.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(
                    Call<JsonObject> call,
                    Response<JsonObject> response
            ) {
                if (shouldIgnore(call, requestToken)) {
                    return;
                }

                progressSession.setVisibility(View.GONE);

                if (response.code() == 401) {
                    // AuthInterceptor đảm nhiệm xóa phiên và điều hướng.
                    return;
                }

                if (!response.isSuccessful()) {
                    tvSessionStatus.setText(getString(
                            R.string.session_http_error,
                            response.code()
                    ));
                    btnRetrySession.setVisibility(View.VISIBLE);
                    return;
                }

                JsonObject body = response.body();

                // /auth/toi trả vai_tro và trang_thai dạng snake_case.
                String role = readString(body, "vai_tro");
                String status = readString(body, "trang_thai");

                if (!isAdminRole(role) || !"HOAT_DONG".equals(status)) {
                    sessionManager.clearSession();

                    Toast.makeText(
                            MainActivity.this,
                            R.string.login_admin_only,
                            Toast.LENGTH_LONG
                    ).show();

                    openLogin();
                    return;
                }

                // Cập nhật vai trò mới nhất lấy từ server.
                sessionManager.saveSession(requestToken, role);

                setupRoleMenu(role);
                tvSessionStatus.setText(R.string.session_valid);
                menuContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(
                    Call<JsonObject> call,
                    Throwable throwable
            ) {
                if (shouldIgnore(call, requestToken)) {
                    return;
                }

                progressSession.setVisibility(View.GONE);
                tvSessionStatus.setText(R.string.session_network_error);
                btnRetrySession.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean shouldIgnore(
            Call<JsonObject> call,
            String requestToken
    ) {
        return call.isCanceled()
                || isFinishing()
                || isDestroyed()
                || requestToken == null
                || !requestToken.equals(sessionManager.getAccessToken());
    }

    private String readString(JsonObject body, String key) {
        if (body == null
                || !body.has(key)
                || !body.get(key).isJsonPrimitive()) {
            return null;
        }

        return body.get(key).getAsString();
    }

    private boolean isAdminRole(String role) {
        return "CHU_CUA_HANG".equals(role)
                || "NHAN_VIEN_BAN_HANG".equals(role)
                || "NHAN_VIEN_GIAO_HANG".equals(role);
    }

    private void setupRoleMenu(String role) {
        menuContainer.removeAllViews();

        int roleLabel;

        // Chính sách menu đề xuất, cần thống nhất với nhóm.
        if ("CHU_CUA_HANG".equals(role)) {
            roleLabel = R.string.home_owner;

            addPendingFeature(R.string.home_categories);
            addPendingFeature(R.string.home_products);
            addPendingFeature(R.string.home_suppliers);
            addPendingFeature(R.string.home_batches);
            addPendingFeature(R.string.home_stocktaking);
            addPendingFeature(R.string.home_alerts);
            addPendingFeature(R.string.home_sales);
            addPendingFeature(R.string.home_orders);
            addPendingFeature(R.string.home_reports);
            addPendingFeature(R.string.home_promotions);

        } else if ("NHAN_VIEN_BAN_HANG".equals(role)) {
            roleLabel = R.string.home_sales_staff;

            addPendingFeature(R.string.home_sales);
            addPendingFeature(R.string.home_orders);

        } else {
            roleLabel = R.string.home_delivery_staff;

            addPendingFeature(R.string.home_orders);
        }

        TextView tvRole = findViewById(R.id.tvRole);
        tvRole.setText(getString(
                R.string.home_role,
                getString(roleLabel)
        ));
    }

    private void addPendingFeature(@StringRes int titleRes) {
        Button button = new Button(this);
        button.setText(titleRes);

        button.setOnClickListener(view ->
                Toast.makeText(
                        MainActivity.this,
                        R.string.feature_pending,
                        Toast.LENGTH_SHORT
                ).show()
        );

        menuContainer.addView(button);
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (sessionCall != null) {
            sessionCall.cancel();
        }

        super.onDestroy();
    }
}