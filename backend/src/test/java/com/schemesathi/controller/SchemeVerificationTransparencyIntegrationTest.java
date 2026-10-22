package com.schemesathi.controller;

import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.Scheme.PublicationStatus;
import com.schemesathi.entity.Scheme.VerificationStatus;
import com.schemesathi.repository.SchemeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SchemeVerificationTransparencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SchemeRepository schemeRepository;

    @Test
    @DisplayName("1. Public scheme listing returns actual verificationStatus for VERIFIED schemes")
    void testPublicSchemesListing_returnsVerifiedStatus() throws Exception {
        Scheme verifiedScheme = Scheme.builder()
                .name("Transparency Test Verified Scheme")
                .description("Verified scheme for testing public transparency")
                .governmentAuthority("Ministry of Rural Development")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Social Security")
                .publicationStatus(PublicationStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.VERIFIED)
                .officialUrl("https://rural.nic.in/scheme")
                .sourceUrl("https://rural.nic.in/gazette-2024.pdf")
                .verificationNotes("Affirmative gazette order review completed")
                .verificationDate(LocalDate.of(2024, 7, 1))
                .active(true)
                .build();
        verifiedScheme = schemeRepository.saveAndFlush(verifiedScheme);

        mockMvc.perform(get("/api/schemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == " + verifiedScheme.getId() + ")].verificationStatus")
                        .value(hasItem("VERIFIED")))
                .andExpect(jsonPath("$.data[?(@.id == " + verifiedScheme.getId() + ")].officialUrl")
                        .value(hasItem("https://rural.nic.in/scheme")));
    }

    @Test
    @DisplayName("2. Public scheme listing returns actual verificationStatus for NEEDS_REVIEW schemes")
    void testPublicSchemesListing_returnsNeedsReviewStatus() throws Exception {
        Scheme needsReviewScheme = Scheme.builder()
                .name("Transparency Test Needs Review Scheme")
                .description("Legacy scheme requiring affirmative source verification")
                .governmentAuthority("Ministry of Agriculture")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Agriculture")
                .publicationStatus(PublicationStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.NEEDS_REVIEW)
                .officialUrl("https://agricoop.nic.in/legacy")
                .active(true)
                .build();
        needsReviewScheme = schemeRepository.saveAndFlush(needsReviewScheme);

        mockMvc.perform(get("/api/schemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == " + needsReviewScheme.getId() + ")].verificationStatus")
                        .value(hasItem("NEEDS_REVIEW")));
    }

    @Test
    @DisplayName("3. Public scheme listing returns actual verificationStatus for UNVERIFIED schemes")
    void testPublicSchemesListing_returnsUnverifiedStatus() throws Exception {
        Scheme unverifiedScheme = Scheme.builder()
                .name("Transparency Test Unverified Scheme")
                .description("Unverified scheme awaiting documentation")
                .governmentAuthority("Department of Education")
                .scope(Scheme.SchemeScope.STATE)
                .state("Telangana")
                .benefitCategory("Education")
                .publicationStatus(PublicationStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.UNVERIFIED)
                .officialUrl("https://education.telangana.gov.in/pilot")
                .active(true)
                .build();
        unverifiedScheme = schemeRepository.saveAndFlush(unverifiedScheme);

        mockMvc.perform(get("/api/schemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == " + unverifiedScheme.getId() + ")].verificationStatus")
                        .value(hasItem("UNVERIFIED")));
    }

    @Test
    @DisplayName("4. Scheme detail endpoint returns VERIFIED status and audit metadata")
    void testSchemeDetail_verifiedStatusAndMetadata() throws Exception {
        Scheme scheme = Scheme.builder()
                .name("Detailed Verified Scheme")
                .description("Comprehensive verified program")
                .governmentAuthority("Ministry of Finance")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Insurance")
                .publicationStatus(PublicationStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.VERIFIED)
                .officialUrl("https://financialservices.gov.in")
                .sourceUrl("https://egazette.gov.in/gazette2024.pdf")
                .verificationNotes("Reviewed against Official Gazette Notification No. 891")
                .verificationDate(LocalDate.of(2024, 8, 15))
                .active(true)
                .build();
        scheme = schemeRepository.saveAndFlush(scheme);

        mockMvc.perform(get("/api/schemes/" + scheme.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(scheme.getId()))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verificationDate").value("2024-08-15"))
                .andExpect(jsonPath("$.data.sourceUrl").value("https://egazette.gov.in/gazette2024.pdf"))
                .andExpect(jsonPath("$.data.officialUrl").value("https://financialservices.gov.in"));
    }

    @Test
    @DisplayName("5. Scheme detail endpoint returns NEEDS_REVIEW status without false verification claim")
    void testSchemeDetail_needsReviewStatus() throws Exception {
        Scheme scheme = Scheme.builder()
                .name("Detailed Needs Review Scheme")
                .description("Published scheme undergoing review")
                .governmentAuthority("State Government")
                .scope(Scheme.SchemeScope.STATE)
                .state("Telangana")
                .benefitCategory("Housing")
                .publicationStatus(PublicationStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.NEEDS_REVIEW)
                .officialUrl("https://housing.telangana.gov.in")
                .active(true)
                .build();
        scheme = schemeRepository.saveAndFlush(scheme);

        mockMvc.perform(get("/api/schemes/" + scheme.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(scheme.getId()))
                .andExpect(jsonPath("$.data.verificationStatus").value("NEEDS_REVIEW"))
                .andExpect(jsonPath("$.data.verificationStatus").value(not("VERIFIED")));
    }

    @Autowired
    private com.schemesathi.service.SchemeService schemeService;

    @Test
    @DisplayName("6. Missing verification metadata safely defaults to UNVERIFIED and never produces false VERIFIED")
    void testSchemeDetail_missingMetadataSafelyDefaultsToUnverified() {
        Scheme scheme = Scheme.builder()
                .id(9999L)
                .name("In-Memory Scheme With Null Verification Field")
                .description("Testing fallback when status is null in entity")
                .governmentAuthority("Ministry of Health")
                .scope(Scheme.SchemeScope.CENTRAL)
                .benefitCategory("Health")
                .publicationStatus(PublicationStatus.PUBLISHED)
                .officialUrl("https://mohfw.gov.in")
                .active(true)
                .build();
        // Force verificationStatus to null directly to simulate legacy/missing field
        scheme.setVerificationStatus(null);

        com.schemesathi.dto.SchemeDto dto = schemeService.toDto(scheme, null);
        org.assertj.core.api.Assertions.assertThat(dto.getVerificationStatus()).isEqualTo("UNVERIFIED");
        org.assertj.core.api.Assertions.assertThat(dto.getVerificationStatus()).isNotEqualTo("VERIFIED");
    }
}
