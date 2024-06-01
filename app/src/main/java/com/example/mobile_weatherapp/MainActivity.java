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
import android.widget.ImageView;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private TextView currentLocationTextView;
    private TextView currentWeatherTextView;
    private RequestQueue requestQueue;
    
    private TextView forecast1, forecast2, forecast3, forecast4, forecast5;
    private TextView currentWeather, currentTemperature, location, rainProbability;
    private ImageView weatherIcon;
    private TextView forecastDay1, forecastTemp1, forecastDay2, forecastTemp2, forecastDay3, forecastTemp3, forecastDay4, forecastTemp4, forecastDay5, forecastTemp5;
    private ImageView forecastIcon1, forecastIcon2, forecastIcon3, forecastIcon4, forecastIcon5;
    private TextView pm25TextView, pm10TextView;
    private LineChart lineChart;

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
        currentTemperature = findViewById(R.id.currentTemperature);
        weatherIcon = findViewById(R.id.weatherIcon);
        location = findViewById(R.id.location);
        rainProbability = findViewById(R.id.rainProbability);
        forecastDay1 = findViewById(R.id.forecastDay1);
        forecastTemp1 = findViewById(R.id.forecastTemp1);
        forecastIcon1 = findViewById(R.id.forecastIcon1);
        forecastDay2 = findViewById(R.id.forecastDay2);
        forecastTemp2 = findViewById(R.id.forecastTemp2);
        forecastIcon2 = findViewById(R.id.forecastIcon2);
        forecastDay3 = findViewById(R.id.forecastDay3);
        forecastTemp3 = findViewById(R.id.forecastTemp3);
        forecastIcon3 = findViewById(R.id.forecastIcon3);
        forecastDay4 = findViewById(R.id.forecastDay4);
        forecastTemp4 = findViewById(R.id.forecastTemp4);
        forecastIcon4 = findViewById(R.id.forecastIcon4);
        forecastDay5 = findViewById(R.id.forecastDay5);
        forecastTemp5 = findViewById(R.id.forecastTemp5);
        forecastIcon5 = findViewById(R.id.forecastIcon5);
        pm25TextView = findViewById(R.id.pm25TextView);
        pm10TextView = findViewById(R.id.pm10TextView);
        lineChart = findViewById(R.id.lineChart);

        String city = "Seoul, KR";

        new FetchWeatherTask().execute(city);
        new FetchForecastTask().execute(city);
        new FetchAirQualityTask().execute(city);
    }

    private class FetchAirQualityTask extends AsyncTask<String, Void, AirQualityData> {
        @Override
        protected AirQualityData doInBackground(String... params) {
            String city = params[0];
            try {
                return lookUpAirQuality(city);
            } catch (IOException | JSONException e) {
                Log.e("FetchAirQualityTask", "Error fetching air quality data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(AirQualityData airQualityData) {
            if (airQualityData != null) {
                pm25TextView.setText(String.format("PM2.5 (초미세먼지): %s (%.2f µg/m³)", airQualityData.getPm25Status(), airQualityData.pm25));
                pm10TextView.setText(String.format("PM10 (미세먼지): %s (%.2f µg/m³)", airQualityData.getPm10Status(), airQualityData.pm10));
            } else {
                pm25TextView.setText("미세먼지 데이터를 불러올 수 없습니다.");
                pm10TextView.setText("");
            }
        }

        private AirQualityData lookUpAirQuality(String city) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // Replace with your actual API key
            String urlStr = "https://api.openweathermap.org/data/2.5/air_pollution?lat=37.5665&lon=126.9780&appid=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.d("FetchAirQualityTask", "Response Code: " + responseCode);

            if (responseCode == 200) { // HTTP OK
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.d("FetchAirQualityTask", "Air quality data: " + content.toString());
                JSONObject jsonObject = new JSONObject(content.toString());
                JSONObject main = jsonObject.getJSONArray("list").getJSONObject(0).getJSONObject("components");
                double pm25 = main.getDouble("pm2_5");
                double pm10 = main.getDouble("pm10");
                return new AirQualityData(pm25, pm10);
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.e("FetchAirQualityTask", "Error response: " + content.toString());
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
                Log.e("FetchForecastTask", "Error fetching forecast data");
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ForecastModel> forecastList) {
            if (forecastList != null && forecastList.size() >= 5) {
                updateForecast(forecastList);
            } else {
                forecastDay1.setText("예보 데이터를 불러올 수 없습니다.");
                forecastTemp1.setText("");
                forecastDay2.setText("");
                forecastTemp2.setText("");
                forecastDay3.setText("");
                forecastTemp3.setText("");
                forecastDay4.setText("");
                forecastTemp4.setText("");
                forecastDay5.setText("");
                forecastTemp5.setText("");
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
                    forecast.setIcon(forecastObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
                    forecast.setRainProbability(forecastObject.has("rain") ? forecastObject.getJSONObject("rain").optDouble("3h", 0) * 100 : 0);
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

        private void updateForecast(List<ForecastModel> forecastList) {
            setForecastData(forecastList.get(0), forecastDay1, forecastTemp1, forecastIcon1);
            setForecastData(forecastList.get(1), forecastDay2, forecastTemp2, forecastIcon2);
            setForecastData(forecastList.get(2), forecastDay3, forecastTemp3, forecastIcon3);
            setForecastData(forecastList.get(3), forecastDay4, forecastTemp4, forecastIcon4);
            setForecastData(forecastList.get(4), forecastDay5, forecastTemp5, forecastIcon5);
            setupChart(forecastList); // 그래프 설정
        }

        private void setForecastData(ForecastModel forecast, TextView day, TextView temp, ImageView icon) {
            day.setText(formatDate(forecast.getDate()));
            temp.setText(String.format("+%.0f°C \n(비 올 확률: %.0f%%)", forecast.getTemperature(), forecast.getRainProbability()));
            int iconResource = getResources().getIdentifier("ic_" + forecast.getIcon(), "drawable", getPackageName());
            icon.setImageResource(iconResource);
        }

        private String formatDate(String dateStr) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM월 dd일", Locale.getDefault());
            try {
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return dateStr;
            }
        }
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, WeatherData> {
        @Override
        protected WeatherData doInBackground(String... params) {
            String city = params[0];
            try {
                return lookUpWeather(city);
            } catch (IOException | JSONException e) {
                Log.e("FetchWeatherTask", "Error fetching weather data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(WeatherData weatherData) {
            if (weatherData != null) {
                currentWeather.setText(weatherData.description);
                currentTemperature.setText(String.format("+%.0f°C", weatherData.temperature));
                rainProbability.setText(String.format("비 올 확률: %.0f%%", weatherData.rainProbability));
                int iconResource = getResources().getIdentifier("ic_" + weatherData.icon, "drawable", getPackageName());
                weatherIcon.setImageResource(iconResource);
            } else {
                currentWeather.setText("날씨 데이터를 불러올 수 없습니다.");
            }
        }

        private WeatherData lookUpWeather(String city) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // Replace with your actual API key
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
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                double temperature = jsonObject.getJSONObject("main").getDouble("temp");
                String icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                double rainProbability = jsonObject.has("rain") ? jsonObject.getJSONObject("rain").optDouble("1h", 0) * 100 : 0;
                return new WeatherData(description, temperature, icon, rainProbability);
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

    private class WeatherData {
        String description;
        double temperature;
        String icon;
        double rainProbability;

        WeatherData(String description, double temperature, String icon, double rainProbability) {
            this.description = description;
            this.temperature = temperature;
            this.icon = icon;
            this.rainProbability = rainProbability;
        }
    }

    private class ForecastModel {
        private String date;
        private double temperature;
        private String description;
        private String icon;
        private double rainProbability;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public double getRainProbability() { return rainProbability; }
        public void setRainProbability(double rainProbability) { this.rainProbability = rainProbability; }
    }

    public class DateValueFormatter extends ValueFormatter {
        private final List<String> dates;

        public DateValueFormatter(List<String> dates) {
            this.dates = dates;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            return dates.get(index);
        }
    }

    private class AirQualityData {
        double pm25;
        double pm10;

        AirQualityData(double pm25, double pm10) {
            this.pm25 = pm25;
            this.pm10 = pm10;
        }

        String getPm25Status() {
            if (pm25 <= 5) return "완전 좋음";
            else if (pm25 <= 15) return "좋음";
            else if (pm25 <= 35) return "보통";
            else if (pm25 <= 75) return "나쁨";
            else if (pm25 <= 100) return "완전 나쁨";
            else return "최악";
        }

        String getPm10Status() {
            if (pm10 <= 15) return "완전 좋음";
            else if (pm10 <= 30) return "좋음";
            else if (pm10 <= 50) return "보통";
            else if (pm10 <= 100) return "나쁨";
            else if (pm10 <= 150) return "완전 나쁨";
            else return "최악";
        }
    }

    private void setupChart(List<ForecastModel> forecastList) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < forecastList.size(); i++) {
            ForecastModel forecast = forecastList.get(i);
            entries.add(new Entry(i, (float) forecast.getTemperature()));
            dates.add(formatDateForChart(forecast.getDate()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter(dates));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.invalidate(); // refresh
    }

    private String formatDateForChart(String dateStr) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr;
        }
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
