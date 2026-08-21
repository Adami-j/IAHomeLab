package fr.lab.iahomelab.web.controller;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.IdentityType;
import fr.lab.iahomelab.security.entity.UserIdentity;
import fr.lab.iahomelab.security.entity.UserRole;
import fr.lab.iahomelab.security.repository.AppUserRepository;
import fr.lab.iahomelab.security.repository.UserIdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class LoginPageControllerIT {

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
    void shouldRenderLoginPageForAnonymousUser() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/login"))
                .andExpect(content().string(containsString("Connexion")));
    }

    @Test
    void shouldAuthenticateWithHtmlFormAndOpenApp() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "test-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app"))
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        assert session != null;

        mockMvc.perform(get("/app").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnToLoginPageWhenPasswordIsInvalid() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
