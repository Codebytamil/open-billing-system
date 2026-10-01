package com.billing.billing_system.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public record CreateUserRequest(
            @NotBlank(message = "username is required")
            @Size(min = 3, max = 50, message = "username must be 3 to 50 characters")
            @Pattern(regexp = "[A-Za-z0-9_.]*",
                     message = "username can only have letters, numbers, dot and underscore")
            String username,

            @NotBlank(message = "password is required")
            @Size(min = 8, max = 100, message = "password must be at least 8 characters")
            String password
    ) {
    }

    public record UserResponse(Long id, String username, String role) {
    }

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createCashier(@Valid @RequestBody CreateUserRequest request) {
        AppUser user = service.createCashier(request.username(), request.password());
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}