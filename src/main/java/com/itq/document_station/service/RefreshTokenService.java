package com.itq.document_station.service;

import com.itq.document_station.model.Token;

import java.util.Optional;

public interface RefreshTokenService {
    Token createRefreshToken(Long userId);
    Token verifyExpiration(Token token);
    Optional<Token> findByToken(String token);
    int deleteByUserId(Long userId);
}
