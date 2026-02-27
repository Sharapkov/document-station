package com.itq.document_station.controller;

import com.itq.document_station.dto.auth.JwtResponse;
import com.itq.document_station.dto.auth.LoginRequest;
import com.itq.document_station.dto.MessageResponse;
import com.itq.document_station.dto.auth.RefreshTokenRequest;
import com.itq.document_station.exception.EntityNotFoundException;
import com.itq.document_station.exception.TokenRefreshException;
import com.itq.document_station.model.Token;
import com.itq.document_station.model.User;
import com.itq.document_station.repository.UserRepository;
import com.itq.document_station.security.JwtUtils;

import com.itq.document_station.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        String accessToken = jwtUtils.generateAccessToken(user.getUsername());
        Token refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getRefreshToken(), user.getId(), user.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(Token::getUser)
                .map(user -> {
                    String accessToken = jwtUtils.generateAccessToken(user.getUsername());
                    return ResponseEntity.ok(new JwtResponse(accessToken, requestRefreshToken, user.getId(), user.getUsername()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Токен не найден в бд"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        refreshTokenService.deleteByUserId(user.getId());
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK.value(), "Выход из системы"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody LoginRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse(HttpStatus.OK.value(),"Имя пользователя уже занято"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK.value(),"Пользователь успешно зарегистрирован"));
    }

}
