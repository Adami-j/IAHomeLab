package fr.lab.iahomelab.source.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.source.entity.Source;
import fr.lab.iahomelab.source.entity.SourceType;
import fr.lab.iahomelab.source.repository.SourceIdeaRepository;
import fr.lab.iahomelab.source.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SourceIdeaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceIdeaRepository sourceIdeaRepository;

    @BeforeEach
    void setUp() {
        sourceIdeaRepository.deleteAll();
        sourceRepository.deleteAll();
    }

    @Test
    void shouldCreateIdeaForSource() throws Exception {
        Source source = createSource("https://example.com/source-idea");

        mockMvc.perform(post("/api/v1/sources/{sourceId}/ideas", source.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Try hybrid retrieval",
                                  "content": "Compare dense retrieval with BM25 + reranking.",
                                  "type": "IDEA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.sourceId").value(source.getId().toString()))
                .andExpect(jsonPath("$.title").value("Try hybrid retrieval"))
                .andExpect(jsonPath("$.type").value("IDEA"));
    }

    @Test
    void shouldCreateClaimForSourceAndListIdeas() throws Exception {
        Source source = createSource("https://example.com/source-claim");

        mockMvc.perform(post("/api/v1/sources/{sourceId}/ideas", source.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Claim about reranking",
                                  "content": "A reranker improves retrieval quality on this benchmark.",
                                  "type": "CLAIM"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/sources/{sourceId}/ideas", source.getId())
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sourceId").value(source.getId().toString()))
                .andExpect(jsonPath("$[0].title").value("Claim about reranking"))
                .andExpect(jsonPath("$[0].type").value("CLAIM"));
    }

    @Test
    void shouldReturnNotFoundWhenSourceDoesNotExist() throws Exception {
        UUID sourceId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/sources/{sourceId}/ideas", sourceId)
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing source",
                                  "content": "This source does not exist.",
                                  "type": "IDEA"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldRejectAnonymousUser() throws Exception {
        Source source = createSource("https://example.com/anonymous-source-idea");

        mockMvc.perform(get("/api/v1/sources/{sourceId}/ideas", source.getId()))
                .andExpect(status().isUnauthorized());
    }

    private Source createSource(String url) {
        Source source = new Source();
        source.setTitle("Source for idea test");
        source.setUrl(url);
        source.setType(SourceType.ARTICLE);
        return sourceRepository.save(source);
    }
}
