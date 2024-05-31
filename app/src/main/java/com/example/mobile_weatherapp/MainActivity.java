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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView currentWeather, currentTemperature, location;
    private ImageView weatherIcon;
    private TextView forecastDay1, forecastTemp1, forecastDay2, forecastTemp2, forecastDay3, forecastTemp3, forecastDay4, forecastTemp4, forecastDay5, forecastTemp5;
    private ImageView forecastIcon1, forecastIcon2, forecastIcon3, forecastIcon4, forecastIcon5;
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

        String city = "Seoul,kr";

        new FetchWeatherTask().execute(city);
        new FetchForecastTask().execute(city);
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
