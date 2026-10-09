package LangBridge.UserService.service;

import LangBridge.UserService.domain.User;
import LangBridge.UserService.exception.InvalidCredentialsException;
import LangBridge.UserService.exception.UserAlreadyExistsException;
import LangBridge.UserService.repository.UserRepository;
import LangBridge.UserService.service.UserService.JwtIssuer;
import LangBridge.UserService.service.UserService.JwtToken;
import LangBridge.UserService.web.dto.AuthResponse;
import LangBridge.UserService.web.dto.LoginRequest;
import LangBridge.UserService.web.dto.RegisterRequest;
import LangBridge.UserService.web.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtIssuer jwtIssuer;

    UserService service;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode("fake-password")).thenReturn("fake-hash");
        service = new UserService(userRepository, passwordEncoder, jwtIssuer);
    }

    @Test
    void successfulRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest("User@Email.Com", "password123", "EN", "FR", (short)3);

        when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.saveAndFlush(captor.capture())).thenAnswer(inv -> {
            User u = captor.getValue();
            // simulate DB-assigned id
            setField(u, "id", UUID.randomUUID());
            setField(u, "createdAt", Instant.now());
            return u;
        });

        UserResponse resp = service.register(req);
        assertThat(resp.id()).isNotNull();
        assertThat(resp.email()).isEqualTo("user@email.com");
        assertThat(resp.level()).isEqualTo((short)3);
        assertThat(resp.nativeLang()).isEqualTo("EN");
        assertThat(resp.targetLang()).isEqualTo("FR");
    }

    @Test
    void duplicateEmailDifferentCaseCaughtByConstraint() {
        RegisterRequest req = new RegisterRequest("User@Email.Com", "password123", "EN", "FR", null);

        when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_users_lower_email"));

        assertThrows(UserAlreadyExistsException.class, () -> service.register(req));
    }

    @Test
    void loginWithCorrectPassword() {
        UUID id = UUID.randomUUID();
        User user = new User();
        setField(user, "id", id);
        user.setEmail("user@email.com");
        user.setPasswordHash("hashed");
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtIssuer.issueToken(id, "user@email.com")).thenReturn(new JwtToken("token", 900, Instant.now().plusSeconds(900)));

        AuthResponse resp = service.login(new LoginRequest("User@Email.Com", "password123"));
        assertThat(resp.accessToken()).isEqualTo("token");
        assertThat(resp.tokenType()).isEqualTo("Bearer");
        assertThat(resp.expiresIn()).isEqualTo(900);
    }

    @Test
    void loginWithWrongPassword() {
        User user = new User();
        setField(user, "id", UUID.randomUUID());
        user.setEmail("user@email.com");
        user.setPasswordHash("hashed");
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.login(new LoginRequest("user@email.com", "wrong")));
    }

    @Test
    void loginWithNonexistentEmail() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(eq("password123"), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.login(new LoginRequest("user@email.com", "password123")));
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
