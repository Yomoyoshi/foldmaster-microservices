package com.foldmaster.userservice.controller;

import com.foldmaster.common.dto.ApiResponse;
import com.foldmaster.common.security.JwtUtil;
import com.foldmaster.userservice.dto.LoginRequest;
import com.foldmaster.userservice.entity.User;
import com.foldmaster.userservice.service.EmailService;
import com.foldmaster.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();

        if (phone == null || phone.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Phone and password are required"));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phone, password)
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Неверный логин или пароль"));
        }

        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));

        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, String> response = Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "phone", user.getPhone(),
                "id", String.valueOf(user.getId()),
                "email", user.getEmail() != null ? user.getEmail() : ""
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        if (userService.findByPhone(user.getPhone()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("User with this phone already exists"));
        }
        User created = userService.register(user);
        return ResponseEntity.ok(ApiResponse.success("User registered", created));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> payload) {
        String phone = payload.get("phone");
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Номер телефона обязателен"));
        }

        String token = userService.generateResetToken(phone);
        if (token != null) {
            userService.findByPhone(phone).ifPresent(user -> {
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    emailService.sendResetPasswordLink(user.getEmail(), token);
                }
            });
        }

        // Всегда возвращаем 200, чтобы не сообщать злоумышленнику о существовании номера
        return ResponseEntity.ok(
                ApiResponse.success("Если номер зарегистрирован, письмо отправлено", null)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Токен и новый пароль обязательны"));
        }

        boolean success = userService.resetPassword(token, newPassword);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Недействительный или истёкший токен"));
        }
        return ResponseEntity.ok(ApiResponse.success("Пароль успешно изменён", null));
    }

}