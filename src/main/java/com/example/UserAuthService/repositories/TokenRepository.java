package com.example.UserAuthService.repositories;

import com.example.UserAuthService.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    Token save(Token token);
    Optional<Token> findByTokenValueAndDeletedAndExpiryAtAfter(String tokenValue, boolean deleted, java.util.Date currentDate);
}
