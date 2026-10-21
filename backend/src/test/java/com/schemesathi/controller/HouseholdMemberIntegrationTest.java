package com.schemesathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemesathi.dto.HouseholdMemberDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.HouseholdMember;
import com.schemesathi.entity.User;
import com.schemesathi.repository.CitizenProfileRepository;
import com.schemesathi.repository.HouseholdMemberRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HouseholdMemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CitizenProfileRepository citizenProfileRepository;

    @Autowired
    private HouseholdMemberRepository householdMemberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User primaryUser;
    private String primaryToken;

    private User secondaryUser;
    private String secondaryToken;

    @BeforeEach
    void setUp() {
        primaryUser = userRepository.findByEmail("household_owner@schemesathi.test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("household_owner@schemesathi.test")
                        .password(passwordEncoder.encode("Password123!"))
                        .fullName("Primary Citizen")
                        .role(User.UserRole.CITIZEN)
                        .active(true)
                        .build()));
        primaryToken = jwtTokenProvider.generateToken(primaryUser.getEmail(), primaryUser.getRole().name());

        secondaryUser = userRepository.findByEmail("other_citizen@schemesathi.test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("other_citizen@schemesathi.test")
                        .password(passwordEncoder.encode("Password123!"))
                        .fullName("Secondary Citizen")
                        .role(User.UserRole.CITIZEN)
                        .active(true)
                        .build()));
        secondaryToken = jwtTokenProvider.generateToken(secondaryUser.getEmail(), secondaryUser.getRole().name());

        // Clear existing household members for clean state
        householdMemberRepository.findByUserId(primaryUser.getId()).forEach(householdMemberRepository::delete);
        householdMemberRepository.findByUserId(secondaryUser.getId()).forEach(householdMemberRepository::delete);

        // Ensure primary user has a saved citizen profile
        CitizenProfile primaryProfile = citizenProfileRepository.findByUserId(primaryUser.getId())
                .orElseGet(() -> CitizenProfile.builder().user(primaryUser).build());
        primaryProfile.setAge(42);
        primaryProfile.setGender("Male");
        primaryProfile.setState("Telangana");
        primaryProfile.setOccupation("IT Consultant");
        primaryProfile.setAnnualIncome(950000.0);
        primaryProfile.setLandOwnership(false);
        citizenProfileRepository.save(primaryProfile);
    }

    @Test
    @DisplayName("1. Should create a household member successfully for authenticated user")
    void shouldCreateHouseholdMember() throws Exception {
        HouseholdMemberDto dto = HouseholdMemberDto.builder()
                .fullName("Lakshmi Reddy")
                .relationship("Spouse")
                .age(38)
                .gender("Female")
                .state("Telangana")
                .occupation("Farmer")
                .annualIncome(120000.0)
                .landOwnership(true)
                .landArea(2.0)
                .category("OBC")
                .build();

        mockMvc.perform(post("/api/household")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Lakshmi Reddy"))
                .andExpect(jsonPath("$.data.relationship").value("Spouse"))
                .andExpect(jsonPath("$.data.occupation").value("Farmer"))
                .andExpect(jsonPath("$.data.id").isNumber());

        assertThat(householdMemberRepository.findByUserId(primaryUser.getId())).hasSize(1);
    }

    @Test
    @DisplayName("2. Should list only members owned by the authenticated user (cross-tenant isolation)")
    void shouldListOnlyOwnedMembers() throws Exception {
        // Primary user adds a member
        HouseholdMember primaryMember = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Primary Child")
                .relationship("Child")
                .age(12)
                .gender("Female")
                .build());

        // Secondary user adds a member
        HouseholdMember secondaryMember = householdMemberRepository.save(HouseholdMember.builder()
                .user(secondaryUser)
                .fullName("Secondary Spouse")
                .relationship("Spouse")
                .age(35)
                .gender("Female")
                .build());

        // Primary user requests their list
        mockMvc.perform(get("/api/household")
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].fullName").value("Primary Child"))
                .andExpect(jsonPath("$.data[0].id").value(primaryMember.getId()));

        // Secondary user requests their list
        mockMvc.perform(get("/api/household")
                        .header("Authorization", "Bearer " + secondaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].fullName").value("Secondary Spouse"))
                .andExpect(jsonPath("$.data[0].id").value(secondaryMember.getId()));
    }

    @Test
    @DisplayName("3. Should retrieve and edit an owned household member")
    void shouldRetrieveAndEditOwnedMember() throws Exception {
        HouseholdMember member = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Sunita Devi")
                .relationship("Spouse")
                .age(36)
                .gender("Female")
                .occupation("Homemaker")
                .build());

        // Retrieve
        mockMvc.perform(get("/api/household/" + member.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Sunita Devi"));

        // Edit
        HouseholdMemberDto updateDto = HouseholdMemberDto.builder()
                .fullName("Sunita Devi")
                .relationship("Spouse")
                .age(37)
                .gender("Female")
                .occupation("Artisan")
                .annualIncome(60000.0)
                .build();

        mockMvc.perform(put("/api/household/" + member.getId())
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.age").value(37))
                .andExpect(jsonPath("$.data.occupation").value("Artisan"));

        HouseholdMember updated = householdMemberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getAge()).isEqualTo(37);
        assertThat(updated.getOccupation()).isEqualTo("Artisan");
    }

    @Test
    @DisplayName("4. Should delete an owned household member")
    void shouldDeleteOwnedMember() throws Exception {
        HouseholdMember member = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Elder Parent")
                .relationship("Parent")
                .age(68)
                .gender("Male")
                .build());

        mockMvc.perform(delete("/api/household/" + member.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(householdMemberRepository.findById(member.getId())).isEmpty();
    }

    @Test
    @DisplayName("5. Reject cross-account access when attempting to read, update, or delete another user's member")
    void shouldRejectCrossAccountAccess() throws Exception {
        HouseholdMember primaryMember = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Confidential Member")
                .relationship("Spouse")
                .age(30)
                .gender("Female")
                .build());

        // Secondary user attempts to GET
        mockMvc.perform(get("/api/household/" + primaryMember.getId())
                        .header("Authorization", "Bearer " + secondaryToken))
                .andExpect(status().isNotFound());

        // Secondary user attempts to PUT
        HouseholdMemberDto tamperDto = HouseholdMemberDto.builder()
                .fullName("Hacked Name")
                .relationship("Spouse")
                .build();

        mockMvc.perform(put("/api/household/" + primaryMember.getId())
                        .header("Authorization", "Bearer " + secondaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tamperDto)))
                .andExpect(status().isBadRequest());

        // Secondary user attempts to DELETE
        mockMvc.perform(delete("/api/household/" + primaryMember.getId())
                        .header("Authorization", "Bearer " + secondaryToken))
                .andExpect(status().isNotFound());

        // Verify primary record was NOT altered or deleted
        HouseholdMember untouched = householdMemberRepository.findById(primaryMember.getId()).orElseThrow();
        assertThat(untouched.getFullName()).isEqualTo("Confidential Member");
    }

    @Test
    @DisplayName("6. Reject invalid household member profile values (Bean Validation)")
    void shouldRejectInvalidMemberValues() throws Exception {
        // Missing name and relationship
        HouseholdMemberDto emptyDto = HouseholdMemberDto.builder()
                .age(-5) // Invalid
                .annualIncome(-200.0) // Invalid
                .build();

        mockMvc.perform(post("/api/household")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("7. Preserving the account holder's existing profile when household members are modified")
    void shouldPreserveAccountHolderProfile() throws Exception {
        // Primary user profile has income 950000, occupation "IT Consultant"
        HouseholdMemberDto childDto = HouseholdMemberDto.builder()
                .fullName("Aarav")
                .relationship("Child")
                .age(14)
                .gender("Male")
                .occupation("Student")
                .annualIncome(0.0)
                .build();

        mockMvc.perform(post("/api/household")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(childDto)))
                .andExpect(status().isCreated());

        CitizenProfile primaryProfile = citizenProfileRepository.findByUserId(primaryUser.getId()).orElseThrow();
        assertThat(primaryProfile.getOccupation()).isEqualTo("IT Consultant");
        assertThat(primaryProfile.getAnnualIncome()).isEqualTo(950000.0);
        assertThat(primaryProfile.getAge()).isEqualTo(42);
    }

    @Test
    @DisplayName("8. Evaluating eligibility for selected household member via dedicated and query endpoints")
    void shouldEvaluateEligibilityForHouseholdMember() throws Exception {
        // Primary user is IT consultant with high income (not eligible for PM-KISAN)
        // Primary user adds a farmer spouse with low income and land ownership
        HouseholdMember farmerSpouse = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Sita")
                .relationship("Spouse")
                .age(35)
                .gender("Female")
                .state("Telangana")
                .occupation("Farmer")
                .annualIncome(150000.0)
                .landOwnership(true)
                .landArea(2.5)
                .category("OBC")
                .build());

        // Evaluate via dedicated household endpoint
        mockMvc.perform(get("/api/household/" + farmerSpouse.getId() + "/eligibility/all")
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.schemeName =~ /.*PM-KISAN.*/)].overallStatus").value(hasItem("ELIGIBLE")));

        // Evaluate via extended scheme endpoint with memberId query parameter
        mockMvc.perform(get("/api/schemes/eligibility/all?memberId=" + farmerSpouse.getId())
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.schemeName =~ /.*PM-KISAN.*/)].overallStatus").value(hasItem("ELIGIBLE")));
    }

    @Test
    @DisplayName("9. Temporary checks and evaluations do not alter saved household member records")
    void shouldNotAlterSavedHouseholdMemberOnEligibilityCheck() throws Exception {
        HouseholdMember member = householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Rajesh")
                .relationship("Brother")
                .age(28)
                .gender("Male")
                .state("Telangana")
                .occupation("Artisan")
                .annualIncome(80000.0)
                .build());

        mockMvc.perform(get("/api/household/" + member.getId() + "/eligibility/all")
                        .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk());

        HouseholdMember fromDb = householdMemberRepository.findById(member.getId()).orElseThrow();
        assertThat(fromDb.getOccupation()).isEqualTo("Artisan");
        assertThat(fromDb.getAnnualIncome()).isEqualTo(80000.0);
    }

    @Test
    @DisplayName("10. Reject accidental duplicate household member records (same name and relationship)")
    void shouldRejectDuplicateMemberRecord() throws Exception {
        householdMemberRepository.save(HouseholdMember.builder()
                .user(primaryUser)
                .fullName("Pooja")
                .relationship("Child")
                .age(8)
                .gender("Female")
                .build());

        HouseholdMemberDto duplicateDto = HouseholdMemberDto.builder()
                .fullName("Pooja")
                .relationship("Child")
                .age(8)
                .gender("Female")
                .build();

        mockMvc.perform(post("/api/household")
                        .header("Authorization", "Bearer " + primaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("11. Unauthenticated requests to household endpoints must return HTTP 401")
    void shouldRejectUnauthenticatedHouseholdRequests() throws Exception {
        mockMvc.perform(get("/api/household"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/household")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
