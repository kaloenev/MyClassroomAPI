package com.alibou.security.auth;

import com.alibou.security.config.JwtService;
import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.token.Token;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.token.TokenType;
import com.alibou.security.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;

    private final StudentRepository studentRepository;

    private final TeacherRepository teacherRepository;

    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private final String resetUrl = "/api/v1/auth/resetPassword/validateToken/";

    public AuthenticationResponse register(RegisterRequest request) throws CustomException {
        String jwtToken;
        String refreshToken;
        if (!(request.getRole().equals(Role.STUDENT) || request.getRole().equals(Role.TEACHER)
                || request.getRole().equals(Role.ADMIN)))
            throw new CustomException(HttpStatus.BAD_REQUEST, "Incorrect role");

        if (!request.getEmail().contains("@")) throw new CustomException(HttpStatus.BAD_REQUEST, "Not a valid email");
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, "Вече съществува профил с посочения имейл адрес");
        }
        if (request.getRole().equals(Role.STUDENT)) {
            var user = Student.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .pictureLocation("images/default_image.jpg")
                    .build();
            var savedUser = studentRepository.save(user);
            jwtToken = jwtService.generateToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(savedUser, jwtToken, false, Timestamp.valueOf(LocalDateTime.now()));
        } else {
            var user = Teacher.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .pictureLocation("images/default_image.jpg")
                    .isEnabled(true)
                    .build();
            var savedUser = teacherRepository.save(user);
            jwtToken = jwtService.generateToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(savedUser, jwtToken, false, Timestamp.valueOf(LocalDateTime.now()));
        }

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(request.getEmail());
        emailDetails.setSubject("Потвърждение на регистрация");
        emailDetails.setMsgBody("Още не сме добавили линка във фронтенда да ти потвърждава имейла!");
        emailService.sendSimpleMail(emailDetails);
        //TODO Add email verification
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Изпратено е потвърждение за регистрацията на посочения от Вас имейл")
                .build();
    }

    public String getEmailResetToken(String email) throws CustomException {
        Optional<User> user = repository.findByEmail(email);
        if (user.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма регистриран потребител с този имейл");
        } else {
            User realUser = user.get();
            String resetToken = UUID.randomUUID().toString();
            realUser.setResetToken(resetToken);

            // Save token to database
            repository.save(realUser);

            return resetUrl + resetToken;
        }
    }

    public boolean validateResetToken(String token) {
        return repository.findByResetToken(token).isPresent();
    }

    public boolean resetPassword(AuthenticationRequest request) {
        var user1 = repository.findByResetToken(request.getToken());
        if (user1.isEmpty()) return false;
        User user = user1.get();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetToken(null);
        revokeAllUserTokens(user);
        repository.save(user);
        return true;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user;
        if (request.getEmail() == null) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            user = repository.findByUsername(request.getUsername())
                    .orElseThrow();
        } else {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            user = repository.findByEmail(request.getEmail())
                    .orElseThrow();
        }
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken, request.isRememberMe(), Timestamp.valueOf(LocalDateTime.now()));
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken, boolean rememberMe, Timestamp timestamp) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .rememberMe(rememberMe)
                .timestamp(timestamp)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken, false, Timestamp.valueOf(LocalDateTime.now()));
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
