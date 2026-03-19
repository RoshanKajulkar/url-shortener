package com.example.urlshortener.service;

import com.example.urlshortener.model.Url;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.util.Base62Util;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {

    private final StringRedisTemplate redisTemplate;

    private final UrlRepository urlRepository;
    private final Random random = new Random();

    @Value("${cache.ttl.seconds}")
    private long cacheTtl;

    public UrlService(UrlRepository urlRepository, StringRedisTemplate redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    public String createShortUrl(String originalUrl) {

        var existing = urlRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return existing.get().getShortCode();
        }

        String code;

        do {
            long randomNum = Math.abs(random.nextLong());
            code = Base62Util.encode(randomNum).substring(0, 6);
        } while (urlRepository.findByShortCode(code).isPresent());

        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0);
        url.setShortCode(code);

        urlRepository.save(url);

        return code;
    }

    public String getOriginalUrl(String code) {

        String cached = redisTemplate.opsForValue().get(code);
        if (cached != null) {
            return cached;
        }

        return urlRepository.findByShortCode(code)
                .map(url -> {
                    url.setClickCount(
                            url.getClickCount() == null ? 1 : url.getClickCount() + 1
                    );
                    urlRepository.save(url);

                    redisTemplate.opsForValue().set(code, url.getOriginalUrl(), cacheTtl, TimeUnit.SECONDS);

                    return url.getOriginalUrl();
                })
                .orElse(null);
    }
}