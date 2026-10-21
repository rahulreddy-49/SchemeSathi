package com.schemesathi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String STRONG_PROD_SECRET = "ProductionSecretKeyThatHasAtLeast256BitsOfEntropyAndLength!!";

    @Test
    @DisplayName("Should generate and validate token successfully in development")
    void testGenerateAndValidateToken_dev() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtTokenProvider provider = new JwtTokenProvider(
                JwtTokenProvider.DEFAULT_DEV_SECRET,
                86400000L,
                env
        );

        String token = provider.generateToken("citizen@example.com", "CITIZEN");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("citizen@example.com", provider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("Production must fail startup if using the known default dev secret")
    void testProductionStartupFails_withDefaultSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            new JwtTokenProvider(
                    JwtTokenProvider.DEFAULT_DEV_SECRET,
                    86400000L,
                    env
            )
        );

        assertTrue(ex.getMessage().contains("Production cannot start using a known default or insecure JWT secret"));
    }

    @Test
    @DisplayName("Production must fail startup if JWT secret is blank or empty")
    void testProductionStartupFails_withBlankSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("production");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            new JwtTokenProvider(
                    "   ",
                    86400000L,
                    env
            )
        );

        assertTrue(ex.getMessage().contains("FATAL SECURITY MISCONFIGURATION"));
    }

    @Test
    @DisplayName("Production must fail startup if JWT secret is under 32 characters (256 bits)")
    void testProductionStartupFails_withWeakSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            new JwtTokenProvider(
                    "short-weak-secret-12345",
                    86400000L,
                    env
            )
        );

        assertTrue(ex.getMessage().contains("at least 256 bits"));
    }

    @Test
    @DisplayName("Production succeeds startup when provided a strong secret of 32+ characters")
    void testProductionStartupSucceeds_withStrongSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        JwtTokenProvider provider = new JwtTokenProvider(
                STRONG_PROD_SECRET,
                86400000L,
                env
        );

        String token = provider.generateToken("admin@prod.schemesathi.in", "ADMIN");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("admin@prod.schemesathi.in", provider.getEmailFromToken(token));
    }
}
