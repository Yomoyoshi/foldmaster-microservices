package com.foldmaster.contactservice.controller;

import com.foldmaster.common.dto.ApiResponse;
import com.foldmaster.contactservice.entity.ContactMessage;
import com.foldmaster.contactservice.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // Открытый эндпоинт для отправки сообщения (без авторизации)
    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessage>> sendMessage(@Valid @RequestBody ContactMessage message) {
        try {
            ContactMessage saved = contactService.saveMessage(message);
            return ResponseEntity.ok(ApiResponse.success("Message sent", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Защищённые эндпоинты (для администрирования)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getAllMessages() {
        return ResponseEntity.ok(ApiResponse.success(contactService.getAllMessages()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactMessage>> getMessage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contactService.getMessageById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        contactService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getUserMessages(@PathVariable Long userId) {
        List<ContactMessage> messages = contactService.getMessagesForUser(userId, 5);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}