package com.expense.tracker.dto;

import com.expense.tracker.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String currency,
        String description,
        String category,
        String type,
        LocalDate date
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCurrency(),
                expense.getDescription(),
                expense.getCategory(),
                expense.getType(),
                expense.getDate()
        );
    }
}
