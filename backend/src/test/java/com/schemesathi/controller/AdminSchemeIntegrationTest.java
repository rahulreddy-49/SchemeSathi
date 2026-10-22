package com.schemesathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemesathi.dto.AdminSchemeDto;
import com.schemesathi.dto.AdminSchemeDto.AdminEligibilityRuleDto;
import com.schemesathi.dto.AdminSchemeDto.AdminRequiredDocumentDto;
import com.schemesathi.dto.AdminVerificationRequestDto;
import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.dto.SchemeApplicationDto;
import com.schemesathi.entity.ApplicationStatus;
import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.User;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSchemeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String citizenToken;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail("admin_audit@schemesathi.test")) {
            User admin = User.builder()
                    .email("admin_audit@schemesathi.test")
                    .password(passwordEncoder.encode("AdminPass123!"))
                    .fullName("Admin Audit Officer")
                    .role(User.UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("citizen_audit@schemesathi.test")) {
            User citizen = User.builder()
                    .email("citizen_audit@schemesathi.test")
                    .password(passwordEncoder.encode("CitizenPass123!"))
                    .fullName("Citizen Audit User")
                    .role(User.UserRole.CITIZEN)
                    .active(true)
                    .build();
            userRepository.save(citizen);
        }

        adminToken = jwtTokenProvider.generateToken("admin_audit@schemesathi.test", "ADMIN");
        citizenToken = jwtTokenProvider.generateToken("citizen_audit@schemesathi.test", "CITIZEN");
    }

    @Test
    @DisplayName("1. Unauthenticated users cannot access admin scheme management endpoints")
    void testAdminEndpoints_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/schemes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Authentication required")));

        mockMvc.perform(post("/api/admin/schemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Authenticated citizens cannot access admin scheme management endpoints")
    void testAdminEndpoints_citizenRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/schemes")
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));

        mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Authorized administrator can retrieve schemes list and dashboard metrics")
    void testAdminEndpoints_adminRole_succeeds() throws Exception {
        mockMvc.perform(get("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/admin/schemes/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSchemes").isNumber())
                .andExpect(jsonPath("$.data.publishedSchemes").isNumber());
    }

    @Test
    @DisplayName("4. Admin creates a draft scheme; drafts must NOT be visible to public citizens")
    void testCreateDraftScheme_isolatedFromPublic() throws Exception {
        AdminSchemeDto newScheme = new AdminSchemeDto();
        newScheme.setName("Draft Green Energy Subsidy");
        newScheme.setDescription("Subsidy for rooftop solar panel installations in residential buildings.");
        newScheme.setBenefitCategory("Energy");
        newScheme.setScope("CENTRAL");
        newScheme.setPublicationStatus("DRAFT");
        newScheme.setVerificationStatus("UNVERIFIED");

        AdminEligibilityRuleDto rule = new AdminEligibilityRuleDto();
        rule.setCriterionField("annualIncome");
        rule.setOperator("LTE");
        rule.setValue("800000");
        rule.setDisplayLabel("Annual Income Limit");
        rule.setMandatory(true);
        newScheme.setEligibilityRules(List.of(rule));

        // Create via admin endpoint
        MvcResult createResult = mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newScheme)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.publicationStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.verificationStatus").value("UNVERIFIED"))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long schemeId = objectMapper.readTree(responseJson).path("data").path("id").asLong();
        assertThat(schemeId).isPositive();

        // 5. PUBLIC ISOLATION: Public citizen should NOT see this draft in catalog or direct detail
        mockMvc.perform(get("/api/schemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + schemeId + ")]").doesNotExist());

        mockMvc.perform(get("/api/schemes/" + schemeId))
                .andExpect(status().isNotFound());

        // Clean up
        schemeRepository.deleteById(schemeId);
    }

    @Test
    @DisplayName("5. Reject malformed eligibility rules: invalid BETWEEN format, negative values, unsupported operators")
    void testEligibilityRuleIntegrity_rejectsMalformedRules() throws Exception {
        // Test A: Malformed BETWEEN value (not two numbers)
        AdminSchemeDto malformedBetween = new AdminSchemeDto();
        malformedBetween.setName("Malformed Between Rule Scheme");
        malformedBetween.setDescription("Scheme with invalid rule syntax.");
        malformedBetween.setBenefitCategory("Housing");
        malformedBetween.setScope("CENTRAL");

        AdminEligibilityRuleDto badBetweenRule = new AdminEligibilityRuleDto();
        badBetweenRule.setCriterionField("age");
        badBetweenRule.setOperator("BETWEEN");
        badBetweenRule.setValue("invalid_range"); // NOT numeric min:max
        malformedBetween.setEligibilityRules(List.of(badBetweenRule));

        mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(malformedBetween)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Malformed BETWEEN value")));

        // Test B: Unsupported operator
        AdminSchemeDto unsupportedOp = new AdminSchemeDto();
        unsupportedOp.setName("Unsupported Operator Scheme");
        unsupportedOp.setDescription("Scheme with invalid rule syntax.");
        unsupportedOp.setBenefitCategory("Housing");

        AdminEligibilityRuleDto badOpRule = new AdminEligibilityRuleDto();
        badOpRule.setCriterionField("age");
        badOpRule.setOperator("REGEX_MATCH"); // Unsupported
        badOpRule.setValue(".*");
        unsupportedOp.setEligibilityRules(List.of(badOpRule));

        mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unsupportedOp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Unsupported operator")));

        // Test C: Negative numeric value where impossible
        AdminSchemeDto negativeValue = new AdminSchemeDto();
        negativeValue.setName("Negative Income Scheme");
        negativeValue.setDescription("Scheme with negative income limit.");
        negativeValue.setBenefitCategory("Housing");

        AdminEligibilityRuleDto negRule = new AdminEligibilityRuleDto();
        negRule.setCriterionField("annualIncome");
        negRule.setOperator("LTE");
        negRule.setValue("-50000"); // Negative income
        negativeValue.setEligibilityRules(List.of(negRule));

        mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeValue)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("cannot be negative")));
    }

    @Test
    @DisplayName("6. Publishing quality gate: unverified schemes cannot be published")
    void testPublishQualityGate_rejectsUnverifiedScheme() throws Exception {
        // Create an unverified draft scheme
        AdminSchemeDto draft = new AdminSchemeDto();
        draft.setName("Unverified Trial Scheme");
        draft.setDescription("Trial scheme pending gazette verification.");
        draft.setBenefitCategory("Agriculture");
        draft.setPublicationStatus("DRAFT");
        draft.setVerificationStatus("UNVERIFIED");

        AdminEligibilityRuleDto rule = new AdminEligibilityRuleDto();
        rule.setCriterionField("landArea");
        rule.setOperator("LTE");
        rule.setValue("5.0");
        draft.setEligibilityRules(List.of(rule));

        MvcResult createResult = mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isCreated())
                .andReturn();

        Long schemeId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Attempt to publish before verification -> MUST FAIL with 400
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/publish")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Scheme source must be verified before publishing")));

        // Clean up
        schemeRepository.deleteById(schemeId);
    }

    @Test
    @DisplayName("7. Complete verified administrative publishing lifecycle: verify -> publish -> public access -> archive")
    void testCompleteAdministrativeLifecycle() throws Exception {
        // 1. Create a draft scheme
        AdminSchemeDto newScheme = new AdminSchemeDto();
        newScheme.setName("National Rural Electric Mobility Mission");
        newScheme.setDescription("Financial subsidy of ₹25,000 for rural artisans purchasing electric 2-wheelers.");
        newScheme.setBenefitCategory("Employment");
        newScheme.setScope("CENTRAL");
        newScheme.setPublicationStatus("DRAFT");
        newScheme.setVerificationStatus("NEEDS_REVIEW");
        newScheme.setOfficialUrl("https://heavyindustries.gov.in/schemes/emobility");

        AdminEligibilityRuleDto ageRule = new AdminEligibilityRuleDto();
        ageRule.setCriterionField("age");
        ageRule.setOperator("BETWEEN");
        ageRule.setValue("18:60");
        ageRule.setDisplayLabel("Age Limit");
        ageRule.setMandatory(true);

        AdminEligibilityRuleDto incomeRule = new AdminEligibilityRuleDto();
        incomeRule.setCriterionField("annualIncome");
        incomeRule.setOperator("LTE");
        incomeRule.setValue("300000");
        incomeRule.setDisplayLabel("Annual Income Limit");
        incomeRule.setMandatory(true);

        newScheme.setEligibilityRules(List.of(ageRule, incomeRule));

        AdminRequiredDocumentDto doc = new AdminRequiredDocumentDto();
        doc.setDocumentName("Artisan Card or Udyam Aadhaar");
        doc.setMandatory(true);
        newScheme.setRequiredDocuments(List.of(doc));

        MvcResult createResult = mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newScheme)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.publicationStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.verificationStatus").value("NEEDS_REVIEW"))
                .andReturn();

        Long schemeId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Perform official source review workflow
        AdminVerificationRequestDto verification = new AdminVerificationRequestDto(
                "https://heavyindustries.gov.in/gazette/2024/notification-emobility.pdf",
                "VERIFIED",
                "Verified against Ministry of Heavy Industries Official Gazette Notification No. 492 dated 14-Aug-2024.",
                LocalDate.now()
        );

        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/verify")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.sourceUrl").value("https://heavyindustries.gov.in/gazette/2024/notification-emobility.pdf"));

        // 3. Publish the scheme
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/publish")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.publicationStatus").value("PUBLISHED"));

        // 4. Scheme MUST now be accessible in public catalog
        mockMvc.perform(get("/api/schemes/" + schemeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("National Rural Electric Mobility Mission"));

        // 5. Archive the scheme
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/archive")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.publicationStatus").value("ARCHIVED"));

        // 6. Archived scheme is removed from public catalog
        mockMvc.perform(get("/api/schemes/" + schemeId))
                .andExpect(status().isNotFound());

        // 7. Delete the test scheme
        mockMvc.perform(delete("/api/admin/schemes/" + schemeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(schemeRepository.findById(schemeId)).isEmpty();
    }

    @Test
    @DisplayName("8. Administrator verification requires valid review evidence and notes (>= 10 characters)")
    void testVerificationRequiresAffirmativeNotes() throws Exception {
        // Create draft scheme
        AdminSchemeDto draft = new AdminSchemeDto();
        draft.setName("Kisan Tractor Subsidy");
        draft.setDescription("Subsidy for purchasing farm tractors.");
        draft.setBenefitCategory("Agriculture");
        draft.setPublicationStatus("DRAFT");
        draft.setVerificationStatus("NEEDS_REVIEW");

        MvcResult result = mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isCreated())
                .andReturn();

        Long schemeId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        // A. Attempt to verify without notes -> MUST FAIL with 400
        AdminVerificationRequestDto noNotesReq = new AdminVerificationRequestDto(
                "https://agricoop.gov.in/tractors",
                "VERIFIED",
                "",
                LocalDate.now()
        );

        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/verify")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noNotesReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Meaningful administrative review notes (at least 10 characters) are required")));

        // B. Attempt to verify with short notes (< 10 chars) -> MUST FAIL with 400
        AdminVerificationRequestDto shortNotesReq = new AdminVerificationRequestDto(
                "https://agricoop.gov.in/tractors",
                "VERIFIED",
                "Looks ok",
                LocalDate.now()
        );

        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/verify")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortNotesReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Meaningful administrative review notes (at least 10 characters) are required")));

        // C. Verify with valid affirmative notes (>= 10 chars) -> SUCCEEDS with 200
        AdminVerificationRequestDto validReq = new AdminVerificationRequestDto(
                "https://agricoop.gov.in/tractors",
                "VERIFIED",
                "Verified against Department of Agriculture Gazette Order #1024/2024.",
                LocalDate.now()
        );

        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/verify")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verificationNotes").value(containsString("Verified against Department of Agriculture")));

        // Clean up
        schemeRepository.deleteById(schemeId);
    }

    @Test
    @DisplayName("9. Demo eligibility endpoint rejects draft and archived schemes, but evaluates published schemes")
    void testDemoEligibility_rejectsDraftAndArchivedSchemes() throws Exception {
        // 1. Create a draft scheme with a rule
        AdminSchemeDto schemeDto = new AdminSchemeDto();
        schemeDto.setName("Demo Renewable Solar Mission");
        schemeDto.setDescription("Solar subsidies for residential rooftops.");
        schemeDto.setBenefitCategory("Energy");
        schemeDto.setScope("CENTRAL");
        schemeDto.setPublicationStatus("DRAFT");
        schemeDto.setVerificationStatus("NEEDS_REVIEW");
        schemeDto.setOfficialUrl("https://mnre.gov.in/solar");

        AdminEligibilityRuleDto rule = new AdminEligibilityRuleDto();
        rule.setCriterionField("annualIncome");
        rule.setOperator("LTE");
        rule.setValue("500000");
        rule.setDisplayLabel("Annual Income Limit");
        rule.setMandatory(true);
        schemeDto.setEligibilityRules(List.of(rule));

        MvcResult createResult = mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(schemeDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long schemeId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        CitizenProfileDto profile = new CitizenProfileDto();
        profile.setAge(30);
        profile.setAnnualIncome(350000.0);
        profile.setState("Telangana");

        // A. Draft scheme MUST return 404 in demo eligibility endpoint
        mockMvc.perform(post("/api/demo/eligibility/" + schemeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isNotFound());

        // B. Verify and publish scheme
        AdminVerificationRequestDto verifyDto = new AdminVerificationRequestDto(
                "https://mnre.gov.in/solar/gazette",
                "VERIFIED",
                "Verified against Ministry of New and Renewable Energy Gazette Notification 2024.",
                LocalDate.now()
        );
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/verify")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/publish")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // C. Published scheme MUST succeed in demo eligibility endpoint
        mockMvc.perform(post("/api/demo/eligibility/" + schemeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.schemeId").value(schemeId))
                .andExpect(jsonPath("$.data.overallStatus").value("ELIGIBLE"));

        // D. Archive scheme
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/archive")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // E. Archived scheme MUST return 404 in demo eligibility endpoint
        mockMvc.perform(post("/api/demo/eligibility/" + schemeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isNotFound());

        // Clean up
        schemeRepository.deleteById(schemeId);
    }

    @Test
    @DisplayName("10. Application tracking rejects draft and archived schemes, but succeeds for published schemes")
    void testApplicationTracking_rejectsDraftAndArchivedSchemes() throws Exception {
        // 1. Create a draft scheme
        AdminSchemeDto schemeDto = new AdminSchemeDto();
        schemeDto.setName("Handloom Weaver Assistance Mission");
        schemeDto.setDescription("Financial grant for registered handloom weavers.");
        schemeDto.setBenefitCategory("Employment");
        schemeDto.setScope("CENTRAL");
        schemeDto.setPublicationStatus("DRAFT");
        schemeDto.setVerificationStatus("NEEDS_REVIEW");
        schemeDto.setOfficialUrl("https://handlooms.nic.in");

        AdminEligibilityRuleDto rule = new AdminEligibilityRuleDto();
        rule.setCriterionField("occupation");
        rule.setOperator("EQ");
        rule.setValue("Artisan");
        rule.setDisplayLabel("Occupation");
        rule.setMandatory(true);
        schemeDto.setEligibilityRules(List.of(rule));

        MvcResult createResult = mockMvc.perform(post("/api/admin/schemes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(schemeDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long schemeId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        SchemeApplicationDto.CreateApplicationRequest appReq = new SchemeApplicationDto.CreateApplicationRequest();
        appReq.setSchemeId(schemeId);
        appReq.setStatus(ApplicationStatus.PLANNING);
        appReq.setNotes("Preparing application for weaver grant");

        // A. Citizen attempting to track a DRAFT scheme -> MUST FAIL with 400
        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Scheme not found with id: " + schemeId)));

        // B. Verify and publish scheme
        AdminVerificationRequestDto verifyDto = new AdminVerificationRequestDto(
                "https://handlooms.nic.in/gazette/weavers-grant-2024.pdf",
                "VERIFIED",
                "Verified against Ministry of Textiles Gazette Notification #308.",
                LocalDate.now()
        );
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/verify")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/publish")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // C. Citizen tracking PUBLISHED scheme -> SUCCEEDS with 201
        MvcResult appResult = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.schemeId").value(schemeId))
                .andReturn();

        Long createdAppId = objectMapper.readTree(appResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Delete created application before archiving/cleaning up
        mockMvc.perform(delete("/api/applications/" + createdAppId)
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk());

        // D. Archive scheme
        mockMvc.perform(patch("/api/admin/schemes/" + schemeId + "/archive")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // E. Citizen attempting to track ARCHIVED scheme -> MUST FAIL with 400
        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Scheme not found with id: " + schemeId)));

        // Clean up
        schemeRepository.deleteById(schemeId);
    }
}
