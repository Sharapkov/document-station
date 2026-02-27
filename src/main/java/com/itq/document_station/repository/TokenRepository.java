package com.itq.document_station.repository;

import com.itq.document_station.model.Token;
import com.itq.document_station.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByUserId(Long userId);
    Optional<Token> findByRefreshToken(String token);
    int deleteByUser(User user);
}
