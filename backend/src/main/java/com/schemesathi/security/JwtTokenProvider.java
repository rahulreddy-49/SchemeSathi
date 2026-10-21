package com.schemesathi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    public static final String DEFAULT_DEV_SECRET = "SchemeSathiDefaultDevSecret2024ThisMustBeAtLeast256BitsLong!!";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs,
            Environment env) {
        boolean isProduction = env != null && (
                env.matchesProfiles("prod", "production")
                || "prod".equalsIgnoreCase(env.getProperty("app.environment"))
                || "production".equalsIgnoreCase(env.getProperty("app.environment"))
        );

        String trimmedSecret = secret != null ? secret.trim() : "";

        if (isProduction) {
            if (trimmedSecret.isEmpty() || DEFAULT_DEV_SECRET.equals(trimmedSecret) || trimmedSecret.length() < 32) {
                throw new IllegalStateException(
                    "FATAL SECURITY MISCONFIGURATION: Production cannot start using a known default or insecure JWT secret. " +
                    "A securely generated, random JWT_SECRET environment variable of at least 256 bits (32+ characters) is required."
                );
            }
            log.info("JWT provider initialized securely with production-grade key configuration.");
        } else if (DEFAULT_DEV_SECRET.equals(trimmedSecret)) {
            log.warn("Running with default development JWT secret. This is strictly prohibited in production.");
        }

        this.key = Keys.hmacShaKeyFor(trimmedSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
