package fr.lab.iahomelab.setup.repository;

import fr.lab.iahomelab.setup.entity.Setup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SetupRepository extends JpaRepository<Setup, UUID> {

    List<Setup> findByNameEquals(String name);

    List<Setup> findByNameEqualsAndIdEquals(String name, UUID id);
}
