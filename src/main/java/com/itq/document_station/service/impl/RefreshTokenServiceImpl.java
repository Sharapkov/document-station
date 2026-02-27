package com.itq.document_station.service.impl;

import com.itq.document_station.exception.EntityNotFoundException;
import com.itq.document_station.exception.TokenRefreshException;
import com.itq.document_station.model.Token;
import com.itq.document_station.model.User;
import com.itq.document_station.repository.TokenRepository;
import com.itq.document_station.repository.UserRepository;
import com.itq.document_station.security.JwtUtils;
import com.itq.document_station.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Token createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        tokenRepository.findByUserId(user.getId()).ifPresent(tokenRepository::delete);

        String refreshTokenString = jwtUtils.generateRefreshToken(user.getUsername());

        Token refreshToken = new Token(refreshTokenString, user);
        return tokenRepository.save(refreshToken);
    }

    @Override
    public Token verifyExpiration(Token token) {
        if (jwtUtils.validateToken(token.getRefreshToken())) {
            return token;
        } else {
            tokenRepository.delete(token);
            throw new TokenRefreshException(token.getRefreshToken(), "Refresh token was expired. Please make a new signin request");
        }
    }

    @Override
    @Transactional
    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByRefreshToken(token);
    }

    @Override
    @Transactional
    public int deleteByUserId(Long userId) {
        return tokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
