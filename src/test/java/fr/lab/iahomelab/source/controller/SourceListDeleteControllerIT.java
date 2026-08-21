package fr.lab.iahomelab.source.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SourceListDeleteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SourceRepository sourceRepository;

    @BeforeEach
    void setUp() {
        sourceRepository.deleteAll();
    }

    @Test
    void shouldListSources() throws Exception {
        saveSource("Source A", "https://example.com/a");
        saveSource("Source B", "https://example.com/b");

        mockMvc.perform(get("/api/v1/sources")
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldDeleteSource() throws Exception {
        Source source = saveSource("Delete me", "https://example.com/delete-api");

        mockMvc.perform(delete("/api/v1/sources/{id}", source.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/sources/{id}", source.getId())
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isNotFound());
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
