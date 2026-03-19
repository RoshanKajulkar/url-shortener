package com.example.urlshortener.service;

import com.example.urlshortener.util.Base62Util;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UrlService {

    private final Map<String, String> store = new HashMap<>();
    private long counter = 1;

    public String createShortUrl(String originalUrl) {
        String code = Base62Util.encode(counter);
        counter++;

        store.put(code, originalUrl);

        return code;
    }

    public String getOriginalUrl(String code) {
        return store.get(code);
    }
}