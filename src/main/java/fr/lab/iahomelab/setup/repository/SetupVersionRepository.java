package fr.lab.iahomelab.setup.repository;

import fr.lab.iahomelab.setup.entity.SetupVersion;
import fr.lab.iahomelab.setup.entity.SetupVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SetupVersionRepository extends JpaRepository<SetupVersion, UUID> {

    boolean existsBySetupIdAndStatus(UUID setupId, SetupVersionStatus status);
}
