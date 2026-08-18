package fr.lab.iahomelab.security.repository;

import fr.lab.iahomelab.security.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    Optional<UserIdentity> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );

}