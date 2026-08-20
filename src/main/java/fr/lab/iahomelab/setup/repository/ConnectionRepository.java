package fr.lab.iahomelab.setup.repository;

import fr.lab.iahomelab.setup.entity.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {

    List<Connection> findAllBySetupVersionId(UUID setupVersionId);
}
