package fr.lab.iahomelab.sourceidea.service;

import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.source.entity.Source;
import fr.lab.iahomelab.source.repository.SourceRepository;
import fr.lab.iahomelab.sourceidea.controller.dto.CreateSourceIdeaRequest;
import fr.lab.iahomelab.sourceidea.controller.dto.SourceIdeaResponse;
import fr.lab.iahomelab.sourceidea.entity.SourceIdea;
import fr.lab.iahomelab.sourceidea.repository.SourceIdeaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SourceIdeaService {

    private final SourceRepository sourceRepository;
    private final SourceIdeaRepository sourceIdeaRepository;

    @Transactional
    public SourceIdeaResponse create(UUID sourceId, CreateSourceIdeaRequest request) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found: " + sourceId));

        SourceIdea sourceIdea = new SourceIdea();
        sourceIdea.setSource(source);
        sourceIdea.setTitle(request.title());
        sourceIdea.setContent(request.content());
        sourceIdea.setType(request.type());

        return toResponse(sourceIdeaRepository.save(sourceIdea));
    }

    @Transactional(readOnly = true)
    public List<SourceIdeaResponse> getAllBySourceId(UUID sourceId) {
        if (!sourceRepository.existsById(sourceId)) {
            throw new ResourceNotFoundException("Source not found: " + sourceId);
        }

        return sourceIdeaRepository.findAllBySourceId(sourceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SourceIdeaResponse toResponse(SourceIdea sourceIdea) {
        return new SourceIdeaResponse(
                sourceIdea.getId(),
                sourceIdea.getSource().getId(),
                sourceIdea.getTitle(),
                sourceIdea.getContent(),
                sourceIdea.getType(),
                sourceIdea.getCreatedAt(),
                sourceIdea.getUpdatedAt()
        );
    }
}
