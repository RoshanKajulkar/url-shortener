package com.example.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ShortenResponse {

    @Schema(example = "http://localhost:8080/abc123", description = "Generated short URL")
    private String shortUrl;

    public ShortenResponse(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}