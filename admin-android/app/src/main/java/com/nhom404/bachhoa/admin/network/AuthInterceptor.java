package com.nhom404.bachhoa.admin.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.nhom404.bachhoa.admin.LoginActivity;
import com.nhom404.bachhoa.admin.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;
    private final Context appContext;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public AuthInterceptor(
            SessionManager sessionManager,
            Context context
    ) {
        this.sessionManager = sessionManager;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // API đăng nhập không gửi token cũ.
        boolean isLogin = original.url().encodedPath()
                .endsWith("/auth/dang-nhap");

        String token = isLogin
                ? null
                : sessionManager.getAccessToken();

        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 401
                && token != null
                && !token.trim().isEmpty()) {

            mainHandler.post(() -> {
                // Không để phản hồi của phiên cũ xóa phiên mới.
                if (!token.equals(sessionManager.getAccessToken())) {
                    return;
                }

                sessionManager.clearSession();

                Intent intent = new Intent(
                        appContext,
                        LoginActivity.class
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                appContext.startActivity(intent);
            });
        }

        return response;
    }
}