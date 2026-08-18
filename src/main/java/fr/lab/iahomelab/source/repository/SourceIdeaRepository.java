package fr.lab.iahomelab.source.repository;

import fr.lab.iahomelab.source.entity.SourceIdea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceIdeaRepository extends JpaRepository<SourceIdea, UUID> {

    List<SourceIdea> findAllBySourceIdOrderByCreatedAtAsc(UUID sourceId);
}
