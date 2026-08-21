package fr.lab.iahomelab.web.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.setup.entity.Setup;
import fr.lab.iahomelab.setup.entity.SetupVersion;
import fr.lab.iahomelab.setup.entity.SetupVersionStatus;
import fr.lab.iahomelab.setup.repository.SetupRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SetupPageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SetupRepository setupRepository;

    @Autowired
    private SetupVersionRepository setupVersionRepository;

    @BeforeEach
    void setUp() {
        setupVersionRepository.deleteAll();
        setupRepository.deleteAll();
    }

    @Test
    void shouldRenderSetupList() throws Exception {
        mockMvc.perform(get("/app/setups")
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/list"))
                .andExpect(content().string(containsString("Nouveau setup")));
    }

    @Test
    void shouldCreateSetupFromHtmlForm() throws Exception {
        mockMvc.perform(post("/app/setups")
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .param("name", "RAG Spring")
                        .param("description", "Workspace de test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/app/setups/*"));
    }

    @Test
    void shouldRenderSetupDetailAndWorkspace() throws Exception {
        Setup setup = new Setup();
        setup.setName("RAG Spring");
        setup.setDescription("Workspace de test");
        setup = setupRepository.save(setup);

        SetupVersion version = new SetupVersion();
        version.setSetup(setup);
        version.setVersionNumber(1);
        version.setStatus(SetupVersionStatus.DRAFT);
        version = setupVersionRepository.save(version);

        mockMvc.perform(get("/app/setups/{setupId}", setup.getId())
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/detail"))
                .andExpect(content().string(containsString("RAG Spring")));

        mockMvc.perform(get(
                                "/app/setups/{setupId}/versions/{versionId}",
                                setup.getId(),
                                version.getId()
                        )
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/workspace"))
                .andExpect(content().string(containsString("Graph workspace")));
    }
}
