package com.expense.tracker.controller;

import com.expense.tracker.config.SecurityConfig;
import com.expense.tracker.dto.RecurringRequest;
import com.expense.tracker.dto.RecurringResponse;
import com.expense.tracker.entity.User;
import com.expense.tracker.exception.GlobalExceptionHandler;
import com.expense.tracker.service.JwtService;
import com.expense.tracker.service.RecurringTransactionService;
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

@WebMvcTest(RecurringController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RecurringControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean RecurringTransactionService recurringService;
    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    private User stubUser() {
        return User.builder()
                .id(1L).email("user@test.com").password("h").fullName("Test").role("USER").emailVerified(true)
                .build();
    }

    private RecurringResponse stubResponse() {
        return new RecurringResponse(1L, new BigDecimal("29.99"), "TRY", "Netflix",
                "entertain", "expense", "monthly", LocalDate.of(2026, 6, 1), true);
    }

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void list_returns200() throws Exception {
        authenticateAs(stubUser());
        when(recurringService.getAllForUser(1L)).thenReturn(List.of(stubResponse()));

        mockMvc.perform(get("/api/recurring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Netflix"))
                .andExpect(jsonPath("$[0].frequency").value("monthly"));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        authenticateAs(stubUser());
        when(recurringService.create(any(), any())).thenReturn(stubResponse());

        RecurringRequest request = new RecurringRequest(
                new BigDecimal("29.99"), "TRY", "Netflix", "entertain",
                "expense", "monthly", LocalDate.of(2026, 6, 1), true);

        mockMvc.perform(post("/api/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Netflix"));
    }

    @Test
    void create_invalidFrequency_returns400() throws Exception {
        authenticateAs(stubUser());

        RecurringRequest request = new RecurringRequest(
                new BigDecimal("29.99"), "TRY", "Netflix", "entertain",
                "expense", "biweekly", LocalDate.of(2026, 6, 1), true);

        mockMvc.perform(post("/api/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_negativeAmount_returns400() throws Exception {
        authenticateAs(stubUser());

        RecurringRequest request = new RecurringRequest(
                new BigDecimal("-5.00"), "TRY", "Netflix", "entertain",
                "expense", "monthly", LocalDate.of(2026, 6, 1), true);

        mockMvc.perform(post("/api/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_unauthenticated_returns403() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/recurring"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_returns204() throws Exception {
        authenticateAs(stubUser());
        mockMvc.perform(delete("/api/recurring/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_validRequest_returns200() throws Exception {
        authenticateAs(stubUser());
        when(recurringService.update(eq(1L), any(), eq(1L))).thenReturn(stubResponse());

        RecurringRequest request = new RecurringRequest(
                new BigDecimal("14.99"), "TRY", "Spotify", "entertain",
                "expense", "monthly", LocalDate.of(2026, 6, 15), true);

        mockMvc.perform(put("/api/recurring/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
