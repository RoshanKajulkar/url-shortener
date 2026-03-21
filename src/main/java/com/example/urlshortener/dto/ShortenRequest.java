package com.example.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ShortenRequest {

    @Schema(example = "https://google.com", description = "Original URL to shorten")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}