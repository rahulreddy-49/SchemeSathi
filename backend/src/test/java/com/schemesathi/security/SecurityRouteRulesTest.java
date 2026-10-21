package com.schemesathi.security;

import com.schemesathi.entity.User;
import com.schemesathi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRouteRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String citizenToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail("testcitizen@schemesathi.test")) {
            User citizen = User.builder()
                    .email("testcitizen@schemesathi.test")
                    .password(passwordEncoder.encode("TestCitizen@123"))
                    .fullName("Test Citizen")
                    .role(User.UserRole.CITIZEN)
                    .active(true)
                    .build();
            userRepository.save(citizen);
        }

        if (!userRepository.existsByEmail("testadmin@schemesathi.test")) {
            User admin = User.builder()
                    .email("testadmin@schemesathi.test")
                    .password(passwordEncoder.encode("TestAdmin@123"))
                    .fullName("Test Admin")
                    .role(User.UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }

        citizenToken = jwtTokenProvider.generateToken("testcitizen@schemesathi.test", "CITIZEN");
        adminToken = jwtTokenProvider.generateToken("testadmin@schemesathi.test", "ADMIN");
    }

    // 1. PUBLIC SCHEME BROWSING
    @Test
    @DisplayName("Public scheme browsing endpoints should remain accessible without authentication")
    void testPublicSchemeBrowsing_allowedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/schemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/schemes/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 2. UNAUTHENTICATED CALLS TO PROTECTED ENDPOINTS MUST RETURN 401 (NOT 500 NPE)
    @Test
    @DisplayName("GET /api/schemes/saved without auth must return 401 Unauthorized (never 500 NPE)")
    void testSavedSchemes_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/schemes/saved"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Authentication required")));
    }

    @Test
    @DisplayName("Eligibility checks without auth must return 401 Unauthorized")
    void testEligibility_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/schemes/1/eligibility"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/schemes/eligibility/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST and DELETE /api/schemes/{id}/save without auth must return 401 Unauthorized")
    void testSaveSchemeMutations_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/schemes/1/save"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(delete("/api/schemes/1/save"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Profile endpoints without auth must return 401 Unauthorized")
    void testProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(put("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("AI chat endpoint without auth must return 401 Unauthorized")
    void testAiChat_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hello\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // 3. ADMIN ROLE ACCESS CONTROL
    @Test
    @DisplayName("Admin endpoint without auth must return 401 Unauthorized")
    void testAdminEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/schemes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Citizen role accessing admin endpoint must return 403 Forbidden")
    void testAdminEndpoint_citizenRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/schemes")
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));
    }

    @Test
    @DisplayName("Admin role accessing admin endpoint is authorized (not 401 or 403)")
    void testAdminEndpoint_adminRole_authorized() throws Exception {
        mockMvc.perform(get("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 401 && status != 403 : "Admin should not be rejected with 401 or 403; got: " + status;
                });
    }

    // 4. H2 CONSOLE IS INACCESSIBLE WHEN DISABLED
    @Test
    @DisplayName("H2 console must be inaccessible by default when spring.h2.console.enabled is false")
    void testH2Console_disabledByDefault_inaccessible() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 401 || status == 403 || status == 404
                            : "H2 console must be protected or inaccessible; got: " + status;
                });
    }

    // 5. AUTHENTICATED CITIZEN CAN ACCESS SAVED SCHEMES SAFELY
    @Test
    @DisplayName("Authenticated citizen can access /api/schemes/saved without error")
    void testSavedSchemes_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/schemes/saved")
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
