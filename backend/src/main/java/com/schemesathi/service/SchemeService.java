package com.schemesathi.service;

import com.schemesathi.dto.SchemeDto;
import com.schemesathi.entity.EligibilityRule;
import com.schemesathi.entity.RequiredDocument;
import com.schemesathi.entity.Scheme;
import com.schemesathi.repository.SavedSchemeRepository;
import com.schemesathi.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class SchemeService {

    private final SchemeRepository schemeRepository;
    private final SavedSchemeRepository savedSchemeRepository;

    public SchemeService(SchemeRepository schemeRepository, SavedSchemeRepository savedSchemeRepository) {
        this.schemeRepository = schemeRepository;
        this.savedSchemeRepository = savedSchemeRepository;
    }

    @Transactional(readOnly = true)
    public List<SchemeDto> getAllSchemes(Long userId) {
        return schemeRepository.findByActiveTrue().stream()
                .map(s -> toDto(s, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SchemeDto getSchemeById(Long id, Long userId) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found"));
        return toDto(scheme, userId);
    }

    @Transactional(readOnly = true)
    public List<SchemeDto> searchSchemes(String query, Long userId) {
        return schemeRepository.searchSchemes(query).stream()
                .map(s -> toDto(s, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SchemeDto> getSchemesByCategory(String category, Long userId) {
        return schemeRepository.findByBenefitCategoryAndActiveTrue(category).stream()
                .map(s -> toDto(s, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SchemeDto> getApplicableSchemes(String state, Long userId) {
        return schemeRepository.findApplicableSchemes(state).stream()
                .map(s -> toDto(s, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SchemeDto> getSchemesByCategoryAndState(String category, String state, Long userId) {
        return schemeRepository.findByCategoryAndState(category, state).stream()
                .map(s -> toDto(s, userId))
                .collect(Collectors.toList());
    }

    private SchemeDto toDto(Scheme s, Long userId) {
        boolean saved = userId != null && savedSchemeRepository.existsByUserIdAndSchemeId(userId, s.getId());
        return SchemeDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .objective(s.getObjective())
                .governmentAuthority(s.getGovernmentAuthority())
                .scope(s.getScope().name())
                .state(s.getState())
                .benefitCategory(s.getBenefitCategory())
                .benefits(s.getBenefits())
                .applicationProcess(s.getApplicationProcess())
                .officialUrl(s.getOfficialUrl())
                .applicationUrl(s.getApplicationUrl())
                .source(s.getSource())
                .verificationDate(s.getVerificationDate())
                .deadline(s.getDeadline())
                .active(s.getActive())
                .importantWarnings(s.getImportantWarnings())
                .commonRejectionReasons(s.getCommonRejectionReasons())
                .saved(saved)
                .eligibilityRules(s.getEligibilityRules().stream()
                        .map(this::toRuleDto)
                        .collect(Collectors.toList()))
                .requiredDocuments(s.getRequiredDocuments().stream()
                        .map(this::toDocDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private SchemeDto.EligibilityRuleDto toRuleDto(EligibilityRule r) {
        return SchemeDto.EligibilityRuleDto.builder()
                .id(r.getId())
                .criterionField(r.getCriterionField())
                .operator(r.getOperator())
                .value(r.getValue())
                .displayLabel(r.getDisplayLabel())
                .description(r.getDescription())
                .mandatory(r.getMandatory())
                .build();
    }

    private SchemeDto.RequiredDocumentDto toDocDto(RequiredDocument d) {
        return SchemeDto.RequiredDocumentDto.builder()
                .id(d.getId())
                .documentName(d.getDocumentName())
                .description(d.getDescription())
                .mandatory(d.getMandatory())
                .alternatives(d.getAlternatives())
                .build();
    }
}
