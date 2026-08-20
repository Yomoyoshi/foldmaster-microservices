package com.foldmaster.common.dto;

import java.math.BigDecimal;

/**
 * DTO товара.
 */
public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer stockQuantity
) {}