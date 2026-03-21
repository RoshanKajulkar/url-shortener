package com.example.urlshortener.integration;

import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.model.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;  // ✅ Boot 4.x
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RedirectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    private void seedUrl(String shortCode, String originalUrl) {
        Url url = new Url();
        url.setShortCode(shortCode);
        url.setOriginalUrl(originalUrl);
        urlRepository.save(url);
    }

    @Test
    void shouldRedirect_whenCodeExistsInDatabase() throws Exception {

        seedUrl("abc123", "https://www.google.com");

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())                           // 302
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    void shouldReturn404_whenCodeDoesNotExistInDatabase() throws Exception {

        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreserveFullUrl_withQueryParams() throws Exception {

        String fullUrl = "https://www.example.com/path?foo=bar&baz=1";
        seedUrl("xyz999", fullUrl);

        mockMvc.perform(get("/xyz999"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", fullUrl));
    }

    @Test
    void shouldRedirect_afterShorteningUrl() throws Exception {

        seedUrl("e2etest", "https://www.github.com");

        mockMvc.perform(get("/e2etest"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.github.com"));
    }
}