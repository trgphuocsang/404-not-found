package com.nhom404.bachhoa.admin.network;

import com.nhom404.bachhoa.admin.model.HealthResponse;

import retrofit2.Call;
import retrofit2.http.GET;
public interface ApiService {
    @GET("health")
    Call<HealthResponse> checkHealth();
}
