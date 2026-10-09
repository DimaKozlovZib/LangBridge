package LangBridge.UserService.web;

import LangBridge.UserService.exception.InvalidCredentialsException;
import LangBridge.UserService.exception.UserAlreadyExistsException;
import LangBridge.UserService.service.UserService;
import LangBridge.UserService.web.dto.AuthResponse;
import LangBridge.UserService.web.dto.RegisterRequest;
import LangBridge.UserService.web.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class, GlobalExceptionHandler.class})
class UserControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    void registerValidationErrors() throws Exception {
        String body = "{\n" +
                "  \"email\": \"invalid\",\n" +
                "  \"password\": \"short\",\n" +
                "  \"nativeLang\": \"EN\",\n" +
                "  \"targetLang\": \"EN\"\n" +
                "}";
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    @Test
    void registerDuplicateEmail() throws Exception {
        Mockito.when(userService.register(Mockito.any())).thenThrow(new UserAlreadyExistsException("user@email.com"));
        String body = "{\n" +
                "  \"email\": \"user@email.com\",\n" +
                "  \"password\": \"password123\",\n" +
                "  \"nativeLang\": \"EN\",\n" +
                "  \"targetLang\": \"FR\"\n" +
                "}";
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        Mockito.when(userService.login(Mockito.any())).thenThrow(new InvalidCredentialsException());
        String body = "{\n" +
                "  \"email\": \"user@email.com\",\n" +
                "  \"password\": \"wrong\"\n" +
                "}";
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void profileForbiddenForOtherUser() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(userService.getProfile(id)).thenReturn(new UserResponse(id, "user@email.com", "EN", "FR", (short)1, Instant.now()));
        mockMvc.perform(get("/users/" + id + "/profile")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
                )
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/problem+json"));
    }
}
