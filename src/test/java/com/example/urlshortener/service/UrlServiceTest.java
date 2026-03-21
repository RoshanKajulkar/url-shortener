package com.example.urlshortener.service;

import com.example.urlshortener.model.Url;
import com.example.urlshortener.repository.UrlRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(urlService, "cacheTtl", 60L);
    }

    @Test
    void shouldReturnExistingShortCode_whenUrlAlreadyExists() {
        Url url = new Url();
        url.setOriginalUrl("https://google.com");
        url.setShortCode("abc123");

        when(urlRepository.findByOriginalUrl("https://google.com"))
                .thenReturn(Optional.of(url));

        String result = urlService.createShortUrl("https://google.com");

        assertEquals("abc123", result);
        verify(urlRepository, never()).save(any());
    }

    @Test
    void shouldCreateNewShortCode_whenUrlDoesNotExist() {
        when(urlRepository.findByOriginalUrl(any()))
                .thenReturn(Optional.empty());
        when(urlRepository.findByShortCode(any()))
                .thenReturn(Optional.empty());

        String code = urlService.createShortUrl("https://test.com");

        assertNotNull(code);
        assertTrue(code.matches("[a-zA-Z0-9]{6}"));

        verify(urlRepository, times(1)).save(any());
    }

    @Test
    void shouldRetryWhenShortCodeCollisionOccurs() {

        when(urlRepository.findByOriginalUrl(any()))
                .thenReturn(Optional.empty());

        // First call → collision, Second call → no collision
        when(urlRepository.findByShortCode(any()))
                .thenReturn(Optional.of(new Url()))  // collision
                .thenReturn(Optional.empty());       // success

        String code = urlService.createShortUrl("https://collision.com");

        assertNotNull(code);
        verify(urlRepository, times(2)).findByShortCode(any());
        verify(urlRepository).save(any());
    }

    @Test
    void shouldReturnFromCache_whenPresentInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("abc123"))
                .thenReturn("https://cached.com");

        String result = urlService.getOriginalUrl("abc123");

        assertEquals("https://cached.com", result);
        verify(urlRepository, never()).findByShortCode(any());
    }

    @Test
    void shouldFetchFromDbAndCache_whenNotInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Url url = new Url();
        url.setOriginalUrl("https://db.com");
        url.setShortCode("abc123");
        url.setClickCount(0);

        when(valueOperations.get("abc123")).thenReturn(null);
        when(urlRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl("abc123");

        assertEquals("https://db.com", result);

        verify(urlRepository).save(argThat(saved ->
                saved.getClickCount() == 1
        ));

        verify(valueOperations).set(
                eq("abc123"),
                eq("https://db.com"),
                eq(60L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void shouldReturnNull_whenCodeNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);
        when(urlRepository.findByShortCode(any()))
                .thenReturn(Optional.empty());

        String result = urlService.getOriginalUrl("invalid");

        assertNull(result);
    }
}