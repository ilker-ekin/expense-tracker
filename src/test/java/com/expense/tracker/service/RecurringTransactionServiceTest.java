package com.expense.tracker.service;

import com.expense.tracker.dto.RecurringRequest;
import com.expense.tracker.dto.RecurringResponse;
import com.expense.tracker.entity.RecurringTransaction;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.RecurringTransactionRepository;
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
class RecurringTransactionServiceTest {

    @Mock RecurringTransactionRepository repository;
    @InjectMocks RecurringTransactionService service;

    private User testUser() {
        return User.builder().id(1L).email("user@test.com").password("h").fullName("U").role("USER").build();
    }

    private RecurringTransaction testRecurring() {
        return RecurringTransaction.builder()
                .id(10L)
                .user(testUser())
                .amount(new BigDecimal("29.99"))
                .currency("TRY")
                .description("Netflix")
                .category("entertain")
                .type("expense")
                .frequency("monthly")
                .nextDate(LocalDate.of(2026, 6, 1))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RecurringRequest testRequest() {
        return new RecurringRequest(
                new BigDecimal("29.99"), "TRY", "Netflix", "entertain",
                "expense", "monthly", LocalDate.of(2026, 6, 1), true);
    }

    @Test
    void getAllForUser_returnsMappedList() {
        when(repository.findByUserIdOrderByNextDateAsc(1L)).thenReturn(List.of(testRecurring()));

        List<RecurringResponse> result = service.getAllForUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Netflix");
        assertThat(result.get(0).frequency()).isEqualTo("monthly");
    }

    @Test
    void getById_existing_returnsResponse() {
        when(repository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testRecurring()));

        RecurringResponse result = service.getById(10L, 1L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.active()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(repository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recurring transaction not found");
    }

    @Test
    void create_savesAndReturns() {
        when(repository.save(any(RecurringTransaction.class))).thenReturn(testRecurring());

        RecurringResponse result = service.create(testRequest(), testUser());

        assertThat(result).isNotNull();
        verify(repository).save(any(RecurringTransaction.class));
    }

    @Test
    void update_existingItem_updatesFields() {
        RecurringTransaction existing = testRecurring();
        when(repository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        RecurringRequest request = new RecurringRequest(
                new BigDecimal("14.99"), "USD", "Spotify", "entertain",
                "expense", "monthly", LocalDate.of(2026, 6, 15), true);

        service.update(10L, request, 1L);

        assertThat(existing.getAmount()).isEqualByComparingTo(new BigDecimal("14.99"));
        assertThat(existing.getDescription()).isEqualTo("Spotify");
    }

    @Test
    void delete_existingItem_deletesIt() {
        when(repository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testRecurring()));

        service.delete(10L, 1L);

        verify(repository).delete(any(RecurringTransaction.class));
    }

    @Test
    void delete_notFound_throws() {
        when(repository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
