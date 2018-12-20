package com.example.fgrin.fweather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fgrin.fweather.gson.Forecast;
import com.example.fgrin.fweather.gson.Weather;
import com.example.fgrin.fweather.util.HttpUtil;
import com.example.fgrin.fweather.util.Utility;
import com.example.fgrin.fweather.util.httpCallback;

import java.util.Calendar;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView aqiaverText;

    private ImageView weatherbackgroundImg;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;

    String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Button navButton;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        aqiaverText = (TextView) findViewById(R.id.aqiaver_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        weatherbackgroundImg = (ImageView) findViewById(R.id.weather_background_img);

        SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pres.getString("weather", null);


        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            requestWeather(weatherId);
            loadbackImg(weather);
            showWeatherInfo(weather);
        } else {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=20cae322779d45c7a672139cea57cb46";
        this.weatherId = weatherId;
        HttpUtil.sendHttpURLConnecion(weatherUrl, new httpCallback() {
            @Override
            public void onFinish(String response) {
                final String responseText = response;
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            loadbackImg(weather);
                            Toast.makeText(WeatherActivity.this, "更新天气信息成功",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°";
        String weatherInfo = weather.now.more.info;
        String aqiInfo = weather.aqi.city.aqi;
        int aqiInfoInt;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        aqiInfoInt = Integer.parseInt(aqiInfo);
        if (aqiInfoInt > 0 && aqiInfoInt <= 50)
            aqiaverText.setText("优");
        else if (aqiInfoInt > 50 && aqiInfoInt <= 100)
            aqiaverText.setText("良");
        else if (aqiInfoInt > 100 && aqiInfoInt <= 200)
            aqiaverText.setText("轻度污染");
        else if (aqiInfoInt > 200 && aqiInfoInt <= 300)
            aqiaverText.setText("中度污染");
        else if (aqiInfoInt > 300)
            aqiaverText.setText("重度污染");

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max + "°/");
            minText.setText(forecast.temperature.min + "°");
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(aqiInfo);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = weather.suggestion.comfort.info;
        comfortText.setText(comfort);
        weatherLayout.setVisibility(View.VISIBLE);


    }

    public void loadbackImg(Weather weather) {
        String weatherInfo = weather.now.more.info;
        int hour;

        boolean b1 = (weatherInfo.contains("晴"));
        boolean b2 = (weatherInfo.contains("阴"));
        boolean b3 = (weatherInfo.contains("多云"));
        boolean b4 = (weatherInfo.contains("雨"));
        boolean b5 = (weatherInfo.contains("雪"));

        Calendar rightnow = Calendar.getInstance();
        hour = rightnow.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour <= 18) {
            if (b1) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_clearsky);
            } else if (b2) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_cloudy);
            } else if (b3) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_partlycloudy);
            } else if (b4) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_rain);
            } else if (b5) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_snow);
            } else {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_fog);
            }
        } else if ((hour >= 0 && hour < 6) || (hour > 18 && hour < 24)) {
            if (b1) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.night_clearsky);
            } else if (b2) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.night_cloudy);
            } else if (b3) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.night_partlycloudy);
            } else if (b4) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.night_rain);
            } else if (b5) {
                weatherbackgroundImg.setBackgroundResource(R.drawable.night_snow);
            } else {
                weatherbackgroundImg.setBackgroundResource(R.drawable.day_fog);
            }
        }
    }
}
