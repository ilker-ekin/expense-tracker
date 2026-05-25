package com.expense.tracker.repository;

import com.expense.tracker.entity.AuthToken;
import com.expense.tracker.entity.AuthToken.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenAndType(String token, TokenType type);
}
