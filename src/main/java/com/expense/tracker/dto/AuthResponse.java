package com.expense.tracker.dto;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        long expiresInMs
) {}