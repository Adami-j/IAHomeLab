package fr.lab.iahomelab.web.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.source.entity.Source;
import fr.lab.iahomelab.source.entity.SourceStatus;
import fr.lab.iahomelab.source.entity.SourceType;
import fr.lab.iahomelab.source.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SourcePageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SourceRepository sourceRepository;

    @BeforeEach
    void setUp() {
        sourceRepository.deleteAll();
    }

    @Test
    void shouldRenderSourceList() throws Exception {
        mockMvc.perform(get("/app/research")
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("source/list"))
                .andExpect(content().string(containsString("Nouvelle source")));
    }

    @Test
    void shouldCreateSourceFromHtmlForm() throws Exception {
        mockMvc.perform(post("/app/research")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .param("title", "Spring AI")
                        .param("url", "https://spring.io/projects/spring-ai")
                        .param("type", "DOCUMENTATION")
                        .param("status", "TO_READ")
                        .param("tags", "spring, ai"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/research"));

        assertThat(sourceRepository.count()).isEqualTo(1);
        Source source = sourceRepository.findAll().getFirst();
        assertThat(source.getTitle()).isEqualTo("Spring AI");
        assertThat(source.getTags()).containsExactlyInAnyOrder("spring", "ai");
    }

    @Test
    void shouldUpdateSourceFromHtmlForm() throws Exception {
        Source source = saveSource("Original", "https://example.com/original");

        mockMvc.perform(post("/app/research/{sourceId}/edit", source.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .param("title", "Updated")
                        .param("url", "https://example.com/updated")
                        .param("type", "PAPER")
                        .param("status", "READ")
                        .param("tags", "rag, evaluation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/research"));

        Source updated = sourceRepository.findById(source.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated");
        assertThat(updated.getType()).isEqualTo(SourceType.PAPER);
        assertThat(updated.getStatus()).isEqualTo(SourceStatus.READ);
        assertThat(updated.getTags()).containsExactlyInAnyOrder("rag", "evaluation");
    }

    @Test
    void shouldDeleteSourceFromHtmlPage() throws Exception {
        Source source = saveSource("Delete me", "https://example.com/delete-me");

        mockMvc.perform(post("/app/research/{sourceId}/delete", source.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/research"));

        assertThat(sourceRepository.existsById(source.getId())).isFalse();
    }

    private Source saveSource(String title, String url) {
        Source source = new Source();
        source.setTitle(title);
        source.setUrl(url);
        source.setType(SourceType.ARTICLE);
        source.setStatus(SourceStatus.TO_READ);
        return sourceRepository.save(source);
    }
}
