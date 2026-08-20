package com.foldmaster.common.dto;

import java.time.LocalDateTime;

/**
 * DTO отзыва.
 */
public record ReviewDto(
        Long id,
        Long productId,
        Long userId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}