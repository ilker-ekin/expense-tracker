package com.expense.tracker.controller;

import com.expense.tracker.dto.ExchangeRateResponse;
import com.expense.tracker.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
@Tag(name = "Exchange Rates")
public class ExchangeRateController {

    private static final Set<String> SUPPORTED = Set.of("TRY", "EUR", "USD");

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    @Operation(summary = "Get exchange rates for a base currency")
    public ResponseEntity<ExchangeRateResponse> getRates(
            @RequestParam(defaultValue = "TRY") String from) {
        String normalized = from.toUpperCase();
        if (!SUPPORTED.contains(normalized)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(exchangeRateService.getRates(normalized));
    }
}
