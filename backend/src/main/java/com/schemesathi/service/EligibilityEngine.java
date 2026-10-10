package com.schemesathi.service;

import com.schemesathi.dto.EligibilityResultDto;
import com.schemesathi.dto.EligibilityResultDto.CriterionResult;
import com.schemesathi.dto.EligibilityResultDto.CriterionResult.CriterionStatus;
import com.schemesathi.dto.EligibilityResultDto.EligibilityStatus;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.EligibilityRule;
import com.schemesathi.entity.RequiredDocument;
import com.schemesathi.entity.Scheme;
import com.schemesathi.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic Eligibility Rule Engine.
 * LLM UNDERSTANDS. RULE ENGINE DECIDES.
 *
 * This engine evaluates citizen profiles against structured scheme eligibility rules
 * using deterministic logic. It never uses AI for eligibility decisions.
 */
@Service
@SuppressWarnings("null")
public class EligibilityEngine {

    private static final Logger log = LoggerFactory.getLogger(EligibilityEngine.class);

    private final SchemeRepository schemeRepository;

    public EligibilityEngine(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    /**
     * Evaluates a citizen's eligibility for a specific scheme.
     */
    public EligibilityResultDto evaluate(Scheme scheme, CitizenProfile profile) {
        List<CriterionResult> criteriaResults = new ArrayList<>();
        List<String> missingInfo = new ArrayList<>();
        int passCount = 0;
        int failCount = 0;
        int missingCount = 0;
        int totalMandatory = 0;

        for (EligibilityRule rule : scheme.getEligibilityRules()) {
            CriterionResult result = evaluateRule(rule, profile);
            criteriaResults.add(result);

            if (Boolean.TRUE.equals(rule.getMandatory())) {
                totalMandatory++;
                switch (result.getStatus()) {
                    case PASS -> passCount++;
                    case FAIL -> failCount++;
                    case MISSING -> {
                        missingCount++;
                        missingInfo.add(result.getDisplayLabel() != null ?
                            result.getDisplayLabel() : result.getCriterionName());
                    }
                    case NEEDS_VERIFICATION -> {}
                }
            }
        }

        // Determine required/missing documents
        List<String> requiredDocs = scheme.getRequiredDocuments().stream()
                .filter(d -> Boolean.TRUE.equals(d.getMandatory()))
                .map(RequiredDocument::getDocumentName)
                .collect(Collectors.toList());

        // Determine overall status
        EligibilityStatus status;
        double matchScore;

        if (totalMandatory == 0) {
            status = EligibilityStatus.REQUIRES_OFFICIAL_VERIFICATION;
            matchScore = 50.0;
        } else if (failCount > 0) {
            status = EligibilityStatus.NOT_ELIGIBLE;
            matchScore = Math.round((passCount * 100.0) / totalMandatory);
        } else if (missingCount > 0) {
            if (passCount > 0) {
                status = EligibilityStatus.PROBABLY_ELIGIBLE;
                matchScore = Math.round((passCount * 100.0) / totalMandatory);
            } else {
                status = EligibilityStatus.INSUFFICIENT_INFORMATION;
                matchScore = 0;
            }
        } else {
            status = EligibilityStatus.ELIGIBLE;
            matchScore = 100.0;
        }

        // Generate explanation
        String explanation = generateExplanation(status, passCount, failCount, missingCount, totalMandatory, scheme.getName());

        return EligibilityResultDto.builder()
                .schemeId(scheme.getId())
                .schemeName(scheme.getName())
                .overallStatus(status)
                .matchScore(matchScore)
                .criteriaResults(criteriaResults)
                .missingInformation(missingInfo)
                .requiredDocuments(requiredDocs)
                .availableDocuments(new ArrayList<>())
                .missingDocuments(requiredDocs)
                .explanation(explanation)
                .build();
    }

    /**
     * Evaluates a single eligibility rule against a citizen profile.
     */
    private CriterionResult evaluateRule(EligibilityRule rule, CitizenProfile profile) {
        String field = rule.getCriterionField();
        Object citizenValue = getProfileField(profile, field);
        String operator = rule.getOperator();
        String requiredValue = rule.getValue();

        String displayLabel = rule.getDisplayLabel() != null ? rule.getDisplayLabel() : formatFieldName(field);
        String citizenValueStr = citizenValue != null ? String.valueOf(citizenValue) : "Not provided";

        // Handle missing data
        if (citizenValue == null || citizenValueStr.isEmpty()) {
            return CriterionResult.builder()
                    .criterionName(field)
                    .displayLabel(displayLabel)
                    .citizenValue("Not provided")
                    .requiredValue(formatRequiredValue(operator, requiredValue))
                    .status(CriterionStatus.MISSING)
                    .explanation("This information is not available in your profile. Please update your profile.")
                    .build();
        }

        boolean passed = evaluateCondition(citizenValue, operator, requiredValue);

        return CriterionResult.builder()
                .criterionName(field)
                .displayLabel(displayLabel)
                .citizenValue(citizenValueStr)
                .requiredValue(formatRequiredValue(operator, requiredValue))
                .status(passed ? CriterionStatus.PASS : CriterionStatus.FAIL)
                .explanation(passed
                    ? "You meet the " + displayLabel + " requirement."
                    : "You do not meet the " + displayLabel + " requirement. Required: " + formatRequiredValue(operator, requiredValue) + ", Your value: " + citizenValueStr)
                .build();
    }

    /**
     * Evaluates a condition based on operator type.
     */
    private boolean evaluateCondition(Object citizenValue, String operator, String requiredValue) {
        try {
            return switch (operator.toUpperCase()) {
                case "EQ" -> String.valueOf(citizenValue).equalsIgnoreCase(requiredValue);
                case "NEQ" -> !String.valueOf(citizenValue).equalsIgnoreCase(requiredValue);
                case "LT" -> toDouble(citizenValue) < toDouble(requiredValue);
                case "LTE" -> toDouble(citizenValue) <= toDouble(requiredValue);
                case "GT" -> toDouble(citizenValue) > toDouble(requiredValue);
                case "GTE" -> toDouble(citizenValue) >= toDouble(requiredValue);
                case "IN" -> {
                    String val = String.valueOf(citizenValue).toLowerCase();
                    yield Arrays.stream(requiredValue.split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .anyMatch(v -> v.equals(val));
                }
                case "NOT_IN" -> {
                    String val2 = String.valueOf(citizenValue).toLowerCase();
                    yield Arrays.stream(requiredValue.split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .noneMatch(v -> v.equals(val2));
                }
                case "BETWEEN" -> {
                    String[] parts = requiredValue.split(":");
                    double cv = toDouble(citizenValue);
                    yield cv >= toDouble(parts[0]) && cv <= toDouble(parts[1]);
                }
                case "BOOL" -> {
                    boolean expected = Boolean.parseBoolean(requiredValue);
                    boolean actual = citizenValue instanceof Boolean ? (Boolean) citizenValue
                            : Boolean.parseBoolean(String.valueOf(citizenValue));
                    yield expected == actual;
                }
                case "EXISTS" -> citizenValue != null && !String.valueOf(citizenValue).isEmpty();
                default -> {
                    log.warn("Unknown operator: {}", operator);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.warn("Error evaluating rule: field={}, operator={}, value={}, error={}",
                    citizenValue, operator, requiredValue, e.getMessage());
            return false;
        }
    }

    /**
     * Extracts a profile field value by name using reflection-free approach.
     */
    private Object getProfileField(CitizenProfile profile, String field) {
        if (profile == null) return null;
        return switch (field.toLowerCase()) {
            case "age" -> profile.getAge();
            case "gender" -> profile.getGender();
            case "state" -> profile.getState();
            case "district" -> profile.getDistrict();
            case "occupation" -> profile.getOccupation();
            case "annualincome", "annual_income", "income" -> profile.getAnnualIncome();
            case "educationlevel", "education_level", "education" -> profile.getEducationLevel();
            case "category" -> profile.getCategory();
            case "disabilitystatus", "disability_status", "disability" -> profile.getDisabilityStatus();
            case "disabilitypercentage", "disability_percentage" -> profile.getDisabilityPercentage();
            case "landownership", "land_ownership" -> profile.getLandOwnership();
            case "landarea", "land_area" -> profile.getLandArea();
            case "employmentstatus", "employment_status", "employment" -> profile.getEmploymentStatus();
            case "residencetype", "residence_type", "residence" -> profile.getResidenceType();
            case "maritalstatus", "marital_status" -> profile.getMaritalStatus();
            case "familysize", "family_size" -> profile.getFamilySize();
            case "bplstatus", "bpl_status", "bpl" -> profile.getBplStatus();
            case "rationcardtype", "ration_card_type" -> profile.getRationCardType();
            case "minoritystatus", "minority_status", "minority" -> profile.getMinorityStatus();
            case "exserviceman", "ex_serviceman" -> profile.getExServiceman();
            default -> {
                log.warn("Unknown profile field: {}", field);
                yield null;
            }
        };
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(value).trim());
    }

    private String formatFieldName(String field) {
        StringBuilder sb = new StringBuilder();
        for (char c : field.toCharArray()) {
            if (Character.isUpperCase(c) && !sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(sb.isEmpty() ? Character.toUpperCase(c) : c);
        }
        return sb.toString();
    }

    private String formatRequiredValue(String operator, String value) {
        return switch (operator.toUpperCase()) {
            case "LTE" -> "≤ " + value;
            case "GTE" -> "≥ " + value;
            case "LT" -> "< " + value;
            case "GT" -> "> " + value;
            case "EQ" -> value;
            case "NEQ" -> "Not " + value;
            case "IN" -> "One of: " + value;
            case "NOT_IN" -> "Not: " + value;
            case "BETWEEN" -> value.replace(":", " to ");
            case "BOOL" -> Boolean.parseBoolean(value) ? "Yes" : "No";
            case "EXISTS" -> "Must be provided";
            default -> value;
        };
    }

    private String generateExplanation(EligibilityStatus status, int pass, int fail, int missing, int total, String schemeName) {
        return switch (status) {
            case ELIGIBLE -> String.format(
                "You meet all %d eligibility criteria for %s. Please ensure you have all required documents ready before applying.", total, schemeName);
            case PROBABLY_ELIGIBLE -> String.format(
                "You meet %d of %d verified criteria for %s. %d criteria could not be verified due to missing profile information. Update your profile for a complete assessment.", pass, total, schemeName, missing);
            case NOT_ELIGIBLE -> String.format(
                "You do not meet %d of %d criteria for %s. Review the failed criteria below for details.", fail, total, schemeName);
            case INSUFFICIENT_INFORMATION -> String.format(
                "We cannot determine your eligibility for %s because essential profile information is missing. Please complete your profile.", schemeName);
            case REQUIRES_OFFICIAL_VERIFICATION ->
                "This scheme requires official verification. Please contact the relevant government authority for eligibility confirmation.";
        };
    }

    /**
     * Evaluates eligibility for all applicable schemes for a citizen.
     */
    public List<EligibilityResultDto> evaluateAll(CitizenProfile profile) {
        String state = profile != null ? profile.getState() : null;
        List<Scheme> schemes = state != null
                ? schemeRepository.findApplicableSchemes(state)
                : schemeRepository.findByActiveTrue();

        return schemes.stream()
                .map(scheme -> evaluate(scheme, profile))
                .sorted(Comparator.comparingDouble(EligibilityResultDto::getMatchScore).reversed())
                .collect(Collectors.toList());
    }
}
