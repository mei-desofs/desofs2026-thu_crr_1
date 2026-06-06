package com.techstore.app.helpers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Map;

public class CookiesHelper {

    public static void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access token - expira em 1 hora
        Cookie accessCookie = createCookie("access_token", accessToken, 3600);
        response.addCookie(accessCookie);

        // Refresh token - expira em 7 dias
        Cookie refreshCookie = createCookie("refresh_token", refreshToken, 7 * 24 * 3600);
        response.addCookie(refreshCookie);
    }

    public static void clearAuthCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }

    public static void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie accessCookie = createCookie("access_token", "", 0);
        response.addCookie(accessCookie);
    }

    public static void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshCookie = createCookie("refresh_token", "", 0);
        response.addCookie(refreshCookie);
    }

    public static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName(); // this is userId in your filter

    }
}
