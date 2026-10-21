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
        int needsVerificationCount = 0;
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
                    case NEEDS_VERIFICATION -> needsVerificationCount++;
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
        } else if (needsVerificationCount > 0) {
            status = EligibilityStatus.REQUIRES_OFFICIAL_VERIFICATION;
            matchScore = Math.round((passCount * 100.0) / totalMandatory);
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

        String displayLabel = rule.getDisplayLabel() != null ? rule.getDisplayLabel() : formatFieldName(field != null ? field : "Criterion");
        String citizenValueStr = citizenValue != null ? String.valueOf(citizenValue) : "Not provided";

        // Handle missing or blank data
        if (citizenValue == null || citizenValue.toString().trim().isEmpty()) {
            return CriterionResult.builder()
                    .criterionName(field)
                    .displayLabel(displayLabel)
                    .citizenValue("Not provided")
                    .requiredValue(formatRequiredValue(operator, requiredValue))
                    .status(CriterionStatus.MISSING)
                    .explanation("This information is not available in your profile. Please update your profile.")
                    .build();
        }

        // Check if operator is recognized and valid
        if (operator == null || !isSupportedOperator(operator)) {
            log.warn("Rule with unsupported or unknown operator: '{}' for field '{}'", operator, field);
            return CriterionResult.builder()
                    .criterionName(field)
                    .displayLabel(displayLabel)
                    .citizenValue(citizenValueStr)
                    .requiredValue(formatRequiredValue(operator, requiredValue))
                    .status(CriterionStatus.NEEDS_VERIFICATION)
                    .explanation("This criterion requires departmental or manual verification (unsupported operator: " + operator + ").")
                    .build();
        }

        // Handle malformed requiredValue for BETWEEN
        if ("BETWEEN".equalsIgnoreCase(operator.trim()) && !isValidBetweenValue(requiredValue)) {
            log.warn("Rule with malformed BETWEEN value: '{}' for field '{}'", requiredValue, field);
            return CriterionResult.builder()
                    .criterionName(field)
                    .displayLabel(displayLabel)
                    .citizenValue(citizenValueStr)
                    .requiredValue(requiredValue != null ? requiredValue : "")
                    .status(CriterionStatus.NEEDS_VERIFICATION)
                    .explanation("Criterion rule definition is incomplete or malformed. Official verification required.")
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
     * Checks if operator is recognized by the engine.
     */
    private boolean isSupportedOperator(String operator) {
        if (operator == null) return false;
        return switch (operator.trim().toUpperCase()) {
            case "EQ", "NEQ", "LT", "LTE", "GT", "GTE", "BETWEEN", "IN", "NOT_IN", "BOOL", "EXISTS" -> true;
            default -> false;
        };
    }

    /**
     * Validates that a BETWEEN rule has two valid numeric boundaries.
     */
    private boolean isValidBetweenValue(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        String val = value.trim();
        String[] parts;
        if (val.contains(":")) {
            parts = val.split(":");
        } else if (val.contains(" to ")) {
            parts = val.split(" to ");
        } else if (val.contains("..")) {
            parts = val.split("\\.\\.");
        } else if (val.contains("-") && val.indexOf('-') > 0) {
            parts = val.split("-");
        } else {
            return false;
        }
        if (parts.length < 2) return false;
        return isNumeric(parts[0].trim()) && isNumeric(parts[1].trim());
    }

    /**
     * Evaluates a condition based on operator type.
     */
    private boolean evaluateCondition(Object citizenValue, String operator, String requiredValue) {
        if (operator == null || requiredValue == null) return false;
        try {
            return switch (operator.trim().toUpperCase()) {
                case "EQ" -> {
                    if (citizenValue instanceof Number || isNumeric(String.valueOf(citizenValue))) {
                        if (isNumeric(requiredValue)) {
                            yield Math.abs(toDouble(citizenValue) - toDouble(requiredValue)) < 0.0001;
                        }
                    }
                    yield String.valueOf(citizenValue).trim().equalsIgnoreCase(requiredValue.trim());
                }
                case "NEQ" -> {
                    if (citizenValue instanceof Number || isNumeric(String.valueOf(citizenValue))) {
                        if (isNumeric(requiredValue)) {
                            yield Math.abs(toDouble(citizenValue) - toDouble(requiredValue)) >= 0.0001;
                        }
                    }
                    yield !String.valueOf(citizenValue).trim().equalsIgnoreCase(requiredValue.trim());
                }
                case "LT" -> toDouble(citizenValue) < toDouble(requiredValue);
                case "LTE" -> toDouble(citizenValue) <= toDouble(requiredValue);
                case "GT" -> toDouble(citizenValue) > toDouble(requiredValue);
                case "GTE" -> toDouble(citizenValue) >= toDouble(requiredValue);
                case "IN" -> {
                    String val = String.valueOf(citizenValue).trim().toLowerCase();
                    boolean isNum = isNumeric(val);
                    double numVal = isNum ? toDouble(citizenValue) : 0.0;
                    yield Arrays.stream(requiredValue.split(","))
                            .map(String::trim)
                            .anyMatch(v -> {
                                if (isNum && isNumeric(v)) {
                                    return Math.abs(numVal - toDouble(v)) < 0.0001;
                                }
                                return v.equalsIgnoreCase(val);
                            });
                }
                case "NOT_IN" -> {
                    String val2 = String.valueOf(citizenValue).trim().toLowerCase();
                    boolean isNum = isNumeric(val2);
                    double numVal = isNum ? toDouble(citizenValue) : 0.0;
                    yield Arrays.stream(requiredValue.split(","))
                            .map(String::trim)
                            .noneMatch(v -> {
                                if (isNum && isNumeric(v)) {
                                    return Math.abs(numVal - toDouble(v)) < 0.0001;
                                }
                                return v.equalsIgnoreCase(val2);
                            });
                }
                case "BETWEEN" -> {
                    String val = requiredValue.trim();
                    String[] parts;
                    if (val.contains(":")) {
                        parts = val.split(":");
                    } else if (val.contains(" to ")) {
                        parts = val.split(" to ");
                    } else if (val.contains("..")) {
                        parts = val.split("\\.\\.");
                    } else {
                        parts = val.split("-");
                    }
                    double min = toDouble(parts[0].trim());
                    double max = toDouble(parts[1].trim());
                    double low = Math.min(min, max);
                    double high = Math.max(min, max);
                    double cv = toDouble(citizenValue);
                    yield cv >= low && cv <= high;
                }
                case "BOOL" -> {
                    boolean expected = parseBooleanSafely(requiredValue);
                    boolean actual = citizenValue instanceof Boolean b ? b
                            : parseBooleanSafely(String.valueOf(citizenValue));
                    yield expected == actual;
                }
                case "EXISTS" -> citizenValue != null && !String.valueOf(citizenValue).trim().isEmpty();
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

    private boolean parseBooleanSafely(String str) {
        if (str == null) return false;
        String trimmed = str.trim().toLowerCase();
        return "true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed) || "y".equals(trimmed);
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extracts a profile field value by name using reflection-free approach.
     */
    private Object getProfileField(CitizenProfile profile, String field) {
        if (profile == null || field == null) return null;
        String normalized = field.trim().toLowerCase().replaceAll("[_\\-\\s]", "");
        return switch (normalized) {
            case "age" -> profile.getAge();
            case "gender" -> profile.getGender();
            case "state" -> profile.getState();
            case "district" -> profile.getDistrict();
            case "occupation" -> profile.getOccupation();
            case "annualincome", "income" -> profile.getAnnualIncome();
            case "educationlevel", "education" -> profile.getEducationLevel();
            case "category" -> profile.getCategory();
            case "disabilitystatus", "disability" -> profile.getDisabilityStatus();
            case "disabilitypercentage" -> profile.getDisabilityPercentage();
            case "disabilitytype" -> profile.getDisabilityType();
            case "landownership" -> profile.getLandOwnership();
            case "landarea" -> profile.getLandArea();
            case "employmentstatus", "employment" -> profile.getEmploymentStatus();
            case "residencetype", "residence" -> profile.getResidenceType();
            case "maritalstatus" -> profile.getMaritalStatus();
            case "familysize" -> profile.getFamilySize();
            case "bplstatus", "bpl" -> profile.getBplStatus();
            case "rationcardtype" -> profile.getRationCardType();
            case "minoritystatus", "minority" -> profile.getMinorityStatus();
            case "exserviceman" -> profile.getExServiceman();
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
        if (field == null || field.trim().isEmpty()) return "Criterion";
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
        if (operator == null) return value != null ? value : "";
        if (value == null) return "";
        return switch (operator.trim().toUpperCase()) {
            case "LTE" -> "≤ " + value;
            case "GTE" -> "≥ " + value;
            case "LT" -> "< " + value;
            case "GT" -> "> " + value;
            case "EQ" -> value;
            case "NEQ" -> "Not " + value;
            case "IN" -> "One of: " + value;
            case "NOT_IN" -> "Not: " + value;
            case "BETWEEN" -> value.replace(":", " to ").replace("..", " to ");
            case "BOOL" -> parseBooleanSafely(value) ? "Yes" : "No";
            case "EXISTS" -> "Must be provided";
            default -> value;
        };
    }

    private String generateExplanation(EligibilityStatus status, int pass, int fail, int missing, int total, String schemeName) {
        return switch (status) {
            case ELIGIBLE -> String.format(
                "You meet all %d evaluated statutory criteria for %s based on submitted information. Official verification by competent government authorities is required prior to benefit disbursement.", total, schemeName);
            case PROBABLY_ELIGIBLE -> String.format(
                "You meet %d of %d verified criteria for %s. %d criteria could not be verified due to missing information. Complete all required fields for a definitive check.", pass, total, schemeName, missing);
            case NOT_ELIGIBLE -> String.format(
                "You do not meet %d of %d statutory criteria for %s. Review the failed criteria below for specific requirements.", fail, total, schemeName);
            case INSUFFICIENT_INFORMATION -> String.format(
                "Eligibility for %s cannot be determined because essential criteria values were not provided. Please provide the required information.", schemeName);
            case REQUIRES_OFFICIAL_VERIFICATION ->
                "This scheme requires official in-person or departmental verification. Please consult the competent authority or official portal.";
        };
    }

    /**
     * Evaluates eligibility for all applicable schemes for a citizen.
     */
    public List<EligibilityResultDto> evaluateAll(CitizenProfile profile) {
        String state = (profile != null && profile.getState() != null && !profile.getState().trim().isEmpty())
                ? profile.getState().trim()
                : null;
        List<Scheme> schemes = state != null
                ? schemeRepository.findApplicableSchemes(state)
                : schemeRepository.findByActiveTrue();

        return schemes.stream()
                .map(scheme -> evaluate(scheme, profile))
                .sorted(Comparator.comparingDouble(EligibilityResultDto::getMatchScore).reversed())
                .collect(Collectors.toList());
    }
}
