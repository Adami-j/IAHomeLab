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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
    void shouldUpdateAndDeleteSetupFromHtmlPages() throws Exception {
        Setup setup = saveSetup("Initial setup");

        mockMvc.perform(post("/app/setups/{setupId}/edit", setup.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .param("name", "Updated setup")
                        .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/setups/" + setup.getId()));

        Setup updated = setupRepository.findById(setup.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated setup");
        assertThat(updated.getDescription()).isEqualTo("Updated description");

        mockMvc.perform(post("/app/setups/{setupId}/delete", setup.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/setups"));

        assertThat(setupRepository.existsById(setup.getId())).isFalse();
    }

    @Test
    void shouldCreateUpdateAndFreezeVersionFromHtmlPages() throws Exception {
        Setup setup = saveSetup("Versioned setup");

        mockMvc.perform(post("/app/setups/{setupId}/versions", setup.getId())
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .param("description", "First draft"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/app/setups/*/versions/*"));

        SetupVersion version = setupVersionRepository.findAll().get(0);
        assertThat(version.getStatus()).isEqualTo(SetupVersionStatus.DRAFT);
        assertThat(version.getDescription()).isEqualTo("First draft");

        mockMvc.perform(post(
                                "/app/setups/{setupId}/versions/{versionId}/update",
                                setup.getId(),
                                version.getId()
                        )
                        .with(user("test-user").roles("USER"))
                        .with(csrf())
                        .param("description", "Ready to freeze"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/app/setups/" + setup.getId() + "/versions/" + version.getId()
                ));

        assertThat(setupVersionRepository.findById(version.getId()).orElseThrow().getDescription())
                .isEqualTo("Ready to freeze");

        mockMvc.perform(post(
                                "/app/setups/{setupId}/versions/{versionId}/freeze",
                                setup.getId(),
                                version.getId()
                        )
                        .with(user("test-user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(setupVersionRepository.findById(version.getId()).orElseThrow().getStatus())
                .isEqualTo(SetupVersionStatus.FROZEN);
    }

    @Test
    void shouldDeleteDraftVersionFromHtmlPage() throws Exception {
        Setup setup = saveSetup("Disposable version");
        SetupVersion version = saveVersion(setup, SetupVersionStatus.DRAFT);

        mockMvc.perform(post(
                                "/app/setups/{setupId}/versions/{versionId}/delete",
                                setup.getId(),
                                version.getId()
                        )
                        .with(user("test-user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/setups/" + setup.getId()));

        assertThat(setupVersionRepository.existsById(version.getId())).isFalse();
    }

    @Test
    void shouldRenderSetupDetailAndWorkspace() throws Exception {
        Setup setup = saveSetup("RAG Spring");
        setup.setDescription("Workspace de test");
        setup = setupRepository.save(setup);

        SetupVersion version = saveVersion(setup, SetupVersionStatus.DRAFT);

        mockMvc.perform(get("/app/setups/{setupId}", setup.getId())
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/detail"))
                .andExpect(content().string(containsString("RAG Spring")))
                .andExpect(content().string(containsString("Modifier")))
                .andExpect(content().string(containsString("Nouvelle version")));

        mockMvc.perform(get(
                                "/app/setups/{setupId}/versions/{versionId}",
                                setup.getId(),
                                version.getId()
                        )
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/workspace"))
                .andExpect(content().string(containsString("Graph workspace")))
                .andExpect(content().string(containsString("Figer la version")));
    }

    private Setup saveSetup(String name) {
        Setup setup = new Setup();
        setup.setName(name);
        return setupRepository.save(setup);
    }

    private SetupVersion saveVersion(Setup setup, SetupVersionStatus status) {
        SetupVersion version = new SetupVersion();
        version.setSetup(setup);
        version.setVersionNumber(1);
        version.setStatus(status);
        return setupVersionRepository.save(version);
    }
}
