package com.example.property_management.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
    static final String PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    static final Pattern pattern = Pattern.compile(PATTERN);
    static Matcher matcher;

    public static boolean isValidEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
