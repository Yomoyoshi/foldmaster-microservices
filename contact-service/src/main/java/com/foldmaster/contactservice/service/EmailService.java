package com.foldmaster.contactservice.service;

import com.foldmaster.contactservice.entity.ContactMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${contact.email.to}")
    private String toEmail;

    @Value("${contact.email.subject}")
    private String subject;

    public void sendContactNotification(ContactMessage message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(String.format(
                    "New contact message from %s (%s):\n\n%s",
                    message.getName(),
                    message.getEmail(),
                    message.getText()
            ));
            mailSender.send(mailMessage);
            log.info("Email notification sent for message id: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send email notification for message id {}: {}", message.getId(), e.getMessage());
            // Не бросаем исключение, чтобы не нарушить сохранение сообщения
        }
    }
}