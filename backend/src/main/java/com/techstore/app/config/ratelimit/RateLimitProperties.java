package com.techstore.app.config.ratelimit;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "ratelimit")
@Component
@Getter
@Setter
public class RateLimitProperties {

    private Map<String, Rule> rules;

    @Getter
    @Setter
    public static class Rule {
        private RateLimitType type;
        private int capacity;
        private Duration duration;
    }
}
