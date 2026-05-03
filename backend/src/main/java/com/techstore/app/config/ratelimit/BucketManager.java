package com.techstore.app.config.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class BucketManager {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolve(String key, int capacity, Duration duration) {
        return buckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.intervally(capacity, duration)
            );

            return Bucket.builder()
                .addLimit(limit)
                .build();
        });
    }
}