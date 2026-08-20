package fr.lab.iahomelab.setup.controller.dto;

import fr.lab.iahomelab.setup.entity.ComponentType;

import java.util.Map;
import java.util.UUID;

public record ComponentInstanceResponse(
        UUID id,
        UUID setupVersionId,
        String name,
        ComponentType type,
        Map<String, Object> configuration
) {
}
