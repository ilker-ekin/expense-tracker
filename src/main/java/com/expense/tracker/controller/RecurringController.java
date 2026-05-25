package com.expense.tracker.controller;

import com.expense.tracker.dto.RecurringRequest;
import com.expense.tracker.dto.RecurringResponse;
import com.expense.tracker.entity.User;
import com.expense.tracker.service.RecurringTransactionService;
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
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@Tag(name = "Recurring Transactions")
public class RecurringController {

    private final RecurringTransactionService recurringService;

    @GetMapping
    @Operation(summary = "List all recurring transactions for the authenticated user")
    public List<RecurringResponse> list(@AuthenticationPrincipal User user) {
        return recurringService.getAllForUser(user.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single recurring transaction by ID")
    public RecurringResponse get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return recurringService.getById(id, user.getId());
    }

    @PostMapping
    @Operation(summary = "Create a new recurring transaction")
    public ResponseEntity<RecurringResponse> create(
            @Valid @RequestBody RecurringRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurringService.create(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing recurring transaction")
    public RecurringResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RecurringRequest request,
            @AuthenticationPrincipal User user
    ) {
        return recurringService.update(id, request, user.getId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recurring transaction")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        recurringService.delete(id, user.getId());
    }
}
