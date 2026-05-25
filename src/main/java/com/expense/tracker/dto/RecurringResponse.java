package com.expense.tracker.dto;

import com.expense.tracker.entity.RecurringTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringResponse(
        Long id,
        BigDecimal amount,
        String currency,
        String description,
        String category,
        String type,
        String frequency,
        LocalDate nextDate,
        boolean active
) {
    public static RecurringResponse from(RecurringTransaction rt) {
        return new RecurringResponse(
                rt.getId(),
                rt.getAmount(),
                rt.getCurrency(),
                rt.getDescription(),
                rt.getCategory(),
                rt.getType(),
                rt.getFrequency(),
                rt.getNextDate(),
                rt.isActive()
        );
    }
}
