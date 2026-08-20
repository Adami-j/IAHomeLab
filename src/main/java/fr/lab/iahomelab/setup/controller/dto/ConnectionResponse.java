package fr.lab.iahomelab.setup.controller.dto;

import java.util.UUID;

public record ConnectionResponse(
        UUID id,
        UUID setupVersionId,
        UUID sourceComponentId,
        UUID targetComponentId,
        String name
) {
}
