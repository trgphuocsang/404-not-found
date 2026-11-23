package com.nhom404.bachhoa.admin.network;

import com.nhom404.bachhoa.admin.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json");

        String token = sessionManager.getAccessToken();

        if (token != null && !token.trim().isEmpty()) {
            requestBuilder.header(
                    "Authorization",
                    "Bearer " + token
            );
        }

        Request request = requestBuilder.build();

        return chain.proceed(request);
    }
}