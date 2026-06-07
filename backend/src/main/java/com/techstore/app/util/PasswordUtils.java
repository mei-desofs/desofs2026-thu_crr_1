package com.techstore.app.util;

import com.techstore.app.client.HaveIBeenPwnedClient;
import com.techstore.app.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PasswordUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordUtils.class);
    private static final int MINIMUM_LENGTH = 12;

    private final HaveIBeenPwnedClient hibpClient;
    private final Set<String> commonPasswords;
    private final Set<String> contextWords = Set.of(
            "techstore", "tech store", "password", "welcome", "admin", "user");

    public PasswordUtils(HaveIBeenPwnedClient hibpClient) {
        this.hibpClient = hibpClient;
        this.commonPasswords = loadCommonPasswords();
    }

    public void validate(String password, String userEmail) {
        checkLength(password);
        checkContextWords(password, userEmail);
        checkCommonPasswords(password);
        checkBreached(password);
    }

    private void checkLength(String password) {
        if (password == null || password.length() < MINIMUM_LENGTH) {
            throw new BusinessException("Password must be at least " + MINIMUM_LENGTH + " characters long.");
        }
    }

    private void checkContextWords(String password, String userEmail) {
        String lower = password.toLowerCase();

        for (String word : contextWords) {
            if (lower.contains(word)) {
                throw new BusinessException("Password must not contain easily guessable words.");
            }
        }

        if (userEmail != null) {
            String localPart = userEmail.split("@")[0].toLowerCase();
            if (localPart.length() >= 4 && lower.contains(localPart)) {
                throw new BusinessException("Password must not contain parts of your email address.");
            }
        }
    }

    private void checkCommonPasswords(String password) {
        if (commonPasswords.contains(password.toLowerCase())) {
            throw new BusinessException("Password is too common. Please choose a more unique password.");
        }
    }

    private void checkBreached(String password) {
        if (hibpClient.isBreached(password)) {
            throw new BusinessException("This password has appeared in a known data breach. Please choose a different password.");
        }
    }

    private Set<String> loadCommonPasswords() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("common-passwords.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            LOGGER.warn("Could not load common passwords list: {}", e.getMessage());
            return new HashSet<>();
        }
    }
}
