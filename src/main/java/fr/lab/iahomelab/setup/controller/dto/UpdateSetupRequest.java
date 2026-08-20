package fr.lab.iahomelab.setup.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateSetupRequest (

    @NotBlank
    UUID id,
    @Size(max = 200) @NotBlank
     String name,

    @Size(max = 500)
     String description
){}
