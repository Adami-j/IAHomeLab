package fr.lab.iahomelab.source.controller.dto;

import fr.lab.iahomelab.source.entity.SourceStatus;
import fr.lab.iahomelab.source.entity.SourceType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record SourceResponse(
        UUID id,
        String title,
        String url,
        String storagePath,
        String fileName,
        String mimeType,
        SourceType type,
        SourceStatus status,
        String summary,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Set<String> tags
) {
}