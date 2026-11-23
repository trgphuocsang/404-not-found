package com.nhom404.bachhoa.admin.network;

import android.content.Context;

import com.nhom404.bachhoa.admin.session.SessionManager;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {
    //Đợi đổi URL backend đang chạy bằng đt thật
    private static final String BASE_URL = "http://127.0.0.1:3000/";

    private static ApiService apiService;

    private ApiClient(){

    }

    public static synchronized ApiService getApiService(Context context){
        if(apiService == null){
            SessionManager sessionManager =
                    new SessionManager(context.getApplicationContext());
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(60, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
