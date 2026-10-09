package LangBridge.UserService.service;

import LangBridge.UserService.domain.User;
import LangBridge.UserService.exception.InvalidCredentialsException;
import LangBridge.UserService.exception.UserAlreadyExistsException;
import LangBridge.UserService.exception.UserNotFoundException;
import LangBridge.UserService.repository.UserRepository;
import LangBridge.UserService.web.dto.AuthResponse;
import LangBridge.UserService.web.dto.LoginRequest;
import LangBridge.UserService.web.dto.RegisterRequest;
import LangBridge.UserService.web.dto.UserResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    // Precomputed fake hash to mitigate timing attacks when user is not found
    private final String fakeHash;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtIssuer jwtIssuer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtIssuer = jwtIssuer;
        this.fakeHash = passwordEncoder.encode("fake-password");
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new UserAlreadyExistsException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNativeLang(request.nativeLang().trim());
        user.setTargetLang(request.targetLang().trim());
        short level = Optional.ofNullable(request.level()).orElse((short)1);
        user.setLevel(level);

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // In case of race condition or DB unique constraint trigger
            throw new UserAlreadyExistsException(email);
        }

        // TODO: outbox write: user.registered within this transaction

        return toUserResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Optional<User> userOpt = userRepository.findByEmail(email);

        boolean passwordMatches;
        if (userOpt.isPresent()) {
            passwordMatches = passwordEncoder.matches(request.password(), userOpt.get().getPasswordHash());
        } else {
            // Run matches against fake hash to avoid timing attacks
            passwordMatches = passwordEncoder.matches(request.password(), fakeHash);
        }

        if (userOpt.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        User user = userOpt.get();
        JwtToken token = jwtIssuer.issueToken(user.getId(), user.getEmail());
        return new AuthResponse(token.value(), "Bearer", token.expiresInSeconds());
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return toUserResponse(user);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNativeLang(),
                user.getTargetLang(),
                user.getLevel(),
                user.getCreatedAt()
        );
    }

    /** JWT issuing abstraction to decouple service from Nimbus APIs */
    public interface JwtIssuer {
        JwtToken issueToken(UUID userId, String email);
    }

    /** JWT token value + TTL info */
    public record JwtToken(String value, long expiresInSeconds, Instant expiresAt) {}
}
