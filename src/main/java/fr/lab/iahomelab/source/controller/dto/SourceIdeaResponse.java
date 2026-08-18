package fr.lab.iahomelab.source.controller.dto;

import fr.lab.iahomelab.source.entity.SourceIdeaType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SourceIdeaResponse(
        UUID id,
        UUID sourceId,
        String title,
        String content,
        SourceIdeaType type,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
