package com.expense.tracker.repository;

import com.expense.tracker.AbstractPostgresIT;
import com.expense.tracker.entity.AuthToken;
import com.expense.tracker.entity.AuthToken.TokenType;
import com.expense.tracker.entity.Expense;
import com.expense.tracker.entity.RecurringTransaction;
import com.expense.tracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The expense list feeds the dashboard/report views and the recurring list feeds the recurring
 * page, so these are the queries whose ordering and user scoping matter on the real database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryIT extends AbstractPostgresIT {

    @Autowired TestEntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired ExpenseRepository expenseRepository;
    @Autowired RecurringTransactionRepository recurringRepository;
    @Autowired AuthTokenRepository authTokenRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = em.persist(user("alice@example.com"));
        bob = em.persist(user("bob@example.com"));
    }

    @Test
    void expenses_areScopedToUserAndOrderedByDateDesc() {
        Expense older = em.persist(expense(alice, "10.00", LocalDate.of(2026, 1, 5)));
        Expense newest = em.persist(expense(alice, "20.00", LocalDate.of(2026, 3, 1)));
        Expense middle = em.persist(expense(alice, "30.00", LocalDate.of(2026, 2, 14)));
        em.persist(expense(bob, "99.00", LocalDate.of(2026, 4, 1)));
        em.flush();
        em.clear();

        assertThat(expenseRepository.findByUserIdOrderByDateDesc(alice.getId()))
                .extracting(Expense::getId)
                .containsExactly(newest.getId(), middle.getId(), older.getId());
    }

    @Test
    void expenses_preserveDecimalPrecisionAndDefaults() {
        Expense saved = em.persistAndFlush(expense(alice, "1234567890.99", LocalDate.of(2026, 1, 1)));
        em.clear();

        Expense loaded = expenseRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getAmount()).isEqualByComparingTo("1234567890.99");
        assertThat(loaded.getCurrency()).isEqualTo("TRY");
        assertThat(loaded.getType()).isEqualTo("expense");
    }

    @Test
    void expenseById_isNotVisibleToOtherUsers() {
        Expense expense = em.persistAndFlush(expense(alice, "10.00", LocalDate.of(2026, 1, 1)));

        assertThat(expenseRepository.findByIdAndUserId(expense.getId(), alice.getId())).isPresent();
        assertThat(expenseRepository.findByIdAndUserId(expense.getId(), bob.getId())).isEmpty();
    }

    @Test
    void recurring_areScopedToUserAndOrderedByNextDateAsc_includingInactive() {
        RecurringTransaction later = em.persist(recurring(alice, LocalDate.of(2026, 12, 1), true));
        RecurringTransaction soonest = em.persist(recurring(alice, LocalDate.of(2026, 10, 1), false));
        RecurringTransaction middle = em.persist(recurring(alice, LocalDate.of(2026, 11, 1), true));
        em.persist(recurring(bob, LocalDate.of(2026, 9, 1), true));
        em.flush();
        em.clear();

        assertThat(recurringRepository.findByUserIdOrderByNextDateAsc(alice.getId()))
                .extracting(RecurringTransaction::getId)
                .containsExactly(soonest.getId(), middle.getId(), later.getId());
    }

    @Test
    void recurringById_isNotVisibleToOtherUsers() {
        RecurringTransaction rt = em.persistAndFlush(recurring(alice, LocalDate.of(2026, 10, 1), true));

        assertThat(recurringRepository.findByIdAndUserId(rt.getId(), alice.getId())).isPresent();
        assertThat(recurringRepository.findByIdAndUserId(rt.getId(), bob.getId())).isEmpty();
    }

    @Test
    void users_lookupByEmail() {
        assertThat(userRepository.findByEmail("alice@example.com")).contains(alice);
        assertThat(userRepository.existsByEmail("bob@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void authTokens_lookupByTokenAndType() {
        em.persistAndFlush(AuthToken.builder()
                .user(alice).token("abc").type(TokenType.VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(1)).build());

        assertThat(authTokenRepository.findByTokenAndType("abc", TokenType.VERIFICATION)).isPresent();
        assertThat(authTokenRepository.findByTokenAndType("abc", TokenType.PASSWORD_RESET)).isEmpty();
    }

    private static User user(String email) {
        return User.builder().email(email).password("hash").fullName("Test").role("USER").build();
    }

    private static Expense expense(User user, String amount, LocalDate date) {
        return Expense.builder()
                .user(user).amount(new BigDecimal(amount)).description("item")
                .category("Food").date(date).build();
    }

    private static RecurringTransaction recurring(User user, LocalDate nextDate, boolean active) {
        return RecurringTransaction.builder()
                .user(user).amount(new BigDecimal("50.00")).description("sub")
                .category("Bills").nextDate(nextDate).active(active).build();
    }
}
