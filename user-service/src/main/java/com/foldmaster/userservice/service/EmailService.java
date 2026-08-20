package com.foldmaster.userservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final JavaMailSender mailSender;

    /**
     * Отправляет письмо со ссылкой для сброса пароля.
     * @param toEmail – email получателя
     * @param token   – уникальный токен сброса
     */
    public void sendResetPasswordLink(String toEmail, String token) {
        log.info("Отправка письма на {}", toEmail);
        if (!EMAIL_PATTERN.matcher(toEmail).matches()) {
            log.warn("Некорректный email: {}", toEmail);
            return;
        }

        try {
            String resetLink = "http://localhost:8080/reset-password.html?token=" + token;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom("dmitryayoshin@yandex.ru");
            helper.setTo(toEmail);
            helper.setSubject("Восстановление пароля на FoldMaster");
            helper.setText("Для сброса пароля перейдите по ссылке:\n" + resetLink);

            mailSender.send(mimeMessage);
            log.info("Письмо отправлено на {}", toEmail);
        } catch (Exception e) {
            log.error("Ошибка отправки письма: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить письмо", e);
        }
    }
}
