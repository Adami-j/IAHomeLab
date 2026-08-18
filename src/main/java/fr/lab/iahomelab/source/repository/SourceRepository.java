package fr.lab.iahomelab.source.repository;

import fr.lab.iahomelab.source.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {

    Optional<Source> findByUrl(String url);

    boolean existsByUrl(String url);
}