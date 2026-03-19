package com.example.urlshortener.controller;

import com.example.urlshortener.service.UrlService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, String> request) {

        String url = request.get("url");

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