package fr.lab.iahomelab.setup.repository;

import fr.lab.iahomelab.setup.entity.ComponentInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ComponentInstanceRepository extends JpaRepository<ComponentInstance, UUID> {
}
