package com.nhom404.bachhoa.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nhom404.bachhoa.admin.model.HealthResponse;
import com.nhom404.bachhoa.admin.network.ApiClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectionCheckActivity extends AppCompatActivity {

    private static final String TAG = "NHOM404_Connection";

    private ProgressBar progressLoading;
    private TextView tvStatus;
    private Button btnCheckConnection;

    private Call<HealthResponse> healthCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connection_check);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.connectionRoot),
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

        progressLoading = findViewById(R.id.progressLoading);
        tvStatus = findViewById(R.id.tvStatus);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);

        btnCheckConnection.setOnClickListener(
                view -> checkConnection()
        );
    }

    private void checkConnection() {
        if (!btnCheckConnection.isEnabled()) {
            return;
        }

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
                    return;
                }

                HealthResponse body = response.body();

                if (body == null
                        || !"ok".equals(body.getStatus())
                        || body.getMessage() == null) {

                    tvStatus.setText(
                            R.string.connection_invalid_response
                    );
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
                    tvStatus.setText(
                            R.string.connection_network_error
                    );
                } else {
                    tvStatus.setText(
                            R.string.connection_data_error
                    );
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