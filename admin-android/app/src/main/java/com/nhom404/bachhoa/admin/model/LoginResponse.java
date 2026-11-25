package com.nhom404.bachhoa.admin.model;

public class LoginResponse {
    private String token;
    private NguoiDung nguoiDung;

    public String getToken() {
        return token;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public static class NguoiDung {
        private int id;
        private String hoTen;
        private String vaiTro;

        public int getId() {
            return id;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getVaiTro() {
            return vaiTro;
        }
    }
}