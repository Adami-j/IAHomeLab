package fr.lab.iahomelab.setup.controller.dto;

import jakarta.validation.constraints.Size;

public record CreateSetupVersionRequest(
        @Size(max = 500) String description
) {
}
