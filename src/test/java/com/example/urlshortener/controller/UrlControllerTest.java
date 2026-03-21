package com.example.urlshortener.controller;

import com.example.urlshortener.config.RateLimiter;
import com.example.urlshortener.service.UrlService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private RateLimiter rateLimiter;

    private RequestPostProcessor withIp() {
        return request -> {
            request.setRemoteAddr("127.0.0.1");
            return request;
        };
    }

    @Test
    void shouldReturnShortUrl_whenValidRequest() throws Exception {

        Bucket bucket = Mockito.mock(Bucket.class);

        when(rateLimiter.resolveBucket(any())).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(urlService.createShortUrl(any())).thenReturn("abc123");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"google.com\"}")
                        .with(withIp()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl")
                        .value("http://localhost:8080/abc123"));
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {

        Bucket bucket = Mockito.mock(Bucket.class);

        when(rateLimiter.resolveBucket(any())).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"google.com\"}")
                        .with(withIp()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error")
                        .value("Too many requests"));
    }

    @Test
    void shouldReturn400_whenUrlMissing() throws Exception {

        Bucket bucket = Mockito.mock(Bucket.class);

        when(rateLimiter.resolveBucket(any())).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(withIp()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("URL is required"));
    }

    @Test
    void shouldReturn400_whenInvalidDomain() throws Exception {

        Bucket bucket = Mockito.mock(Bucket.class);

        when(rateLimiter.resolveBucket(any())).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"invalid\"}")
                        .with(withIp()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Invalid domain"));
    }

    @Test
    void shouldReturn400_whenInvalidUrlFormat() throws Exception {

        Bucket bucket = Mockito.mock(Bucket.class);

        when(rateLimiter.resolveBucket(any())).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"http://\"}")
                        .with(withIp()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Invalid URL format"));
    }
}