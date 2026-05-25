package com.expense.tracker.controller;

import com.expense.tracker.config.SecurityConfig;
import com.expense.tracker.dto.ExchangeRateResponse;
import com.expense.tracker.service.ExchangeRateService;
import com.expense.tracker.service.JwtService;
import com.expense.tracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
@Import(SecurityConfig.class)
class ExchangeRateControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ExchangeRateService exchangeRateService;
    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    @Test
    @WithMockUser
    void getRates_validFrom_returns200() throws Exception {
        when(exchangeRateService.getRates("TRY")).thenReturn(
                new ExchangeRateResponse("TRY", "2026-05-25",
                        Map.of("EUR", new BigDecimal("0.026"), "USD", new BigDecimal("0.029"))));

        mockMvc.perform(get("/api/rates").param("from", "TRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("TRY"))
                .andExpect(jsonPath("$.rates.EUR").value(0.026))
                .andExpect(jsonPath("$.rates.USD").value(0.029));
    }

    @Test
    @WithMockUser
    void getRates_defaultFrom_usesTRY() throws Exception {
        when(exchangeRateService.getRates("TRY")).thenReturn(
                new ExchangeRateResponse("TRY", "2026-05-25", Map.of()));

        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("TRY"));
    }

    @Test
    @WithMockUser
    void getRates_invalidCurrency_returns400() throws Exception {
        mockMvc.perform(get("/api/rates").param("from", "GBP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getRates_lowercaseInput_normalizedToUppercase() throws Exception {
        when(exchangeRateService.getRates("EUR")).thenReturn(
                new ExchangeRateResponse("EUR", "2026-05-25", Map.of()));

        mockMvc.perform(get("/api/rates").param("from", "eur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("EUR"));
    }

    @Test
    void getRates_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/rates").param("from", "TRY"))
                .andExpect(status().isForbidden());
    }
}
