package com.example.property_management.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlValidator {
    static final String PATTERN = "^(https?|ftp):\\/\\/[^\\s/$.?#].[^\\s]*$";
    static final Pattern pattern = Pattern.compile(PATTERN);
    static Matcher matcher;

    public static boolean isValidUrl(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
