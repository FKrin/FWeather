package com.example.fgrin.fweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.example.fgrin.fweather.gson.Weather;
import com.example.fgrin.fweather.util.Utility;

import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 */
public class FWeatherWidget extends AppWidgetProvider {


    void updateAppWidget(Context context, AppWidgetManager appWidgetManager) {

        SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(context);
        String weatherString = pres.getString("weather", null);
        Weather weather = Utility.handleWeatherResponse(weatherString);
        String weatherInfo = weather.now.more.info;
        int hour;
        boolean b1 = (weatherInfo.contains("晴"));
        boolean b2 = (weatherInfo.contains("阴"));
        boolean b3 = (weatherInfo.contains("多云"));
        boolean b4 = (weatherInfo.contains("雨"));
        boolean b5 = (weatherInfo.contains("雪"));

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fweather_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        Calendar rightnow = Calendar.getInstance();
        hour = rightnow.get(Calendar.HOUR_OF_DAY);

        views.setTextViewText(R.id.textView_widget_time, getTime());
        views.setTextViewText(R.id.textView_widget_location, weather.basic.cityName);
        views.setTextViewText(R.id.textView_widget_avgTemp, weather.now.temperature + "°");
        views.setTextViewText(R.id.textView_widget_info, weather.now.more.info);
        views.setTextViewText(R.id.textView_widget_date, getDate());
        if (hour >= 6 && hour <= 18) {
            if (b1) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_clearsky_2x4);
            } else if (b2) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_cloudy_2x4);
            } else if (b3) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_partlycloudy_2x4);
            } else if (b4) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_rain_2x4);
            } else if (b5) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_snow_2x4);
            } else {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_fog_2x4);
            }
        } else if ((hour >= 0 && hour < 6) || (hour > 18 && hour < 24)) {
            if (b1) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.night_clearsky_2x4);
            } else if (b2) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.night_cloudy_2x4);
            } else if (b3) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.night_partlycloudy_2x4);
            } else if (b4) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.night_rain_2x4);
            } else if (b5) {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.night_snow_2x4);
            } else {
                views.setImageViewResource(R.id.weatherwidget_background_img, R.drawable.day_fog_2x4);
            }
        }

        // Instruct the widget manager to update the widget
        ComponentName componentName = new ComponentName(context, FWeatherWidget.class);
        appWidgetManager.updateAppWidget(componentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appId = appWidgetIds[i];
            Intent intent1 = new Intent(context, WeatherActivity.class);
            intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appId);  // Identifies the particular widget...
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent1.setData(Uri.parse(intent1.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendIntent = PendingIntent.getActivity(context, 0, intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fweather_widget);
            views.setOnClickPendingIntent(R.id.weatherwidget_background_img, pendIntent);
            appWidgetManager.updateAppWidget(appId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateAppWidget(context, appWidgetManager);
    }

    @Override
    public void onEnabled(Context context) {
        context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK));
        super.onEnabled(context);
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_TICK)) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fweather_widget);
            views.setTextViewText(R.id.textView_widget_time, getTime());
            views.setTextViewText(R.id.textView_widget_date, getDate());
            ComponentName wd = new ComponentName(context, FWeatherWidget.class);
            AppWidgetManager.getInstance(context).updateAppWidget(wd, views);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        updateAppWidget(context, appWidgetManager);

    }

    public String getTime() {
        final Calendar date = Calendar.getInstance();
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        return new StringBuffer().append(hour < 10 ? "0" + hour : hour).append(
                ":").append(minute < 10 ? "0" + minute : minute).toString();
    }

    public String getDate() {
        final Calendar date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH) + 1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        return new StringBuffer().append(month).append(
                "月").append(day).append("日").toString();
    }

}

