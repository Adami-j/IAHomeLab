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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class UserIdentityRepositoryIT {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void shouldPersistLocalUserIdentity() {
        AppUser savedUser = createUser();

        UserIdentity identity = new UserIdentity(
                savedUser,
                IdentityType.LOCAL,
                "local",
                "admin",
                "{bcrypt}dummy"
        );

        UserIdentity savedIdentity =
                userIdentityRepository.saveAndFlush(identity);

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

    @Test
    void shouldFindIdentityByProviderAndProviderSubject() {
        AppUser savedUser = createUser();

        UserIdentity identity = new UserIdentity(
                savedUser,
                IdentityType.LOCAL,
                "local",
                "admin",
                "{bcrypt}dummy"
        );

        userIdentityRepository.saveAndFlush(identity);

        Optional<UserIdentity> result =
                userIdentityRepository.findByProviderAndProviderSubject(
                        "local",
                        "admin"
                );

        assertThat(result).isPresent();

        UserIdentity foundIdentity = result.get();

        assertThat(foundIdentity.getProvider())
                .isEqualTo("local");

        assertThat(foundIdentity.getProviderSubject())
                .isEqualTo("admin");

        assertThat(foundIdentity.getIdentityType())
                .isEqualTo(IdentityType.LOCAL);

        assertThat(foundIdentity.getUser().getId())
                .isEqualTo(savedUser.getId());
    }

    @Test
    void shouldReturnEmptyWhenIdentityDoesNotExist() {
        Optional<UserIdentity> result =
                userIdentityRepository.findByProviderAndProviderSubject(
                        "google",
                        "unknown"
                );

        assertThat(result).isEmpty();
    }

    private AppUser createUser() {
        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        return appUserRepository.saveAndFlush(user);
    }
     @Test
     void createTwiceUsernameUser() {
        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        AppUser userDuplicated = new AppUser(
                "admin",
                "adminTwice@test.local",
                "Admin",
                UserRole.ADMIN
        );
        appUserRepository.saveAndFlush(user);

         assertThatThrownBy(() ->
                 appUserRepository.saveAndFlush(userDuplicated)
         ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void createTwiceEmailUser() {
        AppUser user = new AppUser(
                "admin",
                "adminTwice@test.local",
                "Admin",
                UserRole.ADMIN
        );

        AppUser userDuplicated = new AppUser(
                "admin1",
                "adminTwice@test.local",
                "Admin",
                UserRole.ADMIN
        );
        appUserRepository.saveAndFlush(user);

        assertThatThrownBy(() ->
                appUserRepository.saveAndFlush(userDuplicated)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void createTwiceDisplayNameUser() {
        AppUser user = new AppUser(
                "admin",
                "admin@test.local",
                "Admin",
                UserRole.ADMIN
        );

        AppUser userDuplicated = new AppUser(
                "admin1",
                "adminTwice@test.local",
                "Admin",
                UserRole.ADMIN
        );
        appUserRepository.saveAndFlush(user);


        appUserRepository.saveAndFlush(userDuplicated);

    }

}