package LangBridge.UserService.web.dto;


import java.time.Instant;
import java.util.UUID;

/**
 * User profile response DTO.
 */
public record UserResponse(
        UUID id,
        String email,
        String nativeLang,
        String targetLang,
        short level,
        Instant createdAt
) {}
