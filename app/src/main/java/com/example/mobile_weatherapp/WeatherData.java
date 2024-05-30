package com.example.mobile_weatherapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class WeatherData {

    private String sky, temperature, wind, rain, snow, humidity;
    private String current_weather_code;
    private String data_sky;

    public String lookUpWeather(String date, String time, String nx, String ny) throws IOException, JSONException {
        String baseDate = date; // 2022xxxx 형식을 사용해야 함
        String baseTime = timeChange(time); // 0500 형식을 사용해야 함
        if(baseTime == "0000"){
            baseTime = "2300";
            int intbaseDate = Integer.parseInt(baseDate);
            baseDate = String.valueOf(intbaseDate - 1);
        }

        String type = "json";

        // end point 주소값
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        // 일반 인증키
        String serviceKey = "fRgos7g5gESUMp9kdmxdkfBnGKHcCbz9UPLPfcmk%2BHWYVccJDKs%2Bq6h%2FqTx98xeJr3A1SPS%2FAmKPXqZpwe%2FREA%3D%3D";

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey); // 서비스 키
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); // x좌표
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); // y좌표
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("14", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* 조회하고싶은 날짜*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));    /* 타입 */

        /*
         * GET방식으로 전송해서 파라미터 받아오기
         */
        URL url = new URL(urlBuilder.toString());
        // json데이터들을 웹페이지를통해 확인할 수 있게  로그캣에 링크 출력
        System.out.println("url : " + url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String result = sb.toString();


        // response 키를 가지고 데이터를 파싱
        JSONObject jsonObj_1 = new JSONObject(result);
        String response = jsonObj_1.getString("response");
        Log.d("API Response", jsonObj_1.toString());

        // response 로 부터 body 찾기
        JSONObject jsonObj_2 = new JSONObject(response);
        String body = jsonObj_2.getString("body");

        // body 로 부터 items 찾기
        JSONObject jsonObj_3 = new JSONObject(body);
        String items = jsonObj_3.getString("items");
        Log.i("ITEMS", items);

        // items로 부터 itemlist 를 받기
        JSONObject jsonObj_4 = new JSONObject(items);
        JSONArray jsonArray = jsonObj_4.getJSONArray("item");

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObj_4 = jsonArray.getJSONObject(i);
            String fcstValue = jsonObj_4.getString("fcstValue");
            String category = jsonObj_4.getString("category");

            if (category.equals("SKY")) {
                if (fcstValue.equals("1")) {
                    sky = "맑음 ";
                    current_weather_code = "1";
                } else if (fcstValue.equals("2")) {
                    sky = "비 ";
                    current_weather_code = "2";
                } else if (fcstValue.equals("3")) {
                    sky = "구름많음 ";
                    current_weather_code = "3";
                } else if (fcstValue.equals("4")) {
                    sky = "흐림 ";
                    current_weather_code = "4";
                }
            }

            if (category.equals("TMP")) {
                temperature = fcstValue + "℃ ";
            }

            if(category.equals("WSD")) {
                wind = fcstValue + "m/s ";
            }

            if(category.equals("POP")) {
                rain = fcstValue + "% ";
            }
            if(category.equals("SNO")) {
                snow = fcstValue + " ";
            }
            if(category.equals("REH")) {
                humidity = fcstValue + "%";
            }

        }

        return sky + rain + temperature + wind + snow + humidity;
    }

    public String timeChange(String time)
    {
        // 현재 시간에 따라 데이터 시간 설정(3시간 마다 업데이트) //
        /**
         시간은 3시간 단위로 조회해야 함
         ex) 0200, 0500, 0800 ~ 2300
         시간을 3시간 단위로 변경하는 메소드
         **/
        switch(time) {

            case "0200":
            case "0300":
            case "0400":
                time = "0200";
                break;
            case "0500":
            case "0600":
            case "0700":
                time = "0500";
                break;
            case "0800":
            case "0900":
            case "1000":
                time = "0800";
                break;
            case "1100":
            case "1200":
            case "1300":
                time = "1100";
                break;
            case "1400":
            case "1500":
            case "1600":
                time = "1400";
                break;
            case "1700":
            case "1800":
            case "1900":
                time = "1700";
                break;
            case "2000":
            case "2100":
            case "2200":
                time = "2000";
                break;
            case "2300":
                time = "2300";
            case "0000":
            case "0100":
                time = "0000";

        }
        return time;
    }

}