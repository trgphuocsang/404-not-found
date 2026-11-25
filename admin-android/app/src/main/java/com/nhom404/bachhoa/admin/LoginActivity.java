package com.nhom404.bachhoa.admin;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.nhom404.bachhoa.admin.model.LoginRequest;
import com.nhom404.bachhoa.admin.model.LoginResponse;
import com.nhom404.bachhoa.admin.network.ApiClient;
import com.nhom404.bachhoa.admin.session.SessionManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtAccount;
    private EditText edtPassword;
    private Call<LoginResponse> loginCall;
    private TextView tvLoginError;
    private ProgressBar progressLogin;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.hasSession()) {
            openMain();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        applyWindowInsets();
        bindViews();
        setupListeners();
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.loginRoot);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets keyboard = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            view.setPadding(
                    bars.left,
                    bars.top,
                    bars.right,
                    Math.max(bars.bottom, keyboard.bottom)
            );

            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void bindViews() {
        edtAccount = findViewById(R.id.edtAccount);
        edtPassword = findViewById(R.id.edtPassword);
        tvLoginError = findViewById(R.id.tvLoginError);
        progressLogin = findViewById(R.id.progressLogin);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(view -> attemptLogin());

        edtPassword.setOnEditorActionListener(
                (textView, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        attemptLogin();
                        return true;
                    }

                    return false;
                }
        );

        findViewById(R.id.btnOpenConnectionCheck)
                .setVisibility(View.VISIBLE);
    }

    private void attemptLogin() {
        // Ngăn gửi thêm khi đang có yêu cầu đăng nhập.
        if (!btnLogin.isEnabled()) {
            return;
        }

        tvLoginError.setVisibility(View.GONE);
        edtAccount.setError(null);
        edtPassword.setError(null);

        String account = edtAccount.getText().toString().trim();

        // Giữ nguyên mật khẩu, không tự ý bỏ khoảng trắng.
        String password = edtPassword.getText().toString();

        if (!validateInput(account, password)) {
            return;
        }

        performLogin(account, password);
    }

    private boolean validateInput(String account, String password) {
        if (account.isEmpty()) {
            edtAccount.setError(
                    getString(R.string.login_account_required)
            );
            edtAccount.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError(
                    getString(R.string.login_password_required)
            );
            edtPassword.requestFocus();
            return false;
        }

        if (!account.matches("^[0-9]{10,11}$")) {
            edtAccount.setError(
                    getString(R.string.login_phone_invalid)
            );
            edtAccount.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean loading) {
        progressLogin.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnLogin.setEnabled(!loading);
        edtAccount.setEnabled(!loading);
        edtPassword.setEnabled(!loading);
    }

    private void showError(String message) {
        tvLoginError.setText(message);
        tvLoginError.setVisibility(View.VISIBLE);
    }

    private void performLogin(String phone, String password) {
        setLoading(true);

        LoginRequest request = new LoginRequest(phone, password);

        loginCall = ApiClient.getApiService(this).login(request);

        loginCall.enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response
            ) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                setLoading(false);

                if (!response.isSuccessful()) {
                    showError(readLoginError(response));
                    return;
                }

                LoginResponse body = response.body();

                if (body == null
                        || body.getToken() == null
                        || body.getToken().trim().isEmpty()
                        || body.getNguoiDung() == null) {

                    showError(getString(R.string.login_invalid_response));
                    return;
                }

                String role = body.getNguoiDung().getVaiTro();

                boolean allowed = "CHU_CUA_HANG".equals(role)
                        || "NHAN_VIEN_BAN_HANG".equals(role)
                        || "NHAN_VIEN_GIAO_HANG".equals(role);

                if (!allowed) {
                    showError(getString(R.string.login_admin_only));
                    return;
                }

                SessionManager sessionManager =
                        new SessionManager(LoginActivity.this);

                sessionManager.saveSession(body.getToken(), role);

                openMain();
            }

            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable throwable
            ) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                setLoading(false);

                if (throwable instanceof IOException) {
                    showError(getString(R.string.connection_network_error));
                } else {
                    showError(getString(R.string.connection_data_error));
                }
            }
        });
    }
    private String readLoginError(Response<?> response) {
        String fallback = getString(
                R.string.login_http_error,
                response.code()
        );

        // Không hiện chi tiết lỗi máy chủ.
        if (response.code() >= 500 || response.errorBody() == null) {
            return fallback;
        }

        try {
            JsonObject error = new Gson().fromJson(
                    response.errorBody().string(),
                    JsonObject.class
            );

            if (error != null
                    && error.has("message")
                    && error.get("message").isJsonPrimitive()) {

                String message = error.get("message").getAsString();

                if (!message.trim().isEmpty()) {
                    return message;
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // Dùng thông báo dự phòng nếu không đọc được JSON.
        }

        return fallback;
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (loginCall != null) {
            loginCall.cancel();
        }

        super.onDestroy();
    }
}