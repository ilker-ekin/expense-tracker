package com.expense.tracker.controller;

import com.expense.tracker.dto.AuthResponse;
import com.expense.tracker.dto.LoginRequest;
import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.dto.UserProfileResponse;
import com.expense.tracker.entity.User;
import com.expense.tracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public UserProfileResponse me(@AuthenticationPrincipal User user) {
        return new UserProfileResponse(user.getEmail(), user.getFullName());
    }
}