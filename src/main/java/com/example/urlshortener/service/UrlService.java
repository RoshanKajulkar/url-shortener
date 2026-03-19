package com.example.urlshortener.service;

import com.example.urlshortener.util.Base62Util;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UrlService {

    private final Map<String, String> codeToUrl = new HashMap<>();
    private final Map<String, String> urlToCode = new HashMap<>();

    private long counter = 1;

    public String createShortUrl(String originalUrl) {

        if (urlToCode.containsKey(originalUrl)) {
            return urlToCode.get(originalUrl);
        }

        String code = Base62Util.encode(counter);
        counter++;

        codeToUrl.put(code, originalUrl);
        urlToCode.put(originalUrl, code);

        return code;
    }

    public String getOriginalUrl(String code) {
        return codeToUrl.get(code);
    }
}