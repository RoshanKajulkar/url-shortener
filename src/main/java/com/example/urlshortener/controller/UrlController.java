package com.example.urlshortener.controller;

import com.example.urlshortener.service.UrlService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public Map<String, String> shorten(@RequestBody Map<String, String> request) {

        String url = request.get("url");

        String code = urlService.createShortUrl(url);

        return Map.of(
                "shortUrl", "http://localhost:8080/" + code
        );
    }
}