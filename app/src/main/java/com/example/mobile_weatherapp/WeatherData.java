package com.example.mobile_weatherapp;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

// WeatherData.java 예시 (디버깅을 위해 로그 추가)
public class WeatherData {
    public String lookUpWeather(String date, String time, String x, String y) throws IOException, JSONException {
        // 여기서 실제 API 호출 및 데이터 처리
        Log.d("WeatherData", "날짜: " + date + " 시간: " + time + " x: " + x + " y: " + y);

        // 예시 응답
        String response = "Cloudy 50% 22.0 3.5 0.0 60%";
        Log.d("WeatherDataResponse", response);
        return response;
    }
}
