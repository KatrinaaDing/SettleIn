package com.example.property_management.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

/**
 * This class is used to format date and time.
 */
public class DateTimeFormatter {

    /**
     * Format date object to "dd MMM yyyy"
     * @param date Date object
     * @return String formatted date
     */
    public static String dateFormatter(Date date) {
        if (date == null) return "";
        String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        return formattedDate;
    }

    public static String dateTimeFormatter(Date date) {
        if (date == null) return "";
        String formattedDate = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date);
        return formattedDate;
    }

    /**
     * Format LocalDate object to "dd MMM yyyy"
     * @param date LocalDate object
     * @return String formatted date
     */
    public static String dateFormatter(LocalDate date) {
        if (date == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());
        String formattedDate = date.format(formatter);
        return formattedDate;
    }

    /**
     * Format LocalTime object to "HH:mm"
     * @param hour hour
     * @param minute minute
     * @return String formatted time
     */
    public static String timeFormatter(int hour, int minute) {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        return formattedTime;
    }

    /**
     * Convert String to LocalDate. The string must be in "dd MMM yyyy" format.
     * @param date String date
     * @return LocalDate object
     */
    public static LocalDate stringToDate(String date) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());
        LocalDate formattedDate = LocalDate.parse(date, formatter);
        return formattedDate;
    }

    /**
     * Convert String to LocalTime. The string must be in "HH:mm" format.
     * @param time String time
     * @return LocalTime object
     */
    public static LocalTime stringToTime(String time) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
        LocalTime formattedTime = LocalTime.parse(time, formatter);
        return formattedTime;
    }

    /**
     * get epoch millis from LocalDate.
     * refrence: https://stackoverflow.com/questions/22990067/how-to-extract-epoch-from-localdate-
     * and-localdatetime
     * @param date LocalDate
     * @return long epoch millis in long
     */
    public static long localDateToLong(LocalDate date) {
        ZoneId zoneId = ZoneId.of("UTC");
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli();
    }

}
