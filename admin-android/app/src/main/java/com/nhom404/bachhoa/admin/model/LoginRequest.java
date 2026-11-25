package com.nhom404.bachhoa.admin.model;

public class LoginRequest {
    private final String soDienThoai;
    private final String matKhau;

    public LoginRequest(String soDienThoai, String matKhau) {
        this.soDienThoai = soDienThoai;
        this.matKhau = matKhau;
    }
}