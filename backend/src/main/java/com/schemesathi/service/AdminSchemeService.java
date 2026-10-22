package com.schemesathi.service;

import com.schemesathi.dto.AdminSchemeDto;
import com.schemesathi.dto.AdminSchemeDto.AdminEligibilityRuleDto;
import com.schemesathi.dto.AdminSchemeDto.AdminRequiredDocumentDto;
import com.schemesathi.dto.AdminSchemeStatsDto;
import com.schemesathi.dto.AdminVerificationRequestDto;
import com.schemesathi.entity.EligibilityRule;
import com.schemesathi.entity.RequiredDocument;
import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.Scheme.PublicationStatus;
import com.schemesathi.entity.Scheme.SchemeScope;
import com.schemesathi.entity.Scheme.VerificationStatus;
import com.schemesathi.entity.User;
import com.schemesathi.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AdminSchemeService {

    private final SchemeRepository schemeRepository;
    private final EligibilityRuleValidator eligibilityRuleValidator;

    public AdminSchemeService(SchemeRepository schemeRepository,
                              EligibilityRuleValidator eligibilityRuleValidator) {
        this.schemeRepository = schemeRepository;
        this.eligibilityRuleValidator = eligibilityRuleValidator;
    }

    @Transactional(readOnly = true)
    public List<AdminSchemeDto> getSchemes(String search, String pubStatus, String verStatus, String category, String scope) {
        PublicationStatus pubEnum = parsePublicationStatus(pubStatus);
        VerificationStatus verEnum = parseVerificationStatus(verStatus);
        SchemeScope scopeEnum = parseScope(scope);

        return schemeRepository.findAllAdmin(
                search != null && !search.trim().isEmpty() ? search.trim() : null,
                pubEnum,
                verEnum,
                category != null && !category.trim().isEmpty() ? category.trim() : null,
                scopeEnum
        ).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminSchemeDto getSchemeById(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with ID: " + id));
        return toDto(scheme);
    }

    @Transactional
    public AdminSchemeDto createScheme(AdminSchemeDto dto, User adminUser) {
        if (dto.getEligibilityRules() != null && !dto.getEligibilityRules().isEmpty()) {
            eligibilityRuleValidator.validateRules(dto.getEligibilityRules());
        }

        PublicationStatus pubStatus = parsePublicationStatus(dto.getPublicationStatus());
        if (pubStatus == null) {
            pubStatus = PublicationStatus.DRAFT;
        }

        VerificationStatus verStatus = parseVerificationStatus(dto.getVerificationStatus());
        if (verStatus == null) {
            verStatus = VerificationStatus.UNVERIFIED;
        }

        SchemeScope scope = parseScope(dto.getScope());
        if (scope == null) {
            scope = SchemeScope.CENTRAL;
        }

        LocalDate verDate = dto.getVerificationDate();
        if (verStatus == VerificationStatus.VERIFIED) {
            if (dto.getVerificationNotes() == null || dto.getVerificationNotes().trim().length() < 10) {
                throw new IllegalArgumentException("Meaningful administrative review notes (at least 10 characters) are required when setting verification status to VERIFIED");
            }
            if (verDate == null) {
                verDate = LocalDate.now();
            }
        }

        Scheme scheme = Scheme.builder()
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .objective(dto.getObjective() != null ? dto.getObjective().trim() : null)
                .governmentAuthority(dto.getGovernmentAuthority() != null ? dto.getGovernmentAuthority().trim() : null)
                .scope(scope)
                .state(dto.getState() != null ? dto.getState().trim() : null)
                .benefitCategory(dto.getBenefitCategory() != null ? dto.getBenefitCategory().trim() : null)
                .benefits(dto.getBenefits() != null ? dto.getBenefits().trim() : null)
                .applicationProcess(dto.getApplicationProcess() != null ? dto.getApplicationProcess().trim() : null)
                .officialUrl(dto.getOfficialUrl() != null ? dto.getOfficialUrl().trim() : null)
                .applicationUrl(dto.getApplicationUrl() != null ? dto.getApplicationUrl().trim() : null)
                .source(dto.getSource() != null ? dto.getSource().trim() : null)
                .sourceUrl(dto.getSourceUrl() != null ? dto.getSourceUrl().trim() : dto.getOfficialUrl())
                .publicationStatus(pubStatus)
                .verificationStatus(verStatus)
                .verificationNotes(dto.getVerificationNotes() != null ? dto.getVerificationNotes().trim() : null)
                .verificationDate(verDate)
                .deadline(dto.getDeadline())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .importantWarnings(dto.getImportantWarnings() != null ? dto.getImportantWarnings().trim() : null)
                .commonRejectionReasons(dto.getCommonRejectionReasons() != null ? dto.getCommonRejectionReasons().trim() : null)
                .build();

        // Add rules
        if (dto.getEligibilityRules() != null) {
            for (AdminEligibilityRuleDto rDto : dto.getEligibilityRules()) {
                EligibilityRule rule = EligibilityRule.builder()
                        .criterionField(rDto.getCriterionField().trim())
                        .operator(rDto.getOperator().trim().toUpperCase())
                        .value(rDto.getValue() != null ? rDto.getValue().trim() : "")
                        .displayLabel(rDto.getDisplayLabel() != null ? rDto.getDisplayLabel().trim() : null)
                        .description(rDto.getDescription() != null ? rDto.getDescription().trim() : null)
                        .mandatory(rDto.getMandatory() != null ? rDto.getMandatory() : true)
                        .priority(rDto.getPriority() != null ? rDto.getPriority() : 0)
                        .build();
                scheme.addEligibilityRule(rule);
            }
        }

        // Add documents
        if (dto.getRequiredDocuments() != null) {
            for (AdminRequiredDocumentDto dDto : dto.getRequiredDocuments()) {
                RequiredDocument doc = RequiredDocument.builder()
                        .documentName(dDto.getDocumentName().trim())
                        .description(dDto.getDescription() != null ? dDto.getDescription().trim() : null)
                        .mandatory(dDto.getMandatory() != null ? dDto.getMandatory() : true)
                        .alternatives(dDto.getAlternatives() != null ? dDto.getAlternatives().trim() : null)
                        .build();
                scheme.addRequiredDocument(doc);
            }
        }

        // If trying to create as PUBLISHED directly, enforce publishing quality gates
        if (pubStatus == PublicationStatus.PUBLISHED) {
            validatePublishQuality(scheme);
        }

        Scheme saved = schemeRepository.save(scheme);
        return toDto(saved);
    }

    @Transactional
    public AdminSchemeDto updateScheme(Long id, AdminSchemeDto dto, User adminUser) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with ID: " + id));

        if (dto.getEligibilityRules() != null && !dto.getEligibilityRules().isEmpty()) {
            eligibilityRuleValidator.validateRules(dto.getEligibilityRules());
        }

        PublicationStatus targetPubStatus = parsePublicationStatus(dto.getPublicationStatus());
        if (targetPubStatus == null) {
            targetPubStatus = scheme.getPublicationStatus();
        }

        VerificationStatus targetVerStatus = parseVerificationStatus(dto.getVerificationStatus());
        if (targetVerStatus == null) {
            targetVerStatus = scheme.getVerificationStatus();
        }

        SchemeScope scope = parseScope(dto.getScope());
        if (scope == null) {
            scope = scheme.getScope();
        }

        scheme.setName(dto.getName() != null ? dto.getName().trim() : scheme.getName());
        scheme.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : scheme.getDescription());
        scheme.setObjective(dto.getObjective() != null ? dto.getObjective().trim() : scheme.getObjective());
        scheme.setGovernmentAuthority(dto.getGovernmentAuthority() != null ? dto.getGovernmentAuthority().trim() : scheme.getGovernmentAuthority());
        scheme.setScope(scope);
        scheme.setState(dto.getState() != null ? dto.getState().trim() : scheme.getState());
        scheme.setBenefitCategory(dto.getBenefitCategory() != null ? dto.getBenefitCategory().trim() : scheme.getBenefitCategory());
        scheme.setBenefits(dto.getBenefits() != null ? dto.getBenefits().trim() : scheme.getBenefits());
        scheme.setApplicationProcess(dto.getApplicationProcess() != null ? dto.getApplicationProcess().trim() : scheme.getApplicationProcess());
        scheme.setOfficialUrl(dto.getOfficialUrl() != null ? dto.getOfficialUrl().trim() : scheme.getOfficialUrl());
        scheme.setApplicationUrl(dto.getApplicationUrl() != null ? dto.getApplicationUrl().trim() : scheme.getApplicationUrl());
        scheme.setSource(dto.getSource() != null ? dto.getSource().trim() : scheme.getSource());
        scheme.setSourceUrl(dto.getSourceUrl() != null ? dto.getSourceUrl().trim() : scheme.getSourceUrl());
        scheme.setVerificationStatus(targetVerStatus);
        scheme.setVerificationNotes(dto.getVerificationNotes() != null ? dto.getVerificationNotes().trim() : scheme.getVerificationNotes());
        scheme.setDeadline(dto.getDeadline());
        if (dto.getActive() != null) {
            scheme.setActive(dto.getActive());
        }
        scheme.setImportantWarnings(dto.getImportantWarnings() != null ? dto.getImportantWarnings().trim() : scheme.getImportantWarnings());
        scheme.setCommonRejectionReasons(dto.getCommonRejectionReasons() != null ? dto.getCommonRejectionReasons().trim() : scheme.getCommonRejectionReasons());

        if (targetVerStatus == VerificationStatus.VERIFIED) {
            String notes = dto.getVerificationNotes() != null ? dto.getVerificationNotes().trim() : scheme.getVerificationNotes();
            if (notes == null || notes.trim().length() < 10) {
                throw new IllegalArgumentException("Meaningful administrative review notes (at least 10 characters) are required when setting verification status to VERIFIED");
            }
            scheme.setVerificationDate(dto.getVerificationDate() != null ? dto.getVerificationDate() : LocalDate.now());
        } else if (dto.getVerificationDate() != null) {
            scheme.setVerificationDate(dto.getVerificationDate());
        }

        // Replace eligibility rules
        if (dto.getEligibilityRules() != null) {
            scheme.getEligibilityRules().clear();
            for (AdminEligibilityRuleDto rDto : dto.getEligibilityRules()) {
                EligibilityRule rule = EligibilityRule.builder()
                        .criterionField(rDto.getCriterionField().trim())
                        .operator(rDto.getOperator().trim().toUpperCase())
                        .value(rDto.getValue() != null ? rDto.getValue().trim() : "")
                        .displayLabel(rDto.getDisplayLabel() != null ? rDto.getDisplayLabel().trim() : null)
                        .description(rDto.getDescription() != null ? rDto.getDescription().trim() : null)
                        .mandatory(rDto.getMandatory() != null ? rDto.getMandatory() : true)
                        .priority(rDto.getPriority() != null ? rDto.getPriority() : 0)
                        .build();
                scheme.addEligibilityRule(rule);
            }
        }

        // Replace required documents
        if (dto.getRequiredDocuments() != null) {
            scheme.getRequiredDocuments().clear();
            for (AdminRequiredDocumentDto dDto : dto.getRequiredDocuments()) {
                RequiredDocument doc = RequiredDocument.builder()
                        .documentName(dDto.getDocumentName().trim())
                        .description(dDto.getDescription() != null ? dDto.getDescription().trim() : null)
                        .mandatory(dDto.getMandatory() != null ? dDto.getMandatory() : true)
                        .alternatives(dDto.getAlternatives() != null ? dDto.getAlternatives().trim() : null)
                        .build();
                scheme.addRequiredDocument(doc);
            }
        }

        // Check publishing quality if scheme is or is being set to PUBLISHED
        if (targetPubStatus == PublicationStatus.PUBLISHED) {
            validatePublishQuality(scheme);
        }
        scheme.setPublicationStatus(targetPubStatus);

        Scheme saved = schemeRepository.save(scheme);
        return toDto(saved);
    }

    @Transactional
    public AdminSchemeDto verifyScheme(Long id, AdminVerificationRequestDto dto, User adminUser) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with ID: " + id));

        validateUrl(dto.getSourceUrl(), "Official source URL");

        VerificationStatus verStatus = parseVerificationStatus(dto.getVerificationStatus());
        if (verStatus == null) {
            throw new IllegalArgumentException("Invalid verification status. Must be UNVERIFIED, VERIFIED, or NEEDS_REVIEW");
        }

        if (verStatus == VerificationStatus.VERIFIED) {
            if (dto.getVerificationNotes() == null || dto.getVerificationNotes().trim().length() < 10) {
                throw new IllegalArgumentException("Meaningful administrative review notes (at least 10 characters) are required to verify a scheme");
            }
        }

        scheme.setSourceUrl(dto.getSourceUrl().trim());
        scheme.setVerificationStatus(verStatus);
        scheme.setVerificationNotes(dto.getVerificationNotes() != null ? dto.getVerificationNotes().trim() : null);
        scheme.setVerificationDate(dto.getVerificationDate() != null ? dto.getVerificationDate() : LocalDate.now());

        Scheme saved = schemeRepository.save(scheme);
        return toDto(saved);
    }

    @Transactional
    public AdminSchemeDto publishScheme(Long id, User adminUser) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with ID: " + id));

        validatePublishQuality(scheme);

        scheme.setPublicationStatus(PublicationStatus.PUBLISHED);
        scheme.setActive(true);

        Scheme saved = schemeRepository.save(scheme);
        return toDto(saved);
    }

    @Transactional
    public AdminSchemeDto archiveScheme(Long id, User adminUser) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with ID: " + id));

        scheme.setPublicationStatus(PublicationStatus.ARCHIVED);
        Scheme saved = schemeRepository.save(scheme);
        return toDto(saved);
    }

    @Transactional
    public void deleteScheme(Long id, User adminUser) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with ID: " + id));

        schemeRepository.delete(scheme);
    }

    @Transactional(readOnly = true)
    public AdminSchemeStatsDto getStats() {
        long total = schemeRepository.count();
        long published = schemeRepository.countByPublicationStatus(PublicationStatus.PUBLISHED);
        long draft = schemeRepository.countByPublicationStatus(PublicationStatus.DRAFT);
        long archived = schemeRepository.countByPublicationStatus(PublicationStatus.ARCHIVED);

        long verified = schemeRepository.countByVerificationStatus(VerificationStatus.VERIFIED);
        long needsReview = schemeRepository.countByVerificationStatus(VerificationStatus.NEEDS_REVIEW);
        long unverified = schemeRepository.countByVerificationStatus(VerificationStatus.UNVERIFIED);

        return new AdminSchemeStatsDto(total, published, draft, archived, verified, needsReview, unverified);
    }

    /**
     * Data quality validation gate: A scheme cannot be published unless:
     * 1. Official scheme name and description are present and sufficiently detailed.
     * 2. Benefit category is present.
     * 3. An official source URL, application URL, or official portal URL is provided and valid.
     * 4. Verification status is explicitly VERIFIED with a valid review date.
     * 5. At least 1 valid structured eligibility rule is present.
     */
    public void validatePublishQuality(Scheme scheme) {
        List<String> violations = new ArrayList<>();

        if (scheme.getName() == null || scheme.getName().trim().length() < 3) {
            violations.add("Scheme name must be at least 3 characters long");
        }

        if (scheme.getDescription() == null || scheme.getDescription().trim().isEmpty()) {
            violations.add("Scheme description must not be empty");
        }

        if (scheme.getBenefitCategory() == null || scheme.getBenefitCategory().trim().isEmpty()) {
            violations.add("Benefit category is required for publication");
        }

        String url = scheme.getSourceUrl() != null && !scheme.getSourceUrl().trim().isEmpty()
                ? scheme.getSourceUrl()
                : (scheme.getOfficialUrl() != null && !scheme.getOfficialUrl().trim().isEmpty()
                    ? scheme.getOfficialUrl()
                    : scheme.getApplicationUrl());

        if (url == null || url.trim().isEmpty()) {
            violations.add("An official source URL, application URL, or ministry portal URL is required to publish");
        } else {
            try {
                validateUrl(url, "Official URL");
            } catch (IllegalArgumentException e) {
                violations.add(e.getMessage());
            }
        }

        if (scheme.getVerificationStatus() != VerificationStatus.VERIFIED) {
            violations.add(String.format("Scheme source must be verified before publishing (current verification status: %s). Please review and verify the official source first.",
                    scheme.getVerificationStatus()));
        }

        if (scheme.getVerificationDate() == null) {
            violations.add("A valid source verification date is required before publication");
        }

        if (scheme.getVerificationNotes() == null || scheme.getVerificationNotes().trim().length() < 10) {
            violations.add("Meaningful administrative review notes (at least 10 characters) confirming official source verification are required before publication");
        }

        if (scheme.getEligibilityRules() == null || scheme.getEligibilityRules().isEmpty()) {
            violations.add("At least one structured eligibility rule is required before publication");
        } else {
            try {
                eligibilityRuleValidator.validateEntityRules(scheme.getEligibilityRules());
            } catch (IllegalArgumentException e) {
                violations.add("Invalid eligibility rules: " + e.getMessage());
            }
        }

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Cannot publish scheme due to data quality violations: " + String.join("; ", violations));
        }
    }

    private void validateUrl(String url, String fieldLabel) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldLabel + " cannot be empty");
        }
        String u = url.trim().toLowerCase();
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            throw new IllegalArgumentException(fieldLabel + " must start with http:// or https:// (got: " + url + ")");
        }
        try {
            URI uri = URI.create(url.trim());
            if (uri.getHost() == null || uri.getHost().trim().isEmpty()) {
                throw new IllegalArgumentException(fieldLabel + " contains an invalid host address: " + url);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldLabel + " is not a valid URL: " + url);
        }
    }

    private AdminSchemeDto toDto(Scheme s) {
        AdminSchemeDto dto = new AdminSchemeDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setDescription(s.getDescription());
        dto.setObjective(s.getObjective());
        dto.setGovernmentAuthority(s.getGovernmentAuthority());
        dto.setScope(s.getScope() != null ? s.getScope().name() : "CENTRAL");
        dto.setState(s.getState());
        dto.setBenefitCategory(s.getBenefitCategory());
        dto.setBenefits(s.getBenefits());
        dto.setApplicationProcess(s.getApplicationProcess());
        dto.setOfficialUrl(s.getOfficialUrl());
        dto.setApplicationUrl(s.getApplicationUrl());
        dto.setSource(s.getSource());
        dto.setSourceUrl(s.getSourceUrl());
        dto.setPublicationStatus(s.getPublicationStatus() != null ? s.getPublicationStatus().name() : "DRAFT");
        dto.setVerificationStatus(s.getVerificationStatus() != null ? s.getVerificationStatus().name() : "UNVERIFIED");
        dto.setVerificationNotes(s.getVerificationNotes());
        dto.setVerificationDate(s.getVerificationDate());
        dto.setDeadline(s.getDeadline());
        dto.setActive(s.getActive());
        dto.setImportantWarnings(s.getImportantWarnings());
        dto.setCommonRejectionReasons(s.getCommonRejectionReasons());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());

        if (s.getEligibilityRules() != null) {
            dto.setEligibilityRules(s.getEligibilityRules().stream().map(r -> {
                AdminEligibilityRuleDto rDto = new AdminEligibilityRuleDto();
                rDto.setId(r.getId());
                rDto.setCriterionField(r.getCriterionField());
                rDto.setOperator(r.getOperator());
                rDto.setValue(r.getValue());
                rDto.setDisplayLabel(r.getDisplayLabel());
                rDto.setDescription(r.getDescription());
                rDto.setMandatory(r.getMandatory());
                rDto.setPriority(r.getPriority());
                return rDto;
            }).collect(Collectors.toList()));
        }

        if (s.getRequiredDocuments() != null) {
            dto.setRequiredDocuments(s.getRequiredDocuments().stream().map(d -> {
                AdminRequiredDocumentDto dDto = new AdminRequiredDocumentDto();
                dDto.setId(d.getId());
                dDto.setDocumentName(d.getDocumentName());
                dDto.setDescription(d.getDescription());
                dDto.setMandatory(d.getMandatory());
                dDto.setAlternatives(d.getAlternatives());
                return dDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    private PublicationStatus parsePublicationStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        try {
            return PublicationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private VerificationStatus parseVerificationStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        try {
            return VerificationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SchemeScope parseScope(String scope) {
        if (scope == null || scope.trim().isEmpty()) return null;
        try {
            return SchemeScope.valueOf(scope.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
