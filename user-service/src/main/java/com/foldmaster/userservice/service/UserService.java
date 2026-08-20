package com.foldmaster.userservice.service;

import com.foldmaster.common.dto.ApiResponse;
import com.foldmaster.userservice.entity.User;
import com.foldmaster.userservice.exception.UserNotFoundException;
import com.foldmaster.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(User user) {
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new RuntimeException("Пользователь с таким номером уже зарегистрирован");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            user.setUsername(user.getPhone());
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Требуется пароль");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User update(Long id, User updatedUser) {
        User existing = findById(id);
        if (updatedUser.getEmail() != null) {
            existing.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhone() != null) {
            existing.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    /**
     * Генерирует токен сброса для пользователя по номеру телефона.
     * @param phone номер телефона
     * @return сгенерированный токен или null, если пользователь не найден
     */
    @Transactional
    public String generateResetToken(String phone) {
        Optional<User> opt = userRepository.findByPhone(phone);
        if (opt.isEmpty()) {
            log.info("User not found by phone: {}", phone);
            return null;
        }
        User user = opt.get();
        log.info("User was found by phone: {}, email: {}", user.getPhone(), user.getEmail());

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // срок действия 1 час
        userRepository.save(user);
        log.debug("Сгенерирован токен сброса для пользователя {}", phone);
        return token;
    }

    /**
     * Сбрасывает пароль, если токен действителен.
     * @param token токен сброса
     * @param newPassword новый пароль (в открытом виде)
     * @return true, если пароль изменён; false, если токен недействителен
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> optUser = userRepository.findByResetToken(token);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        log.info("Пароль изменён для пользователя {}", user.getPhone());
        return true;
    }
}