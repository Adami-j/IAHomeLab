package fr.lab.iahomelab.source.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.source.controller.dto.CreateSourceRequest;
import fr.lab.iahomelab.source.controller.dto.SourceResponse;
import fr.lab.iahomelab.source.controller.dto.UpdateSourceRequest;
import fr.lab.iahomelab.source.entity.Source;
import fr.lab.iahomelab.source.entity.SourceStatus;
import fr.lab.iahomelab.source.repository.SourceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    @Transactional
    public SourceResponse create(CreateSourceRequest request) {

        if (isBlank(request.url()) && isBlank(request.storagePath())) {
            throw new InvalidRequestException(
                    "A source must define either url or storagePath"
            );
        }

        Source source = new Source();

        source.setTitle(request.title());
        source.setUrl(request.url());
        source.setStoragePath(request.storagePath());
        source.setFileName(request.fileName());
        source.setMimeType(request.mimeType());
        source.setType(request.type());
        source.setStatus(
                request.status() != null
                        ? request.status()
                        : SourceStatus.TO_READ
        );
        source.setSummary(request.summary());
        source.setNotes(request.notes());
        source.setTags(
                request.tags() != null
                        ? new HashSet<>(request.tags())
                        : new HashSet<>()
        );

        return toResponse(sourceRepository.save(source));
    }

    @Transactional(readOnly = true)
    public SourceResponse getById(UUID id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Source not found: " + id
                        )
                );

        return toResponse(source);
    }

    private SourceResponse toResponse(Source source) {
        return new SourceResponse(
                source.getId(),
                source.getTitle(),
                source.getUrl(),
                source.getStoragePath(),
                source.getFileName(),
                source.getMimeType(),
                source.getType(),
                source.getStatus(),
                source.getSummary(),
                source.getNotes(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getTags()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional
    public SourceResponse update(UUID id, UpdateSourceRequest request) {

        Source source = sourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Source not found: " + id
                        )
                );

        if (isBlank(request.url()) && isBlank(request.storagePath())) {
            throw new InvalidRequestException(
                    "A source must define either url or storagePath"
            );
        }

        source.setTitle(request.title());
        source.setUrl(request.url());
        source.setStoragePath(request.storagePath());
        source.setFileName(request.fileName());
        source.setMimeType(request.mimeType());
        source.setType(request.type());
        source.setStatus(request.status());
        source.setSummary(request.summary());
        source.setNotes(request.notes());

        source.getTags().clear();

        if (request.tags() != null) {
            source.getTags().addAll(request.tags());
        }

        return toResponse(sourceRepository.save(source));
    }
}