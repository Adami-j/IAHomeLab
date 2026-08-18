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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
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

    @Test
    void shouldRejectAnonymousUserTryUpdateSource() throws Exception {
        mockMvc.perform(put("/api/v1/sources/{id}", "blablabvla")
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
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateSourceStatusToRead() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Status test",
                              "url": "https://example.com/status-test",
                              "type": "ARTICLE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TO_READ"))
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
                              "title": "Status test",
                              "url": "https://example.com/status-test",
                              "type": "ARTICLE",
                              "status": "READ"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));

        mockMvc.perform(get("/api/v1/sources/{id}", id)
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }

    @Test
    void shouldUpdateSourceStatusToInterestingThenArchived() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Interesting source",
                              "url": "https://example.com/interesting",
                              "type": "PAPER"
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
                              "title": "Interesting source",
                              "url": "https://example.com/interesting",
                              "type": "PAPER",
                              "status": "INTERESTING"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERESTING"));

        mockMvc.perform(put("/api/v1/sources/{id}", id)
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Interesting source",
                              "url": "https://example.com/interesting",
                              "type": "PAPER",
                              "status": "ARCHIVED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(get("/api/v1/sources/{id}", id)
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void shouldCreateSourceWithTags() throws Exception {
        mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "RAG survey",
                              "url": "https://example.com/rag-survey",
                              "type": "PAPER",
                              "tags": [
                                "rag",
                                "llm",
                                "retrieval"
                              ]
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andExpect(jsonPath("$.tags", containsInAnyOrder(
                        "rag",
                        "llm",
                        "retrieval"
                )));
    }

    @Test
    void shouldUpdateSourceTags() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "RAG source",
                              "url": "https://example.com/rag-tags",
                              "type": "PAPER",
                              "tags": [
                                "rag",
                                "llm"
                              ]
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
                              "title": "RAG source",
                              "url": "https://example.com/rag-tags",
                              "type": "PAPER",
                              "status": "READ",
                              "tags": [
                                "agents",
                                "evaluation"
                              ]
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", containsInAnyOrder(
                        "agents",
                        "evaluation"
                )));

        mockMvc.perform(get("/api/v1/sources/{id}", id)
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", containsInAnyOrder(
                        "agents",
                        "evaluation"
                )));
    }

    @Test
    void shouldCreateSourceWithoutTags() throws Exception {
        mockMvc.perform(post("/api/v1/sources")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "No tags source",
                              "url": "https://example.com/no-tags",
                              "type": "ARTICLE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags", hasSize(0)));
    }
}