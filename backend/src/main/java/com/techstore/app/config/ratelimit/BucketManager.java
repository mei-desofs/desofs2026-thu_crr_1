package com.techstore.app.config.ratelimit;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class BucketManager {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS) // TTL
            .maximumSize(10_000)
            .build();

    public Bucket resolve(String key, int capacity, Duration duration) {
        return buckets.get(key, k -> createBucket(capacity, duration));
    }

    private Bucket createBucket(int capacity, Duration duration) {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.intervally(capacity, duration)
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}