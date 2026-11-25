package com.nhom404.bachhoa.admin.network;

import com.google.gson.JsonObject;
import com.nhom404.bachhoa.admin.model.HealthResponse;
import com.nhom404.bachhoa.admin.model.LoginRequest;
import com.nhom404.bachhoa.admin.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("health")
    Call<HealthResponse> checkHealth();

    @POST("auth/dang-nhap")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("auth/toi")
    Call<JsonObject> getCurrentUser();

    @GET("auth/kiem-tra-admin")
    Call<JsonObject> checkAdmin();
}