package fr.lab.iahomelab.sourceidea.repository;

import fr.lab.iahomelab.sourceidea.entity.SourceIdea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceIdeaRepository
        extends JpaRepository<SourceIdea, UUID> {

    List<SourceIdea> findAllBySourceId(UUID sourceId);
}