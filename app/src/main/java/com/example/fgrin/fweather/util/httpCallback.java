package com.example.fgrin.fweather.util;

public interface httpCallback {
    void onFinish(String response);
    void onError(Exception e);

}
