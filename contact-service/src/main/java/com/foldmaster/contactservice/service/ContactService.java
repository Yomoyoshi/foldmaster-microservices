package com.foldmaster.contactservice.service;

import com.foldmaster.contactservice.entity.ContactMessage;
import com.foldmaster.contactservice.exception.ContactNotFoundException;
import com.foldmaster.contactservice.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private static final int DAILY_LIMIT = 5;
    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    @Transactional
    public ContactMessage saveMessage(ContactMessage message) {

        LocalDateTime since = LocalDateTime.now().minusDays(1);
        long count = contactMessageRepository.countByUserIdAndCreatedAtAfter(message.getUserId(), since);
        if (count >= DAILY_LIMIT) {
            throw new IllegalArgumentException("Превышен лимит сообщений (5 в сутки)");
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        ContactMessage saved = contactMessageRepository.save(message);
        emailService.sendContactNotification(saved);
        return saved;
    }

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAll();
    }

    public ContactMessage getMessageById(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Message not found with id: " + id));
    }

    @Transactional
    public void deleteMessage(Long id) {
        ContactMessage message = getMessageById(id);
        contactMessageRepository.delete(message);
    }

    public List<ContactMessage> getMessagesForUser(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return contactMessageRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(userId, since);
    }
}
