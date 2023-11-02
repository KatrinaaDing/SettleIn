package com.example.property_management.sensors;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.CalendarContract;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.property_management.callbacks.SensorCallback;
import com.example.property_management.utils.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

public class CalendarSensor {
    private Activity activity;
    private SensorCallback callback;
    private static final int MY_CALENDAR_REQUEST_CODE = 1;

    public CalendarSensor(Activity activity, SensorCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void requiresPermissions() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                MY_CALENDAR_REQUEST_CODE);
    }

    /**
     * Create a new event in the calendar
     * reference: https://www.geeksforgeeks.org/how-to-set-calendar-event-in-android/
     */
    public void createEvent(String date, String time, int durationInMinutes, String title,
                            String description, String address) throws Exception {
        LocalDate localDate = DateTimeFormatter.stringToDate(date);
        LocalTime localTime = DateTimeFormatter.stringToTime(time);

        boolean allDay = false;
        if (localDate == null) {
            throw new Exception("Invalid date format");
        }
        // if time is not specified, make it an all day event
        if (localTime == null) {
            allDay = true;
            localTime = LocalTime.of(0, 0);
        }
        long startTimeMillis = DateTimeFormatter.localDateToMillis(LocalDateTime.of(localDate, localTime));
        long endTimeMillis = startTimeMillis + durationInMinutes * 60 * 1000;
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
                .putExtra(CalendarContract.Events.ALL_DAY, allDay)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, address)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        activity.startActivity(intent);
    }

    public int getMyCalendarRequestCode() {
        return MY_CALENDAR_REQUEST_CODE;
    }

    public static boolean getHasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }
}
