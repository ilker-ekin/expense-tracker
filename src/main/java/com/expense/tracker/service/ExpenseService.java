package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseRequest;
import com.expense.tracker.dto.ExpenseResponse;
import com.expense.tracker.entity.Expense;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public List<ExpenseResponse> getAllForUser(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    public ExpenseResponse getById(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request, User user) {
        Expense expense = Expense.builder()
                .user(user)
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description())
                .category(request.category())
                .type(request.type())
                .date(request.date())
                .build();
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency());
        expense.setDescription(request.description());
        expense.setCategory(request.category());
        expense.setType(request.type());
        expense.setDate(request.date());
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        expenseRepository.delete(expense);
    }
}
