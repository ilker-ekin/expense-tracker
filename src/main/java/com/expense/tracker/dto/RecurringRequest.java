package com.expense.tracker.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank @Size(max = 3)
        String currency,

        @NotBlank @Size(max = 255)
        String description,

        @NotBlank @Size(max = 50)
        String category,

        @NotBlank @Pattern(regexp = "^(expense|income)$", message = "Type must be 'expense' or 'income'")
        String type,

        @NotBlank @Pattern(regexp = "^(daily|weekly|monthly|yearly)$", message = "Frequency must be daily, weekly, monthly, or yearly")
        String frequency,

        @NotNull
        LocalDate nextDate,

        @NotNull
        Boolean active
) {}
