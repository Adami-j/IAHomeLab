package fr.lab.iahomelab.source.controller.dto;

import fr.lab.iahomelab.source.entity.SourceIdeaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSourceIdeaRequest(
        @NotBlank @Size(max = 300) String title,
        @NotBlank String content,
        @NotNull SourceIdeaType type
) {
}
