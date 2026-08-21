package fr.lab.iahomelab.web.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class NavigationPageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderExperimentsPage() throws Exception {
        mockMvc.perform(get("/app/experiments")
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/experiments"));
    }

    @Test
    void shouldRenderFindingsPage() throws Exception {
        mockMvc.perform(get("/app/findings")
                        .with(user("test-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/findings"));
    }
}
