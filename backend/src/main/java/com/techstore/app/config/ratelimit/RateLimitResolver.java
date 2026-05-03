package com.techstore.app.config.ratelimit;

import org.springframework.stereotype.Component;

@Component
public class RateLimitResolver {

    public String resolve(RateLimitType type, String userId, String ip) {
        return switch (type) {
            case USER -> {
                if (userId == null) {
                     throw new IllegalArgumentException("userId is required for RateLimitType.USER");
                }
                yield "user:" + userId;
            }
            case USER_OR_IP -> (userId != null) ? "user:" + userId : "ip:" + ip;
            case IP -> "ip:" + ip;
        };
    }
}
