package LangBridge.UserService.exception;

import java.util.UUID;

/**
 * Thrown when user cannot be found.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }
}
