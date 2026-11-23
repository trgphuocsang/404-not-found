package com.nhom404.bachhoa.admin;
public interface PosCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}