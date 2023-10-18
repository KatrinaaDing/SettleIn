package com.example.property_management.utils;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Helpers {

    /**
     * Close the keyboard
     * @param activity the activity that is currently open
     */
    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String dateFormatter(Date date) {
        if (date == null) return "";
        String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        return formattedDate;
    }

    public static String dateFormatter(LocalDate date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());
        String formattedDate = date.format(formatter);
        return formattedDate;
    }

    public static String timeFormatter(int hour, int minute) {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        return formattedTime;
    }

    public static LocalDate stringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());
        LocalDate formattedDate = LocalDate.parse(date, formatter);
        return formattedDate;
    }

    public static LocalTime stringToTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
        LocalTime formattedTime = LocalTime.parse(time, formatter);
        return formattedTime;
    }

    /**
     * get epoch from LocalDate.
     * refrence: https://stackoverflow.com/questions/22990067/how-to-extract-epoch-from-localdate-
     * and-localdatetime
     * @param date LocalDate
     * @return epoch in long
     */
    public static long localDateToLong(LocalDate date) {
        ZoneId zoneId = ZoneId.of("UTC");
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli();
    }

}
