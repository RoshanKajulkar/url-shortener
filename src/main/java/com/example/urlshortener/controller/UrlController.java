package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.github.bucket4j.Bucket;
import com.example.urlshortener.config.RateLimiter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "URL Shortener", description = "APIs for URL shortening service")
@RestController
@RequestMapping("/api")
public class UrlController {

    private final RateLimiter rateLimiter;
    private final UrlService urlService;

    public UrlController(UrlService urlService, RateLimiter rateLimiter) {
        this.urlService = urlService;
        this.rateLimiter = rateLimiter;
    }

    @Operation(summary = "Create a short URL")
    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody ShortenRequest request, HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429)
                    .body(Map.of("error", "Too many requests"));
        }

        String url = request.getUrl();

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL is required"));
        }

        // Normalize URL
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        try {
            java.net.URI uri = new java.net.URI(url);

            if (uri.getHost() == null || !uri.getHost().contains(".")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid domain"));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid URL format"));
        }

        String code = urlService.createShortUrl(url);

        return ResponseEntity.ok(
                Map.of("shortUrl", "http://localhost:8080/" + code)
        );
    }
}