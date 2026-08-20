package fr.lab.iahomelab.setup.controller.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SetupResponse (

    UUID id,

     String name,

     String description
){}
