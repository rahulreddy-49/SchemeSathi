package com.schemesathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemesathi.dto.SchemeApplicationDto;
import com.schemesathi.entity.*;
import com.schemesathi.repository.*;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SchemeApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private HouseholdMemberRepository householdMemberRepository;

    @Autowired
    private SchemeApplicationRepository schemeApplicationRepository;

    @Autowired
    private SchemeApplicationHistoryRepository schemeApplicationHistoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User primaryUser;
    private String primaryToken;

    private User secondaryUser;
    private String secondaryToken;

    private Scheme testScheme;
    private HouseholdMember primaryMember;
    private HouseholdMember secondaryMember;

    @BeforeEach
    void setUp() {
        schemeApplicationHistoryRepository.deleteAll();
        schemeApplicationRepository.deleteAll();
        householdMemberRepository.deleteAll();
        userRepository.deleteAll();

        primaryUser = userRepository.save(User.builder()
                .email("citizen_tracker@schemesathi.test")
                .password(passwordEncoder.encode("Password123!"))
                .fullName("Ramesh Kumar")
                .role(User.UserRole.CITIZEN)
                .build());
        primaryToken = jwtTokenProvider.generateToken(primaryUser.getEmail(), primaryUser.getRole().name());

        secondaryUser = userRepository.save(User.builder()
                .email("other_tracker@schemesathi.test")
                .password(passwordEncoder.encode("Password123!"))
                .fullName("Sunita Sharma")
                .role(User.UserRole.CITIZEN)
                .build());
        secondaryToken = jwtTokenProvider.generateToken(secondaryUser.getEmail(), secondaryUser.getRole().name());

        testScheme = schemeRepository.findAll().stream().findFirst().orElseGet(() -> {
            Scheme s = Scheme.builder()
                    .name("PM Awas Yojana")
                    .description("Housing scheme")
                    .governmentAuthority("Ministry of Housing")
                    .scope(Scheme.SchemeScope.CENTRAL)
                    .benefitCategory("Housing")
                    .build();
            return schemeRepository.save(s);
        });

        primaryMember = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Lakshmi Kumar")
                .relationship("Spouse")
                .age(38)
                .gender("Female")
                .state("Telangana")
                .build());

        secondaryMember = householdMemberRepository.save(HouseholdMember.builder()
                .user(secondaryUser)
                .fullName("Rahul Sharma")
                .relationship("Child")
                .age(12)
                .gender("Male")
                .state("Delhi")
                .build());
    }

    @Test
    @DisplayName("Should create application-tracking record for self with initial history")
    void createApplication_forSelf_shouldSucceedWithInitialHistory() throws Exception {
        SchemeApplicationDto.CreateApplicationRequest req = new SchemeApplicationDto.CreateApplicationRequest();
        req.setSchemeId(testScheme.getId());
        req.setStatus(ApplicationStatus.PLANNING);
        req.setNotes("Gathering income certificate and bank statements");
        req.setNextActionDate(LocalDate.now().plusDays(7));
        req.setNextActionNotes("Visit MeeSeva center next Monday");

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.schemeId", is(testScheme.getId().intValue())))
                .andExpect(jsonPath("$.data.applicantType", is("SELF")))
                .andExpect(jsonPath("$.data.applicantName", is("Ramesh Kumar")))
                .andExpect(jsonPath("$.data.status", is("PLANNING")))
                .andExpect(jsonPath("$.data.notes", containsString("Gathering income certificate")))
                .andExpect(jsonPath("$.data.history", hasSize(1)))
                .andExpect(jsonPath("$.data.history[0].toStatus", is("PLANNING")))
                .andExpect(jsonPath("$.data.history[0].initiatedBy", is("CITIZEN")));
    }

    @Test
    @DisplayName("Should create application-tracking record for owned household member")
    void createApplication_forHouseholdMember_shouldSucceed() throws Exception {
        SchemeApplicationDto.CreateApplicationRequest req = new SchemeApplicationDto.CreateApplicationRequest();
        req.setSchemeId(testScheme.getId());
        req.setHouseholdMemberId(primaryMember.getId());
        req.setStatus(ApplicationStatus.SUBMITTED);
        req.setApplicationReference("PM-AWAS-2026-12345");
        req.setAppliedDate(LocalDate.now());

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.householdMemberId", is(primaryMember.getId().intValue())))
                .andExpect(jsonPath("$.data.applicantType", is("HOUSEHOLD_MEMBER")))
                .andExpect(jsonPath("$.data.applicantName", containsString("Lakshmi Kumar")))
                .andExpect(jsonPath("$.data.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.data.applicationReference", is("PM-AWAS-2026-12345")));
    }

    @Test
    @DisplayName("Should list only applications owned by authenticated user (tenant isolation)")
    void getApplications_shouldReturnOnlyOwnedRecords() throws Exception {
        SchemeApplicationDto.CreateApplicationRequest req1 = new SchemeApplicationDto.CreateApplicationRequest();
        req1.setSchemeId(testScheme.getId());
        req1.setStatus(ApplicationStatus.PLANNING);

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + primaryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        // Other user creates an application
        SchemeApplicationDto.CreateApplicationRequest req2 = new SchemeApplicationDto.CreateApplicationRequest();
        req2.setSchemeId(testScheme.getId());
        req2.setStatus(ApplicationStatus.READY_TO_APPLY);

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + secondaryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        // Primary user should only see 1 application
        mockMvc.perform(get("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].applicantName", is("Ramesh Kumar")))
                .andExpect(jsonPath("$.data[0].status", is("PLANNING")));
    }

    @Test
    @DisplayName("Should retrieve single application with its complete audit history")
    void getApplicationById_shouldReturnDetailsWithHistory() throws Exception {
        SchemeApplication app = SchemeApplication.builder()
                .user(primaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Ramesh Kumar")
                .status(ApplicationStatus.PLANNING)
                .notes("Initial notes")
                .build();
        app.addHistory(null, ApplicationStatus.PLANNING, "Initial creation", "CITIZEN");
        app = schemeApplicationRepository.save(app);

        mockMvc.perform(get("/api/applications/" + app.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(app.getId().intValue())))
                .andExpect(jsonPath("$.data.schemeName", is(testScheme.getName())))
                .andExpect(jsonPath("$.data.history", hasSize(1)))
                .andExpect(jsonPath("$.data.history[0].toStatus", is("PLANNING")));
    }

    @Test
    @DisplayName("Should update application permitted fields like reference and next action date")
    void updateApplication_permittedFields_shouldUpdateSuccessfully() throws Exception {
        SchemeApplication app = schemeApplicationRepository.save(SchemeApplication.builder()
                .user(primaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Ramesh Kumar")
                .status(ApplicationStatus.PLANNING)
                .build());

        SchemeApplicationDto.UpdateApplicationRequest updateReq = new SchemeApplicationDto.UpdateApplicationRequest();
        updateReq.setApplicationReference("ACK-887766");
        updateReq.setNextActionDate(LocalDate.now().plusDays(5));
        updateReq.setNextActionNotes("Call toll-free helpline for status inquiry");
        updateReq.setNotes("Updated tracking notes");

        mockMvc.perform(put("/api/applications/" + app.getId())
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationReference", is("ACK-887766")))
                .andExpect(jsonPath("$.data.nextActionNotes", is("Call toll-free helpline for status inquiry")))
                .andExpect(jsonPath("$.data.notes", is("Updated tracking notes")));
    }

    @Test
    @DisplayName("Should validate valid status transitions and append to audit history")
    void updateApplication_statusTransition_valid_shouldRecordHistory() throws Exception {
        SchemeApplication app = schemeApplicationRepository.save(SchemeApplication.builder()
                .user(primaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Ramesh Kumar")
                .status(ApplicationStatus.PLANNING)
                .build());

        // 1. Transition: PLANNING -> SUBMITTED
        SchemeApplicationDto.UpdateApplicationRequest submitReq = new SchemeApplicationDto.UpdateApplicationRequest();
        submitReq.setStatus(ApplicationStatus.SUBMITTED);
        submitReq.setApplicationReference("PMAW-2026-9900");
        submitReq.setStatusChangeNote("Submitted hard copy at municipality office");

        mockMvc.perform(put("/api/applications/" + app.getId())
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.data.history", hasSize(1)))
                .andExpect(jsonPath("$.data.history[0].fromStatus", is("PLANNING")))
                .andExpect(jsonPath("$.data.history[0].toStatus", is("SUBMITTED")))
                .andExpect(jsonPath("$.data.history[0].notes", is("Submitted hard copy at municipality office")));

        // 2. Transition: SUBMITTED -> UNDER_REVIEW
        SchemeApplicationDto.UpdateApplicationRequest reviewReq = new SchemeApplicationDto.UpdateApplicationRequest();
        reviewReq.setStatus(ApplicationStatus.UNDER_REVIEW);
        reviewReq.setStatusChangeNote("SMS received stating verification has started");

        mockMvc.perform(put("/api/applications/" + app.getId())
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("UNDER_REVIEW")))
                .andExpect(jsonPath("$.data.history", hasSize(2)))
                .andExpect(jsonPath("$.data.history[1].fromStatus", is("SUBMITTED")))
                .andExpect(jsonPath("$.data.history[1].toStatus", is("UNDER_REVIEW")));
    }

    @Test
    @DisplayName("Should reject invalid status transition and return 400 Bad Request")
    void updateApplication_statusTransition_invalid_shouldReturn400() throws Exception {
        SchemeApplication app = schemeApplicationRepository.save(SchemeApplication.builder()
                .user(primaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Ramesh Kumar")
                .status(ApplicationStatus.PLANNING)
                .build());

        // PLANNING -> APPROVED is invalid (cannot skip submission/review)
        SchemeApplicationDto.UpdateApplicationRequest invalidReq = new SchemeApplicationDto.UpdateApplicationRequest();
        invalidReq.setStatus(ApplicationStatus.APPROVED);

        mockMvc.perform(put("/api/applications/" + app.getId())
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Cannot transition application status")));
    }

    @Test
    @DisplayName("Should return 404 when accessing another citizen's application (anti-enumeration)")
    void accessOtherUsersApplication_shouldReturn404() throws Exception {
        SchemeApplication otherApp = schemeApplicationRepository.save(SchemeApplication.builder()
                .user(secondaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Sunita Sharma")
                .status(ApplicationStatus.PLANNING)
                .build());

        // Primary user tries to access secondary user's application
        mockMvc.perform(get("/api/applications/" + otherApp.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));

        // Primary user tries to update secondary user's application
        SchemeApplicationDto.UpdateApplicationRequest updateReq = new SchemeApplicationDto.UpdateApplicationRequest();
        updateReq.setNotes("Malicious update");

        mockMvc.perform(put("/api/applications/" + otherApp.getId())
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject associating application with another user's household member")
    void createApplication_withOtherUsersHouseholdMember_shouldReturn400() throws Exception {
        SchemeApplicationDto.CreateApplicationRequest req = new SchemeApplicationDto.CreateApplicationRequest();
        req.setSchemeId(testScheme.getId());
        req.setHouseholdMemberId(secondaryMember.getId()); // Belongs to secondaryUser!
        req.setStatus(ApplicationStatus.PLANNING);

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Household member not found or does not belong to your account")));
    }

    @Test
    @DisplayName("Should reject duplicate active tracking record for the same applicant and scheme")
    void createApplication_duplicateActiveRecord_shouldReturn400() throws Exception {
        SchemeApplicationDto.CreateApplicationRequest req = new SchemeApplicationDto.CreateApplicationRequest();
        req.setSchemeId(testScheme.getId());
        req.setStatus(ApplicationStatus.PLANNING);

        // First creation succeeds
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Second creation for same scheme & applicant should be rejected
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("An active application tracking record already exists")));
    }

    @Test
    @DisplayName("Should reject creation with nonexistent scheme ID")
    void createApplication_withNonexistentScheme_shouldReturn400() throws Exception {
        SchemeApplicationDto.CreateApplicationRequest req = new SchemeApplicationDto.CreateApplicationRequest();
        req.setSchemeId(999999L);
        req.setStatus(ApplicationStatus.PLANNING);

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Scheme not found")));
    }

    @Test
    @DisplayName("Should delete owned tracking record after explicit confirmation")
    void deleteApplication_ownedRecord_shouldSucceed() throws Exception {
        SchemeApplication app = schemeApplicationRepository.save(SchemeApplication.builder()
                .user(primaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Ramesh Kumar")
                .status(ApplicationStatus.PLANNING)
                .build());

        mockMvc.perform(delete("/api/applications/" + app.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        assertThat(schemeApplicationRepository.findById(app.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should reject deleting another citizen's tracking record")
    void deleteApplication_otherUsersRecord_shouldReturn404() throws Exception {
        SchemeApplication otherApp = schemeApplicationRepository.save(SchemeApplication.builder()
                .user(secondaryUser)
                .scheme(testScheme)
                .applicantType("SELF")
                .applicantName("Sunita Sharma")
                .status(ApplicationStatus.PLANNING)
                .build());

        mockMvc.perform(delete("/api/applications/" + otherApp.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isNotFound());

        assertThat(schemeApplicationRepository.findById(otherApp.getId())).isPresent();
    }

    @Test
    @DisplayName("Unauthenticated requests to application tracker must return 401 Unauthorized")
    void unauthenticatedRequests_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/applications/1"))
                .andExpect(status().isUnauthorized());
    }
}
