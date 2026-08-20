package fr.lab.iahomelab.setup.controller.dto;

import fr.lab.iahomelab.setup.entity.SetupVersionStatus;

import java.util.UUID;

public record SetupVersionResponse(
        UUID id,
        UUID setupId,
        Integer versionNumber,
        SetupVersionStatus status,
        String description
) {
}
