package fr.lab.iahomelab.setup.repository;

import fr.lab.iahomelab.setup.entity.SetupVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SetupVersionRepository extends JpaRepository<SetupVersion, UUID> {
}
