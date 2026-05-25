package com.expense.tracker.controller;

import com.expense.tracker.dto.ExpenseRequest;
import com.expense.tracker.dto.ExpenseResponse;
import com.expense.tracker.entity.User;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @Operation(summary = "List all expenses for the authenticated user")
    public List<ExpenseResponse> list(@AuthenticationPrincipal User user) {
        return expenseService.getAllForUser(user.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single expense by ID")
    public ExpenseResponse get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return expenseService.getById(id, user.getId());
    }

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.create(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense")
    public ExpenseResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal User user
    ) {
        return expenseService.update(id, request, user.getId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        expenseService.delete(id, user.getId());
    }
}
