package com.techstore.app.util;

public class PasswordUtils {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$";

    private PasswordUtils() {}

    public static boolean isValid(String password) {
        if (password == null || password.isBlank()) return false;
        return password.matches(PASSWORD_REGEX);
    }
}
