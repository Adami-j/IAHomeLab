package fr.lab.iahomelab.setup.controller.dto;

import fr.lab.iahomelab.setup.entity.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateComponentInstanceRequest(
        @NotBlank
        @Size(max = 200)
        String name,

        @NotNull
        ComponentType type,

        Map<String, Object> configuration
) {
}
