package com.techstore.app.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Component
public class HaveIBeenPwnedClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HaveIBeenPwnedClient.class);

    @Value("${hibp.api.url}")
    private String HIBP_URL;

    private final RestTemplate restTemplate;

    public HaveIBeenPwnedClient(@Qualifier("supabaseRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Returns true if the password has appeared in a known breach.
     * Uses k-anonymity — only the first 5 chars of the SHA-1 hash are sent.
     * The full password never leaves the server.
     */
    public boolean isBreached(String password) {
        try {
            String sha1 = sha1(password).toUpperCase();
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);

            ResponseEntity<String> response = restTemplate.getForEntity(HIBP_URL + prefix, String.class);

            if (response.getBody() == null) return false;

            return Arrays.stream(response.getBody().split("\n"))
                    .map(line -> line.split(":")[0].trim())
                    .anyMatch(s -> s.equalsIgnoreCase(suffix));

        } catch (Exception e) {
            LOGGER.warn("HaveIBeenPwned check failed: {}", e.getMessage());
            return false;
        }
    }

    private String sha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
