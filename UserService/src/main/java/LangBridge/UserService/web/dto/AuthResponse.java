package LangBridge.UserService.web.dto;

/**
 * Auth response containing the issued access token.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
