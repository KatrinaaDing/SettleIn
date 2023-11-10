package com.example.property_management.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {
    static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,}$";
    static Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    static Matcher matcher;

    public static boolean isValidPassword(String password) {
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
