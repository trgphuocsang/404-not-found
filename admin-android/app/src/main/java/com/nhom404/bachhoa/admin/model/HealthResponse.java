package com.nhom404.bachhoa.admin.model;

import com.google.gson.annotations.SerializedName;
public class HealthResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public String getStatus(){
        return status;
    }

    public String getMessage(){
        return message;
    }
}
