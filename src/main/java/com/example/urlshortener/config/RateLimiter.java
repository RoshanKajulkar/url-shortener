package com.example.urlshortener.config;

import io.github.bucket4j.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, this::newBucket);
    }

    private Bucket newBucket(String ip) {

        Bandwidth limit = Bandwidth.builder()
                .capacity(10) // max tokens
                .refillIntervally(10, Duration.ofMinutes(1)) // refill 10 every minute
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}