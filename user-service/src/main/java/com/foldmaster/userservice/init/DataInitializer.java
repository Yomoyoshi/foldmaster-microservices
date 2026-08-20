package com.foldmaster.userservice.init;

import com.foldmaster.userservice.entity.User;
import com.foldmaster.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("+79999999999")
                    .email("admin@foldmaster.ru")
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Администратор создан с телефоном +79999999999");
        }
    }
}