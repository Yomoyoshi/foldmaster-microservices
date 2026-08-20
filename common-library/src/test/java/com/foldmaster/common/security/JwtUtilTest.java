package com.foldmaster.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil;

    JwtUtilTest() {
        String testSecret = "mySuperSecretKeyForJwtTesting123!";
        Long testExpiration = 3600000L; // 1 час в миллисекундах
        jwtUtil = new JwtUtil(testSecret, testExpiration);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        var username = "testUser";
        var token = jwtUtil.generateToken(username);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);
        assertThat(jwtUtil.validateToken(token, username)).isTrue();
    }

    @Test
    void shouldNotValidateInvalidToken() {
        var token = "invalid.token.here";
        assertThat(jwtUtil.validateToken(token, "user")).isFalse();
    }
}