package com.expense.tracker;

import com.expense.tracker.service.EmailService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End-to-end through the real security chain, services and PostgreSQL; only email delivery is mocked. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthExpenseFlowIT extends AbstractPostgresIT {

    private static final String EMAIL = "flow@example.com";
    private static final String CREDENTIALS = """
            {"email":"%s","password":"password123"}""".formatted(EMAIL);

    @Autowired MockMvc mockMvc;
    @MockitoBean EmailService emailService;

    @Test
    void register_verify_login_createAndListExpense() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123","fullName":"Flow User"}""".formatted(EMAIL)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq(EMAIL), token.capture());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/verify").param("token", token.getValue()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:8080/?verified=true"));

        Cookie authCookie = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andReturn().getResponse().getCookie("vault_token");
        assertThat(authCookie).isNotNull();
        assertThat(authCookie.isHttpOnly()).isTrue();

        mockMvc.perform(post("/api/expenses")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":42.50,"currency":"EUR","description":"Groceries",
                                 "category":"Food","type":"expense","date":"2026-09-01"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/api/expenses").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Groceries"))
                .andExpect(jsonPath("$[0].amount").value(42.50))
                .andExpect(jsonPath("$[0].currency").value("EUR"));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().is4xxClientError());
    }
}
