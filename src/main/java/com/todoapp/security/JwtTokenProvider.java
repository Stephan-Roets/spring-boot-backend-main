package com.todoapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiryMs;
    private final long verificationTokenExpiryMs;

    public JwtTokenProvider(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
        @Value(
            "${app.jwt.verification-token-expiry-ms}"
        ) long verificationTokenExpiryMs
    ) {
        // Validate secret length (minimum 256 bits = 32 bytes)
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "JWT secret cannot be null or empty. Set 'app.jwt.secret' in application properties."
            );
        }

        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                String.format(
                    "JWT secret is too short (%d bytes). Minimum required: 32 bytes (256 bits). " +
                        "Generate a secure secret with: openssl rand -base64 64",
                    keyBytes.length
                )
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.verificationTokenExpiryMs = verificationTokenExpiryMs;
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public String generateVerificationToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + verificationTokenExpiryMs);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", "email_verification")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
