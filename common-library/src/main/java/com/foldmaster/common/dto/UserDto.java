package com.foldmaster.common.dto;

/**
 * DTO пользователя.
 */
public record UserDto(
        Long id,
        String username,
        String email,
        String phone,
        String role
) {}