package fr.lab.iahomelab.setup.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateConnectionRequest(
        @NotNull UUID sourceComponentId,
        @NotNull UUID targetComponentId,
        @Size(max = 200) String name
) {
}
