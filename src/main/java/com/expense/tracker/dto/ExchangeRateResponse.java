package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponse(
        String base,
        String date,
        Map<String, BigDecimal> rates
) {}
