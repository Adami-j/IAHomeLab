package fr.lab.iahomelab.setup.repository;

import fr.lab.iahomelab.setup.entity.Setup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SetupRepository extends JpaRepository<Setup, UUID> {
}
