package ru.netology.cloudservice.util;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private static final String SECRET_KEY = "your-secret-key-which-should-be-long-enough-to-pass-validation";
    private static final String EMAIL = "test@example.com";
    private static final long EXPIRATION = 3600000L;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        String token = jwtTokenUtil.generateToken(EMAIL);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedEmail = jwtTokenUtil.getEmailFromToken(token);
        assertEquals(EMAIL, extractedEmail);
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        String token = jwtTokenUtil.generateToken(EMAIL);
        String extractedEmail = jwtTokenUtil.getEmailFromToken(token);
        assertEquals(EMAIL, extractedEmail);
    }

    @Test
    void getEmailFromToken_ShouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> jwtTokenUtil.getEmailFromToken(invalidToken));
    }

    @Test
    void getEmailFromToken_ShouldThrowExceptionForExpiredToken() throws Exception {
        String expiredToken = Jwts.builder()
                .setSubject(EMAIL)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        assertThrows(JwtException.class, () -> jwtTokenUtil.getEmailFromToken(expiredToken));
    }

    @Test
    void getEmailFromToken_ShouldFailWithModifiedSignature() {
        String token = Jwts.builder()
                .setSubject(EMAIL)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        JwtTokenUtil modifiedJwtUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(modifiedJwtUtil, "secret", "another-secret-key");
        ReflectionTestUtils.setField(modifiedJwtUtil, "expiration", EXPIRATION);

        assertThrows(JwtException.class, () -> modifiedJwtUtil.getEmailFromToken(token));
    }

}