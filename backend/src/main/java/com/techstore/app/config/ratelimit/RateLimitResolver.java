package com.techstore.app.config.ratelimit;

import org.springframework.stereotype.Component;

@Component
public class RateLimitResolver {

    public String resolve(RateLimitType type, String userId, String ip) {
        return switch (type) {
            case USER, USER_OR_IP -> (userId != null) ? "user:" + userId : "ip:" + ip;
            case IP -> "ip:" + ip;
        };
    }
}
