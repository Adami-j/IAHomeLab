package fr.lab.iahomelab.sourceidea.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.source.entity.Source;
import fr.lab.iahomelab.source.entity.SourceType;
import fr.lab.iahomelab.source.repository.SourceRepository;
import fr.lab.iahomelab.sourceidea.entity.SourceIdea;
import fr.lab.iahomelab.sourceidea.entity.SourceIdeaType;
import fr.lab.iahomelab.sourceidea.repository.SourceIdeaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                                  "content": "Compare vector search with BM25 plus reranking.",
                                  "type": "IDEA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.sourceId").value(source.getId().toString()))
                .andExpect(jsonPath("$.title").value("Try hybrid retrieval"))
                .andExpect(jsonPath("$.content").value("Compare vector search with BM25 plus reranking."))
                .andExpect(jsonPath("$.type").value("IDEA"));
    }

    @Test
    void shouldListIdeasAndClaimsForSource() throws Exception {
        Source source = createSource("https://example.com/source-list-ideas");
        createSourceIdea(source, "First idea", "Experiment with chunk size.", SourceIdeaType.IDEA);
        createSourceIdea(source, "Main claim", "Smaller chunks improve retrieval precision.", SourceIdeaType.CLAIM);

        mockMvc.perform(get("/api/v1/sources/{sourceId}/ideas", source.getId())
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("First idea", "Main claim")))
                .andExpect(jsonPath("$[*].type", containsInAnyOrder("IDEA", "CLAIM")));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingIdeaForUnknownSource() throws Exception {
        UUID sourceId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/sources/{sourceId}/ideas", sourceId)
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unknown source idea",
                                  "content": "This source does not exist.",
                                  "type": "IDEA"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnNotFoundWhenListingIdeasForUnknownSource() throws Exception {
        UUID sourceId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/sources/{sourceId}/ideas", sourceId)
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldRejectAnonymousUserTryingToCreateIdea() throws Exception {
        Source source = createSource("https://example.com/source-anonymous-idea");

        mockMvc.perform(post("/api/v1/sources/{sourceId}/ideas", source.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous idea",
                                  "content": "Must not be created.",
                                  "type": "IDEA"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private Source createSource(String url) {
        Source source = new Source();
        source.setTitle("Test source");
        source.setUrl(url);
        source.setType(SourceType.ARTICLE);
        return sourceRepository.save(source);
    }

    private void createSourceIdea(
            Source source,
            String title,
            String content,
            SourceIdeaType type
    ) {
        SourceIdea sourceIdea = new SourceIdea();
        sourceIdea.setSource(source);
        sourceIdea.setTitle(title);
        sourceIdea.setContent(content);
        sourceIdea.setType(type);
        sourceIdeaRepository.save(sourceIdea);
    }
}
