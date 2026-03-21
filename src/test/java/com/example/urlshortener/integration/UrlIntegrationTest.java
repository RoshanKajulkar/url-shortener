package com.example.urlshortener.integration;

import com.example.urlshortener.repository.UrlRepository;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UrlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldCreateShortUrl_andStoreInDatabase() throws Exception {

        String requestBody = """
                {
                  "url": "google.com"
                }
                """;

        String responseJson = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String shortUrl = jsonMapper.readTree(responseJson)
                .get("shortUrl")
                .asString();

        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        var saved = urlRepository.findByShortCode(shortCode);
        assertThat(saved).isPresent();
        assertThat(saved.get().getOriginalUrl()).contains("google.com");
    }
}