package fr.lab.iahomelab.security.repository;

import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    boolean existsByRole(UserRole userRole);
}