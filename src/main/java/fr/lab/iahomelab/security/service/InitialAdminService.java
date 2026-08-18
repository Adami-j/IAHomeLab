package fr.lab.iahomelab.security.service;

import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.IdentityType;
import fr.lab.iahomelab.security.entity.UserIdentity;
import fr.lab.iahomelab.security.entity.UserRole;
import fr.lab.iahomelab.security.repository.AppUserRepository;
import fr.lab.iahomelab.security.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "iahl.initial-admin",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InitialAdminService implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @NullMarked
    public void run(ApplicationArguments args) {

        if (appUserRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        String username = System.getenv("IAHL_INITIAL_USERNAME");
        String password = System.getenv("IAHL_INITIAL_PASSWORD");
        String email = System.getenv("IAHL_INITIAL_EMAIL");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "No administrator exists. "
                            + "IAHL_INITIAL_USERNAME and IAHL_INITIAL_PASSWORD are required."
            );
        }

        AppUser user = new AppUser(
                username,
                email,
                "Administrator",
                UserRole.ADMIN
        );

        AppUser savedUser = appUserRepository.save(user);

        UserIdentity identity = new UserIdentity(
                savedUser,
                IdentityType.LOCAL,
                "local",
                username,
                passwordEncoder.encode(password)
        );

        userIdentityRepository.save(identity);
    }
}