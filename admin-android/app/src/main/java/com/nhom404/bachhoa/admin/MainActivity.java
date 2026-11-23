package com.nhom404.bachhoa.admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhom404.bachhoa.admin.model.HealthResponse;
import com.nhom404.bachhoa.admin.network.ApiClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "NHOM404_MAIN";

    private ProgressBar progressLoading;
    private TextView tvStatus;
    private Button btnCheckConnection;

    private Call<HealthResponse> healthCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        progressLoading = findViewById(R.id.progressLoading);
        tvStatus = findViewById(R.id.tvStatus);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);

        btnCheckConnection.setOnClickListener(view -> checkConnection());
    }
    private void checkConnection() {
        setLoading(true);
        tvStatus.setText(R.string.connection_loading);

        healthCall = ApiClient.getApiService(this).checkHealth();

        healthCall.enqueue(new Callback<HealthResponse>() {

            @Override
            public void onResponse(
                    Call<HealthResponse> call,
                    Response<HealthResponse> response
            ) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                setLoading(false);

                if (!response.isSuccessful()) {
                    tvStatus.setText(getString(
                            R.string.connection_http_error,
                            response.code()
                    ));

                    Log.w(TAG, "Health HTTP error: " + response.code());
                    return;
                }

                HealthResponse body = response.body();

                if (body == null
                        || !"ok".equals(body.getStatus())
                        || body.getMessage() == null) {

                    tvStatus.setText(R.string.connection_invalid_response);
                    Log.w(TAG, "Invalid health response");
                    return;
                }

                tvStatus.setText(getString(
                        R.string.connection_success,
                        body.getMessage()
                ));

                Log.d(TAG, "Health check successful");
            }

            @Override
            public void onFailure(
                    Call<HealthResponse> call,
                    Throwable throwable
            ) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                setLoading(false);

                if (throwable instanceof IOException) {
                    tvStatus.setText(R.string.connection_network_error);
                } else {
                    tvStatus.setText(R.string.connection_data_error);
                }

                Log.e(TAG, "Health check failed", throwable);
            }
        });
    }

    private void setLoading(boolean loading) {
        progressLoading.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnCheckConnection.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        if (healthCall != null) {
            healthCall.cancel();
        }

        super.onDestroy();
    }
}