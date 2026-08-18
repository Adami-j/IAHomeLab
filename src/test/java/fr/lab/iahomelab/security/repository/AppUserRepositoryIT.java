package fr.lab.iahomelab.security.repository;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class AppUserRepositoryIT {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void shouldFindUserByUsername() {
        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        appUserRepository.saveAndFlush(user);

        Optional<AppUser> result =
                appUserRepository.findByUsername("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
        assertThat(result.get().getEmail()).isEqualTo("admin@test.local");
        assertThat(result.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldFindUserByEmail() {
        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        appUserRepository.saveAndFlush(user);

        Optional<AppUser> result =
                appUserRepository.findByEmail("admin@test.local");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
        assertThat(result.get().getEmail()).isEqualTo("admin@test.local");
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {
        Optional<AppUser> result =
                appUserRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<AppUser> result =
                appUserRepository.findByEmail("unknown@test.local");

        assertThat(result).isEmpty();
    }
}