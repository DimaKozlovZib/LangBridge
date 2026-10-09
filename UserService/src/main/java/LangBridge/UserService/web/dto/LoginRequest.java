package LangBridge.UserService.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTO.
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {}
