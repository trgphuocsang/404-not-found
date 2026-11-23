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

public class LoginActivity extends AppCompatActivity {

    private EditText edtAccount;
    private EditText edtPassword;
    private TextView tvLoginError;
    private ProgressBar progressLogin;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                .setOnClickListener(view -> {
                    Intent intent = new Intent(
                            LoginActivity.this,
                            MainActivity.class
                    );
                    startActivity(intent);
                });
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

        // Điểm nối API ở phần tiếp theo.
        // Chưa lưu token hoặc mở MainActivity tại đây.
        showError(getString(R.string.login_api_not_ready));
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
}