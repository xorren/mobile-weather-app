package com.example.mobile_weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private TextView currentLocationTextView;
    private TextView currentWeatherTextView;
    private RequestQueue requestQueue;

    private TextView currentWeather;
    private TextView forecast1, forecast2, forecast3, forecast4, forecast5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        currentLocationTextView = findViewById(R.id.currentLocation);
        currentWeatherTextView = findViewById(R.id.currentWeather);
        requestQueue = Volley.newRequestQueue(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getLocationPermission();

        currentWeather = findViewById(R.id.currentWeather);
        forecast1 = findViewById(R.id.forecast1);
        forecast2 = findViewById(R.id.forecast2);
        forecast3 = findViewById(R.id.forecast3);
        forecast4 = findViewById(R.id.forecast4);
        forecast5 = findViewById(R.id.forecast5);

        // 기본 도시로 서울 설정
        String city = "Seoul,kr";
        new FetchWeatherTask().execute(city);
        new FetchForecastTask().execute(city);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String city = params[0];
            try {
                return lookUpWeather(city);
            } catch (IOException | JSONException e) {
                Log.e("FetchWeatherTask", "Error fetching weather data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String weather) {
            if (weather != null) {
                currentWeather.setText("현재 날씨 : " + weather);
            } else {
                currentWeather.setText("날씨 데이터를 불러올 수 없습니다.");
            }
        }

        private String lookUpWeather(String city) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // 실제 API 키로 교체하세요
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&APPID=" + apiKey + "&units=metric&lang=kr";
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.d("FetchWeatherTask", "Response Code: " + responseCode);

            if (responseCode == 200) { // HTTP OK
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.d("FetchWeatherTask", "Weather data: " + content.toString());
                JSONObject jsonObject = new JSONObject(content.toString());
                return jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.e("FetchWeatherTask", "Error response: " + content.toString());
                return null;
            }
        }
    }

    private class FetchForecastTask extends AsyncTask<String, Void, List<ForecastModel>> {
        @Override
        protected List<ForecastModel> doInBackground(String... params) {
            String city = params[0];
            try {
                return lookUpForecast(city);
            } catch (IOException | JSONException e) {
                Log.e("FetchForecastTask", "Error fetching forecast data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ForecastModel> forecastList) {
            if (forecastList != null && forecastList.size() >= 5) {
                forecast1.setText(formatForecast(forecastList.get(0)));
                forecast2.setText(formatForecast(forecastList.get(1)));
                forecast3.setText(formatForecast(forecastList.get(2)));
                forecast4.setText(formatForecast(forecastList.get(3)));
                forecast5.setText(formatForecast(forecastList.get(4)));
            } else {
                forecast1.setText("예보 데이터를 불러올 수 없습니다.");
                forecast2.setText("");
                forecast3.setText("");
                forecast4.setText("");
                forecast5.setText("");
            }
        }

        private List<ForecastModel> lookUpForecast(String city) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // 실제 API 키로 교체하세요
            String urlStr = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric&lang=kr";
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.d("FetchForecastTask", "Response Code: " + responseCode);

            if (responseCode == 200) { // HTTP OK
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.d("FetchForecastTask", "Forecast data: " + content.toString());
                JSONObject jsonObject = new JSONObject(content.toString());
                List<ForecastModel> forecastList = new ArrayList<>();
                JSONArray list = jsonObject.getJSONArray("list");
                for (int i = 0; i < list.length(); i += 8) { // 하루 간격으로 데이터 선택
                    JSONObject forecastObject = list.getJSONObject(i);
                    ForecastModel forecast = new ForecastModel();
                    forecast.setDate(forecastObject.getString("dt_txt"));
                    forecast.setTemperature(forecastObject.getJSONObject("main").getDouble("temp"));
                    forecast.setDescription(forecastObject.getJSONArray("weather").getJSONObject(0).getString("description"));
                    forecastList.add(forecast);
                }
                return forecastList;
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.e("FetchForecastTask", "Error response: " + content.toString());
                return null;
            }
        }

        private String formatForecast(ForecastModel forecast) {
            return forecast.getDate() + " - " + forecast.getTemperature() + "°C, " + forecast.getDescription();
        }
    }

    private class ForecastModel {
        private String date;
        private double temperature;
        private String description;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                double cur_lat = location.getLatitude();
                double cur_lon = location.getLongitude();
                getWeatherData(cur_lat, cur_lon);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    private void getWeatherData(double latitude, double longitude) {
        String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // 실제 API 키로 교체하세요
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + apiKey + "&lang=kr";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String cityName = response.getString("name"); // 지역명 추출
                            String weather = response.getJSONArray("weather").getJSONObject(0).getString("description");
                            String temperature = response.getJSONObject("main").getString("temp");
                            currentLocationTextView.setText("현재 위치 : " + cityName);
                            currentWeatherTextView.setText("현재 날씨 : " + weather + ", 온도: " + temperature + "°C");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }
}
