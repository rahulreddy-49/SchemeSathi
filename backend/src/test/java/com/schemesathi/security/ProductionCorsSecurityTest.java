package com.schemesathi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that when the prod profile is active:
 * 1. http://localhost:* is rejected.
 * 2. http://127.0.0.1:* is rejected.
 * 3. Configured trusted frontend origins are accepted with credentials.
 * 4. Arbitrary vercel subdomains are rejected.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:prodcorstest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "app.jwt.secret=ProductionTestSecretKeyThatHasAtLeast256BitsOfEntropyAndLength!!",
    "app.cors.allowed-origins=https://trusted-frontend.vercel.app,https://schemesathi.in"
})
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class ProductionCorsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Production CORS rejects http://localhost:3000")
    void testProductionCors_rejectsLocalhost() throws Exception {
        mockMvc.perform(options("/api/schemes")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Production CORS rejects http://127.0.0.1:5173")
    void testProductionCors_rejectsLoopbackIp() throws Exception {
        mockMvc.perform(options("/api/schemes")
                .header("Origin", "http://127.0.0.1:5173")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Production CORS accepts explicitly configured trusted frontend origin")
    void testProductionCors_acceptsConfiguredTrustedOrigin() throws Exception {
        mockMvc.perform(options("/api/schemes")
                .header("Origin", "https://trusted-frontend.vercel.app")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://trusted-frontend.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Production CORS rejects arbitrary attacker vercel subdomain")
    void testProductionCors_rejectsArbitraryVercelOrigin() throws Exception {
        mockMvc.perform(options("/api/schemes")
                .header("Origin", "https://attacker-app.vercel.app")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
