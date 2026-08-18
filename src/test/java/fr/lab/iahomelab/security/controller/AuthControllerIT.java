package fr.lab.iahomelab.security.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.IdentityType;
import fr.lab.iahomelab.security.entity.UserIdentity;
import fr.lab.iahomelab.security.entity.UserRole;
import fr.lab.iahomelab.security.repository.AppUserRepository;
import fr.lab.iahomelab.security.repository.UserIdentityRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userIdentityRepository.deleteAll();
        appUserRepository.deleteAll();

        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        AppUser savedUser = appUserRepository.save(user);

        UserIdentity identity = new UserIdentity(
                savedUser,
                IdentityType.LOCAL,
                "local",
                "admin",
                passwordEncoder.encode("test-password")
        );

        userIdentityRepository.save(identity);
    }

    @Test
    void shouldExposeCsrfToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectLoginWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "test-password"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAuthenticateAndPersistSession() throws Exception {

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf().asHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "test-password"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andReturn();

        HttpSession session = loginResult
                .getRequest()
                .getSession(false);

        assert session != null;
        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .session(
                                        (org.springframework.mock.web.MockHttpSession) session
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void shouldRejectInvalidPassword() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf().asHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "wrong-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}