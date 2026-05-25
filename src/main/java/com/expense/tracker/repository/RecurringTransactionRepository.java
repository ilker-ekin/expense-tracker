package com.expense.tracker.repository;

import com.expense.tracker.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserIdOrderByNextDateAsc(Long userId);

    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);
}
