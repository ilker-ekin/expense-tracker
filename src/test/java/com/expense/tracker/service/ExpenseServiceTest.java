package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseRequest;
import com.expense.tracker.dto.ExpenseResponse;
import com.expense.tracker.entity.Expense;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock ExpenseRepository expenseRepository;
    @InjectMocks ExpenseService expenseService;

    private User testUser() {
        return User.builder().id(1L).email("user@test.com").password("h").fullName("U").role("USER").build();
    }

    private Expense testExpense() {
        return Expense.builder()
                .id(10L)
                .user(testUser())
                .amount(new BigDecimal("49.99"))
                .currency("TRY")
                .description("Lunch")
                .category("food")
                .type("expense")
                .date(LocalDate.of(2026, 5, 25))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllForUser_returnsMappedList() {
        when(expenseRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(List.of(testExpense()));

        List<ExpenseResponse> result = expenseService.getAllForUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Lunch");
        assertThat(result.get(0).amount()).isEqualByComparingTo(new BigDecimal("49.99"));
    }

    @Test
    void getById_existingExpense_returnsResponse() {
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testExpense()));

        ExpenseResponse result = expenseService.getById(10L, 1L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.category()).isEqualTo("food");
    }

    @Test
    void getById_notFound_throwsException() {
        when(expenseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getById(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expense not found");
    }

    @Test
    void create_savesAndReturnsResponse() {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("100.00"), "EUR", "Groceries", "food", "expense", LocalDate.of(2026, 5, 25));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense());

        ExpenseResponse result = expenseService.create(request, testUser());

        assertThat(result).isNotNull();
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void update_existingExpense_updatesFields() {
        Expense existing = testExpense();
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any())).thenReturn(existing);

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("75.00"), "USD", "Dinner", "food", "expense", LocalDate.of(2026, 5, 26));

        expenseService.update(10L, request, 1L);

        assertThat(existing.getAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(existing.getDescription()).isEqualTo("Dinner");
    }

    @Test
    void delete_existingExpense_deletesIt() {
        when(expenseRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testExpense()));

        expenseService.delete(10L, 1L);

        verify(expenseRepository).delete(any(Expense.class));
    }

    @Test
    void delete_notFound_throwsException() {
        when(expenseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.delete(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}