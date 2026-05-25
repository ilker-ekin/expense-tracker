package com.expense.tracker.service;

import com.expense.tracker.dto.RecurringRequest;
import com.expense.tracker.dto.RecurringResponse;
import com.expense.tracker.entity.RecurringTransaction;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.RecurringTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public List<RecurringResponse> getAllForUser(Long userId) {
        return repository.findByUserIdOrderByNextDateAsc(userId)
                .stream()
                .map(RecurringResponse::from)
                .toList();
    }

    public RecurringResponse getById(Long id, Long userId) {
        RecurringTransaction rt = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
        return RecurringResponse.from(rt);
    }

    @Transactional
    public RecurringResponse create(RecurringRequest request, User user) {
        RecurringTransaction rt = RecurringTransaction.builder()
                .user(user)
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description())
                .category(request.category())
                .type(request.type())
                .frequency(request.frequency())
                .nextDate(request.nextDate())
                .active(request.active())
                .build();
        return RecurringResponse.from(repository.save(rt));
    }

    @Transactional
    public RecurringResponse update(Long id, RecurringRequest request, Long userId) {
        RecurringTransaction rt = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
        rt.setAmount(request.amount());
        rt.setCurrency(request.currency());
        rt.setDescription(request.description());
        rt.setCategory(request.category());
        rt.setType(request.type());
        rt.setFrequency(request.frequency());
        rt.setNextDate(request.nextDate());
        rt.setActive(request.active());
        return RecurringResponse.from(repository.save(rt));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        RecurringTransaction rt = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
        repository.delete(rt);
    }
}
