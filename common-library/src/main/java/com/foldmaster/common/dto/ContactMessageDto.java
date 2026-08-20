package com.foldmaster.common.dto;

import java.time.LocalDateTime;

/**
 * DTO контактного сообщения.
 */
public record ContactMessageDto(
        Long id,
        Long userId,          // может быть null для неавторизованных
        String name,
        String email,
        String text,
        LocalDateTime createdAt
) {}