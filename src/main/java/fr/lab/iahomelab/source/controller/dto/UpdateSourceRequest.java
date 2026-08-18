package fr.lab.iahomelab.source.controller.dto;



import fr.lab.iahomelab.source.entity.SourceStatus;
import fr.lab.iahomelab.source.entity.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSourceRequest(

        @NotBlank
        @Size(max = 500)
        String title,

        @Size(max = 2048)
        String url,

        @Size(max = 2048)
        String storagePath,

        @Size(max = 500)
        String fileName,

        @Size(max = 255)
        String mimeType,

        @NotNull
        SourceType type,

        SourceStatus status,

        String summary,

        String notes
) {
}