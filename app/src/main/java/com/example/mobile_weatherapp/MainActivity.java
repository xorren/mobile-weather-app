package com.example.mobile_weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MainActivity extends AppCompatActivity {
    private TextView currentWeather, currentTemperature, location;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentWeather = findViewById(R.id.currentWeather);
        currentTemperature = findViewById(R.id.currentTemperature);
        weatherIcon = findViewById(R.id.weatherIcon);
        location = findViewById(R.id.location);
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

        String city = "Seoul,kr";

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
                pm25TextView.setText(String.format("PM2.5(초 미세먼지): %s (%.2f µg/m³)", airQualityData.getPm25Status(), airQualityData.pm25));
                pm10TextView.setText(String.format("PM10(미세먼지): %s (%.2f µg/m³)", airQualityData.getPm10Status(), airQualityData.pm10));
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
                return new WeatherData(description, temperature, icon);
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
                updateForecast(forecastList);
                setupChart(forecastList); // 그래프 설정 기능 추가
            } else {
                // 에러 처리
                forecastDay1.setText("예보 데이터를 불러올 수 없습니다.");
                forecastTemp1.setText("");
                forecastIcon1.setImageResource(R.drawable.ic_error); // 기본 에러 아이콘 설정
                forecastDay2.setText("");
                forecastTemp2.setText("");
                forecastIcon2.setImageResource(R.drawable.ic_error);
                forecastDay3.setText("");
                forecastTemp3.setText("");
                forecastIcon3.setImageResource(R.drawable.ic_error);
                forecastDay4.setText("");
                forecastTemp4.setText("");
                forecastIcon4.setImageResource(R.drawable.ic_error);
                forecastDay5.setText("");
                forecastTemp5.setText("");
                forecastIcon5.setImageResource(R.drawable.ic_error);

                // 그래프 초기화
                lineChart.clear();
            }
        }

        private List<ForecastModel> lookUpForecast(String city) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // Replace with your actual API key
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

        private void setupChart(List<ForecastModel> forecastList) {
            List<Entry> entries = new ArrayList<>();
            List<String> dates = new ArrayList<>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM월 dd일", Locale.getDefault());

            for (int i = 0; i < forecastList.size(); i++) {
                ForecastModel forecast = forecastList.get(i);
                entries.add(new Entry(i, (float) forecast.getTemperature()));
                try {
                    Date date = inputFormat.parse(forecast.getDate());
                    dates.add(outputFormat.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, "Temperature");
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new DateValueFormatter(dates));
            xAxis.setGranularity(1f); // 최소 간격을 설정하여 날짜가 올바르게 표시되도록 함
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축을 아래에 표시

            lineChart.invalidate(); // refresh
        }

    }

    private void updateForecast(List<ForecastModel> forecastList) {
        setForecastData(forecastList.get(0), forecastDay1, forecastTemp1, forecastIcon1);
        setForecastData(forecastList.get(1), forecastDay2, forecastTemp2, forecastIcon2);
        setForecastData(forecastList.get(2), forecastDay3, forecastTemp3, forecastIcon3);
        setForecastData(forecastList.get(3), forecastDay4, forecastTemp4, forecastIcon4);
        setForecastData(forecastList.get(4), forecastDay5, forecastTemp5, forecastIcon5);
    }

    private void setForecastData(ForecastModel forecast, TextView day, TextView temp, ImageView icon) {
        day.setText(formatDate(forecast.getDate()));
        temp.setText(String.format("+%.0f°C", forecast.getTemperature()));
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

    private class WeatherData {
        String description;
        double temperature;
        String icon;

        WeatherData(String description, double temperature, String icon) {
            this.description = description;
            this.temperature = temperature;
            this.icon = icon;
        }
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
            return pm25 <= 12 ? "좋음" : "안좋음";
        }

        String getPm10Status() {
            return pm10 <= 50 ? "좋음" : "안좋음";
        }
    }


    private class ForecastModel {
        private String date;
        private double temperature;
        private String description;
        private String icon;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}
