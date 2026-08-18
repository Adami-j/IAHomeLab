package fr.lab.iahomelab.security.repository;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.IdentityType;
import fr.lab.iahomelab.security.entity.UserIdentity;
import fr.lab.iahomelab.security.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class UserIdentityRepositoryIT {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void shouldPersistLocalUserIdentity() {
        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        AppUser savedUser = appUserRepository.saveAndFlush(user);

        UserIdentity identity = new UserIdentity(
                savedUser,
                IdentityType.LOCAL,
                "local",
                "admin",
                "{bcrypt}dummy"
        );

        UserIdentity savedIdentity =
                userIdentityRepository.saveAndFlush(identity);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedIdentity.getId()).isNotNull();

        assertThat(savedIdentity.getUser().getId())
                .isEqualTo(savedUser.getId());

        assertThat(savedIdentity.getIdentityType())
                .isEqualTo(IdentityType.LOCAL);

        assertThat(savedIdentity.getProvider())
                .isEqualTo("local");

        assertThat(savedIdentity.getProviderSubject())
                .isEqualTo("admin");
    }
}