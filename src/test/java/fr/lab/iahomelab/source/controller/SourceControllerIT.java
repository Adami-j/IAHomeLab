package fr.lab.iahomelab.source.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.source.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SourceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sourceRepository.deleteAll();
    }

    @Test
    void shouldCreateSource() throws Exception {
        mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Attention Is All You Need",
                                  "url": "https://arxiv.org/abs/1706.03762",
                                  "type": "PAPER",
                                  "summary": "Transformer paper"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title")
                        .value("Attention Is All You Need"))
                .andExpect(jsonPath("$.type").value("PAPER"))
                .andExpect(jsonPath("$.status").value("TO_READ"));
    }

    @Test
    void shouldCreateSourceWithStoragePathOnly() throws Exception {
        mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Local paper",
                                  "storagePath": "sources/papers/local-paper.pdf",
                                  "fileName": "local-paper.pdf",
                                  "mimeType": "application/pdf",
                                  "type": "PAPER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").isEmpty())
                .andExpect(jsonPath("$.storagePath")
                        .value("sources/papers/local-paper.pdf"));
    }

    @Test
    void shouldRejectSourceWithoutLocation() throws Exception {
        mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid source",
                                  "type": "ARTICLE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST"));
    }

    @Test
    void shouldGetSourceById() throws Exception {
        String response = mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Spring AI",
                                  "url": "https://spring.io/projects/spring-ai",
                                  "type": "DOCUMENTATION"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = new ObjectMapper()
                .readTree(response)
                .get("id").asString();

        mockMvc.perform(get("/api/v1/sources/{id}", id).with(user("test-user").roles("USER")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Spring AI"));
    }

    @Test
    void shouldReturn404WhenSourceDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/sources/{id}", id).with(user("test-user").roles("USER")).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/sources/{id}",
                        UUID.randomUUID()
                ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectPostWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Spring AI",
                                  "url": "https://spring.io/projects/spring-ai",
                                  "type": "DOCUMENTATION"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateSource() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Original title",
                              "url": "https://example.com/original",
                              "type": "ARTICLE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper
                .readTree(createResponse)
                .get("id").asString();

        mockMvc.perform(put("/api/v1/sources/{id}", id)
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Updated title",
                              "url": "https://example.com/updated",
                              "type": "DOCUMENTATION",
                              "status": "READ",
                              "summary": "Updated summary",
                              "notes": "Updated notes"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.url").value("https://example.com/updated"))
                .andExpect(jsonPath("$.type").value("DOCUMENTATION"))
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.summary").value("Updated summary"))
                .andExpect(jsonPath("$.notes").value("Updated notes"));
    }
}