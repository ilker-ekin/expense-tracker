package com.expense.tracker.dto;

public record AuthResponse(
        String email,
        String fullName
) {}
