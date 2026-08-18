package fr.lab.iahomelab.security.controller.dto;

import java.util.List;

public record CurrentUserResponse(
        String username,
        List<String> roles
) {
}