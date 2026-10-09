package LangBridge.UserService.exception;

/**
 * Thrown when credentials are invalid (email or password mismatch).
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
