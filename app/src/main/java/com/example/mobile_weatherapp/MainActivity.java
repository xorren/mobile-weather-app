package com.example.mobile_weatherapp;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private String date = "", time = "";
    private String x = "60", y = "127";
    private String weather = "";
    private TextView currentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        /*
        현재 날씨 알림 ---------------------------------------------------------------
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        currentWeather = findViewById(R.id.currentWeather);

        long now = System.currentTimeMillis();
        Date mDate = new Date(now);

        // 날짜, 시간의 형식 설정
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");

        // 현재 날짜를 받아오는 형식 설정 ex) 20221121
        String getDate = simpleDateFormat1.format(mDate);
        // 현재 시간를 받아오는 형식 설정, 시간만 가져오고 WeatherData의 timechange()를 사용하기 위해 시간만 가져오고 뒤에 00을 붙임 ex) 02 + "00"
        String getTime = simpleDateFormat2.format(mDate) + "00";
        String CurrentTime = simpleDateFormat2.format(mDate) + ":00";
        Log.d("date", getDate + getTime);
        // 현재 월 가져오기 봄 = 3월 ~ 5월 / 여름 = 6월 ~ 8월 / 가을 = 9월, 10월 / 겨울 = 11월 ~ 2월
        String getSeason = simpleDateFormat.format(mDate);

        WeatherData wd = new WeatherData();
        try {
            date = getDate;
            time = getTime;
            weather = wd.lookUpWeather(date, time, x, y);
        } catch (IOException e) {
            Log.i("THREE_ERROR1", e.getMessage());
        } catch (JSONException e) {
            Log.i("THREE_ERROR2", e.getMessage());
        }
        Log.d("현재날씨",weather);

        // return한 값을 " " 기준으로 자른 후 배열에 추가
        // array[0] = 구름의 양, array[1] = 강수 확률, array[2] = 기온, array[3] = 풍속, array[4] = 적설량, array[5] = 습도
        String[] weatherarray = weather.split(" ");
        for(int i = 0; i < weatherarray.length; i++) {
            Log.d("weather = ", i + " " + weatherarray[i]);
        }

        currentWeather.setText("현재 날씨 : " + weatherarray[0]);

    }
}