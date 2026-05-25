package com.expense.tracker.controller;

import com.expense.tracker.config.SecurityConfig;
import com.expense.tracker.dto.ExpenseRequest;
import com.expense.tracker.dto.ExpenseResponse;
import com.expense.tracker.entity.User;
import com.expense.tracker.service.ExpenseService;
import com.expense.tracker.service.JwtService;
import com.expense.tracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import(SecurityConfig.class)
class ExpenseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ExpenseService expenseService;
    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    private User stubUser() {
        return User.builder()
                .id(1L).email("user@test.com").password("h").fullName("Test").role("USER").emailVerified(true)
                .build();
    }

    private ExpenseResponse stubResponse() {
        return new ExpenseResponse(1L, new BigDecimal("50.00"), "TRY", "Lunch", "food", "expense", LocalDate.of(2026, 5, 25));
    }

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void list_returns200WithExpenses() throws Exception {
        User user = stubUser();
        authenticateAs(user);
        when(expenseService.getAllForUser(1L)).thenReturn(List.of(stubResponse()));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Lunch"))
                .andExpect(jsonPath("$[0].amount").value(50.00));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        User user = stubUser();
        authenticateAs(user);
        when(expenseService.create(any(), any())).thenReturn(stubResponse());

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("50.00"), "TRY", "Lunch", "food", "expense", LocalDate.of(2026, 5, 25));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    void create_invalidType_returns400() throws Exception {
        authenticateAs(stubUser());

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("50.00"), "TRY", "Lunch", "food", "invalid", LocalDate.of(2026, 5, 25));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_negativeAmount_returns400() throws Exception {
        authenticateAs(stubUser());

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("-5.00"), "TRY", "Lunch", "food", "expense", LocalDate.of(2026, 5, 25));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_unauthenticated_returns403() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_returns204() throws Exception {
        authenticateAs(stubUser());
        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
    }
}
