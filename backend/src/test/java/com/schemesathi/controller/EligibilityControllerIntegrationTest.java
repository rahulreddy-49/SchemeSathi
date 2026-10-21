package com.schemesathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.User;
import com.schemesathi.repository.CitizenProfileRepository;
import com.schemesathi.repository.SchemeRepository;
import com.schemesathi.repository.UserRepository;
import com.schemesathi.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EligibilityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CitizenProfileRepository citizenProfileRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("formtest_citizen@schemesathi.test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("formtest_citizen@schemesathi.test")
                        .password(passwordEncoder.encode("TestPassword123!"))
                        .fullName("Form Test Citizen")
                        .role(User.UserRole.CITIZEN)
                        .active(true)
                        .build()));

        token = jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getRole().name());

        // Establish an existing saved profile: Farmer with low income (eligible for agricultural schemes)
        CitizenProfile savedProfile = citizenProfileRepository.findByUserId(testUser.getId())
                .orElseGet(() -> CitizenProfile.builder().user(testUser).build());
        savedProfile.setAge(40);
        savedProfile.setGender("Male");
        savedProfile.setState("Telangana");
        savedProfile.setOccupation("Farmer");
        savedProfile.setAnnualIncome(80000.0);
        savedProfile.setLandOwnership(true);
        savedProfile.setLandArea(2.0);
        savedProfile.setCategory("OBC");
        citizenProfileRepository.save(savedProfile);
    }

    @Test
    @DisplayName("POST /api/schemes/eligibility/all must evaluate submitted form values and NOT modify saved profile")
    void shouldEvaluateSubmittedValuesWithoutModifyingSavedProfile() throws Exception {
        // User submits temporary form values differing from saved profile:
        // High income, non-farmer (not eligible for PM-KISAN)
        CitizenProfileDto submittedForm = CitizenProfileDto.builder()
                .age(28)
                .gender("Female")
                .state("Telangana")
                .occupation("IT Consultant")
                .annualIncome(900000.0) // high income
                .landOwnership(false)   // no land
                .landArea(0.0)
                .category("General")
                .build();

        mockMvc.perform(post("/api/schemes/eligibility/all")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submittedForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                // PM-KISAN requires landOwnership=true and occupation in Farmer/Agriculture.
                // With submittedForm, it MUST NOT be ELIGIBLE
                .andExpect(jsonPath("$.data[?(@.schemeName =~ /.*PM-KISAN.*/)].overallStatus").value(hasItem("NOT_ELIGIBLE")));

        // CRITICAL CHECK: Verify saved profile in database is completely UNTOUCHED
        CitizenProfile dbProfile = citizenProfileRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(dbProfile.getOccupation()).isEqualTo("Farmer");
        assertThat(dbProfile.getAnnualIncome()).isEqualTo(80000.0);
        assertThat(dbProfile.getLandOwnership()).isTrue();
    }

    @Test
    @DisplayName("GET /api/schemes/eligibility/all should evaluate saved profile as a distinct workflow")
    void shouldEvaluateSavedProfileOnGet() throws Exception {
        // Saved profile has income 80000, farmer, land ownership = true
        // PM-KISAN must match
        mockMvc.perform(get("/api/schemes/eligibility/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.schemeName =~ /.*PM-KISAN.*/)].overallStatus").value(hasItem("ELIGIBLE")));
    }

    @Test
    @DisplayName("POST /api/schemes/{id}/eligibility must evaluate submitted form values for single scheme")
    void shouldEvaluateSingleSchemeWithSubmittedForm() throws Exception {
        Scheme pmKisan = schemeRepository.findAll().stream()
                .filter(s -> s.getName().contains("PM-KISAN"))
                .findFirst()
                .orElseThrow();

        // Submit form where landOwnership = false
        CitizenProfileDto form = CitizenProfileDto.builder()
                .age(35)
                .occupation("Farmer")
                .landOwnership(false)
                .build();

        mockMvc.perform(post("/api/schemes/" + pmKisan.getId() + "/eligibility")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallStatus").value("NOT_ELIGIBLE"))
                .andExpect(jsonPath("$.data.criteriaResults[?(@.criterionName == 'landOwnership')].status").value(hasItem("FAIL")));
    }

    @Test
    @DisplayName("POST /api/demo/eligibility/all should evaluate submitted form values for unauthenticated users")
    void shouldEvaluateDemoAllSchemesWithoutAuthentication() throws Exception {
        CitizenProfileDto demoCriteria = CitizenProfileDto.builder()
                .age(35)
                .gender("Female")
                .state("Telangana")
                .occupation("Farmer")
                .annualIncome(150000.0)
                .landOwnership(true)
                .landArea(2.5)
                .bplStatus(true)
                .build();

        mockMvc.perform(post("/api/demo/eligibility/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(demoCriteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].matchScore").isNumber());
    }

    @Test
    @DisplayName("Backend validation should reject invalid input values like negative age or negative income")
    void shouldRejectInvalidInputValues() throws Exception {
        CitizenProfileDto invalidForm = CitizenProfileDto.builder()
                .age(-5) // Invalid
                .annualIncome(-1000.0) // Invalid
                .build();

        mockMvc.perform(post("/api/schemes/eligibility/all")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidForm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("cannot be negative")));
    }

    @Test
    @DisplayName("Unauthenticated requests to protected eligibility endpoint must return HTTP 401")
    void shouldDenyUnauthenticatedAccessToProtectedEligibility() throws Exception {
        mockMvc.perform(post("/api/schemes/eligibility/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }
}
