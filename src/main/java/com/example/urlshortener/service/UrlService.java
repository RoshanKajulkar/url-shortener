package com.example.urlshortener.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UrlService {

    private final Map<String, String> store = new HashMap<>();

    public String createShortUrl(String originalUrl) {
        String code = UUID.randomUUID().toString().substring(0, 6);

        store.put(code, originalUrl);

        return code;
    }
}