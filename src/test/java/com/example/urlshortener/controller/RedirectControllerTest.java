package com.example.urlshortener.controller;

import com.example.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void shouldRedirect_whenCodeExists() throws Exception {

        when(urlService.getOriginalUrl("abc123"))
                .thenReturn("https://www.google.com");

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    void shouldReturn404_whenCodeDoesNotExist() throws Exception {

        when(urlService.getOriginalUrl("unknown"))
                .thenReturn(null);

        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCallServiceOnce_withCorrectCode() throws Exception {

        when(urlService.getOriginalUrl("xyz999"))
                .thenReturn("https://www.github.com");

        mockMvc.perform(get("/xyz999"))
                .andExpect(status().isFound());

        verify(urlService, times(1)).getOriginalUrl("xyz999");
    }

    @Test
    void shouldPreserveFullUrl_inLocationHeader() throws Exception {

        String fullUrl = "https://www.example.com/path?foo=bar&baz=1";

        when(urlService.getOriginalUrl("abc123"))
                .thenReturn(fullUrl);

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", fullUrl));
    }

    @Test
    void shouldReturn404_whenCodeIsEmpty() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound());
    }
}