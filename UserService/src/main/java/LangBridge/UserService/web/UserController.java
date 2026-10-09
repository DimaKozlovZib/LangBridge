package LangBridge.UserService.web;

import LangBridge.UserService.service.UserService;
import LangBridge.UserService.web.dto.AuthResponse;
import LangBridge.UserService.web.dto.LoginRequest;
import LangBridge.UserService.web.dto.RegisterRequest;
import LangBridge.UserService.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("#id.toString() == authentication.name")
    public UserResponse profile(@PathVariable UUID id) {
        return userService.getProfile(id);
    }
}
