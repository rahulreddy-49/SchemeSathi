package com.schemesathi.service;

import com.schemesathi.dto.EligibilityResultDto;
import com.schemesathi.dto.EligibilityResultDto.CriterionResult;
import com.schemesathi.dto.EligibilityResultDto.CriterionResult.CriterionStatus;
import com.schemesathi.dto.EligibilityResultDto.EligibilityStatus;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.EligibilityRule;
import com.schemesathi.entity.Scheme;
import com.schemesathi.repository.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityEngineTest {

    private SchemeRepository schemeRepository;
    private EligibilityEngine engine;

    @BeforeEach
    void setUp() {
        schemeRepository = Mockito.mock(SchemeRepository.class);
        engine = new EligibilityEngine(schemeRepository);
    }

    @Test
    @DisplayName("Should evaluate boundary conditions for numerical operators correctly")
    void shouldEvaluateBoundaryConditionsCorrectly() {
        Scheme scheme = Scheme.builder()
                .name("Boundary Test Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule lteRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("annualIncome")
                .operator("LTE")
                .value("200000")
                .displayLabel("Annual Income")
                .mandatory(true)
                .build();

        EligibilityRule gteRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("age")
                .operator("GTE")
                .value("18")
                .displayLabel("Age")
                .mandatory(true)
                .build();

        EligibilityRule betweenRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("familySize")
                .operator("BETWEEN")
                .value("2:6")
                .displayLabel("Family Size")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().addAll(List.of(lteRule, gteRule, betweenRule));

        // Exact boundary match
        CitizenProfile exactBoundary = CitizenProfile.builder()
                .annualIncome(200000.0)
                .age(18)
                .familySize(2)
                .build();

        EligibilityResultDto result1 = engine.evaluate(scheme, exactBoundary);
        assertThat(result1.getOverallStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result1.getMatchScore()).isEqualTo(100.0);
        assertThat(result1.getCriteriaResults()).allMatch(c -> c.getStatus() == CriterionStatus.PASS);

        // Outside boundary by small delta
        CitizenProfile outsideBoundary = CitizenProfile.builder()
                .annualIncome(200000.01) // Fails LTE 200000
                .age(17)                 // Fails GTE 18
                .familySize(7)           // Fails BETWEEN 2:6
                .build();

        EligibilityResultDto result2 = engine.evaluate(scheme, outsideBoundary);
        assertThat(result2.getOverallStatus()).isEqualTo(EligibilityStatus.NOT_ELIGIBLE);
        assertThat(result2.getCriteriaResults()).allMatch(c -> c.getStatus() == CriterionStatus.FAIL);
    }

    @Test
    @DisplayName("Should evaluate set and categorical operators correctly")
    void shouldEvaluateSetOperatorsCorrectly() {
        Scheme scheme = Scheme.builder()
                .name("Category Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule inRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("occupation")
                .operator("IN")
                .value("Farmer,Agriculture,Artisan")
                .mandatory(true)
                .build();

        EligibilityRule boolRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("landOwnership")
                .operator("BOOL")
                .value("true")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().addAll(List.of(inRule, boolRule));

        CitizenProfile matchingProfile = CitizenProfile.builder()
                .occupation("Farmer")
                .landOwnership(true)
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, matchingProfile);
        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);

        CitizenProfile nonMatchingProfile = CitizenProfile.builder()
                .occupation("Software Engineer")
                .landOwnership(true)
                .build();

        EligibilityResultDto nonMatchingResult = engine.evaluate(scheme, nonMatchingProfile);
        assertThat(nonMatchingResult.getOverallStatus()).isEqualTo(EligibilityStatus.NOT_ELIGIBLE);
    }

    @Test
    @DisplayName("Missing criteria should be marked MISSING and not assumed eligible")
    void shouldHandleMissingCriteriaSafely() {
        Scheme scheme = Scheme.builder()
                .name("Missing Info Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule rule1 = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("age")
                .operator("GTE")
                .value("18")
                .mandatory(true)
                .build();

        EligibilityRule rule2 = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("annualIncome")
                .operator("LTE")
                .value("100000")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().addAll(List.of(rule1, rule2));

        // Profile provides age but omits income
        CitizenProfile partialProfile = CitizenProfile.builder()
                .age(25)
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, partialProfile);

        // Must NOT claim ELIGIBLE
        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.PROBABLY_ELIGIBLE);
        assertThat(result.getMatchScore()).isEqualTo(50.0);
        assertThat(result.getMissingInformation()).contains("Annual Income");

        CriterionResult incomeResult = result.getCriteriaResults().stream()
                .filter(c -> "annualIncome".equals(c.getCriterionName()))
                .findFirst()
                .orElseThrow();
        assertThat(incomeResult.getStatus()).isEqualTo(CriterionStatus.MISSING);
    }

    @Test
    @DisplayName("Should normalize field names with underscores, hyphens, and mixed casing")
    void shouldNormalizeFieldNames() {
        Scheme scheme = Scheme.builder()
                .name("Normalization Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule ruleWithUnderscore = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("annual_income")
                .operator("LTE")
                .value("250000")
                .mandatory(true)
                .build();

        EligibilityRule ruleWithHyphen = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("land-ownership")
                .operator("BOOL")
                .value("true")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().addAll(List.of(ruleWithUnderscore, ruleWithHyphen));

        CitizenProfile profile = CitizenProfile.builder()
                .annualIncome(150000.0)
                .landOwnership(true)
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, profile);
        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getCriteriaResults()).allMatch(c -> c.getStatus() == CriterionStatus.PASS);
    }

    @Test
    @DisplayName("Whitespace-only and blank string profile fields must be treated as MISSING, not evaluated or falsely passed")
    void shouldTreatWhitespaceStringsAsMissing() {
        Scheme scheme = Scheme.builder()
                .name("Whitespace Test Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule neqRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("occupation")
                .operator("NEQ")
                .value("Student")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().add(neqRule);

        // Profile with blank whitespace string for occupation
        CitizenProfile profile = CitizenProfile.builder()
                .occupation("   ")
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, profile);

        // Must NOT pass NEQ rule as eligible
        assertThat(result.getOverallStatus()).isNotEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getCriteriaResults().get(0).getStatus()).isEqualTo(CriterionStatus.MISSING);
        assertThat(result.getCriteriaResults().get(0).getCitizenValue()).isEqualTo("Not provided");
    }

    @Test
    @DisplayName("Unknown or unsupported operators must yield NEEDS_VERIFICATION and not falsely mark citizen as FAIL")
    void shouldHandleUnknownOperatorsSafely() {
        Scheme scheme = Scheme.builder()
                .name("Unknown Operator Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule unknownRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("occupation")
                .operator("CUSTOM_INSPECTION")
                .value("ManualCheck")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().add(unknownRule);

        CitizenProfile profile = CitizenProfile.builder()
                .occupation("Farmer")
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, profile);

        // Must yield REQUIRES_OFFICIAL_VERIFICATION, NOT NOT_ELIGIBLE
        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.REQUIRES_OFFICIAL_VERIFICATION);
        assertThat(result.getCriteriaResults().get(0).getStatus()).isEqualTo(CriterionStatus.NEEDS_VERIFICATION);
        assertThat(result.getCriteriaResults().get(0).getExplanation()).contains("unsupported operator");
    }

    @Test
    @DisplayName("Malformed BETWEEN range definitions must yield NEEDS_VERIFICATION without crashing")
    void shouldHandleMalformedBetweenRulesSafely() {
        Scheme scheme = Scheme.builder()
                .name("Malformed Range Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule malformedRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("age")
                .operator("BETWEEN")
                .value("not-a-number")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().add(malformedRule);

        CitizenProfile profile = CitizenProfile.builder()
                .age(25)
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, profile);

        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.REQUIRES_OFFICIAL_VERIFICATION);
        assertThat(result.getCriteriaResults().get(0).getStatus()).isEqualTo(CriterionStatus.NEEDS_VERIFICATION);
        assertThat(result.getCriteriaResults().get(0).getExplanation()).contains("malformed");
    }

    @Test
    @DisplayName("Reversed BETWEEN range boundaries and natural language separators should evaluate properly")
    void shouldEvaluateReversedAndNaturalLanguageBetweenRanges() {
        Scheme scheme = Scheme.builder()
                .name("Reversed Range Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule reversedRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("age")
                .operator("BETWEEN")
                .value("60:18") // reversed high:low
                .mandatory(true)
                .build();

        EligibilityRule wordRule = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("familySize")
                .operator("BETWEEN")
                .value("2 to 6") // ' to ' separator
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().addAll(List.of(reversedRule, wordRule));

        CitizenProfile profile = CitizenProfile.builder()
                .age(30)
                .familySize(4)
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, profile);
        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getCriteriaResults()).allMatch(c -> c.getStatus() == CriterionStatus.PASS);
    }

    @Test
    @DisplayName("Boolean rule evaluation should safely accept truthy string variations")
    void shouldSupportBooleanVariations() {
        Scheme scheme = Scheme.builder()
                .name("Boolean Scheme")
                .scope(Scheme.SchemeScope.CENTRAL)
                .build();

        EligibilityRule boolRuleYes = EligibilityRule.builder()
                .scheme(scheme)
                .criterionField("bplStatus")
                .operator("BOOL")
                .value("yes")
                .mandatory(true)
                .build();

        scheme.getEligibilityRules().add(boolRuleYes);

        CitizenProfile profile = CitizenProfile.builder()
                .bplStatus(true)
                .build();

        EligibilityResultDto result = engine.evaluate(scheme, profile);
        assertThat(result.getOverallStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getCriteriaResults().get(0).getStatus()).isEqualTo(CriterionStatus.PASS);
    }
}
