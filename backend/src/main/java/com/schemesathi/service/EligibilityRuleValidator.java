package com.schemesathi.service;

import com.schemesathi.dto.AdminSchemeDto.AdminEligibilityRuleDto;
import com.schemesathi.entity.EligibilityRule;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates eligibility rule syntax, field definitions, operator types,
 * and boundary constraints before persisting to database.
 * Directly integrates with com.schemesathi.service.EligibilityEngine.
 */
@Component
public class EligibilityRuleValidator {

    private static final Set<String> SUPPORTED_OPERATORS = Set.of(
            "EQ", "NEQ", "LT", "LTE", "GT", "GTE", "BETWEEN", "IN", "NOT_IN", "BOOL", "EXISTS"
    );

    private static final Set<String> NUMERIC_FIELDS = Set.of(
            "age", "annualincome", "income", "landarea", "familysize", "disabilitypercentage"
    );

    private static final Set<String> BOOLEAN_FIELDS = Set.of(
            "disabilitystatus", "disability", "landownership", "bplstatus", "bpl", "minoritystatus", "minority", "exserviceman"
    );

    private static final Set<String> CATEGORICAL_FIELDS = Set.of(
            "gender", "state", "district", "occupation", "educationlevel", "education",
            "category", "disabilitytype", "employmentstatus", "employment", "residencetype",
            "residence", "maritalstatus", "rationcardtype"
    );

    private static final Set<String> ALL_SUPPORTED_FIELDS;
    static {
        Set<String> all = new HashSet<>();
        all.addAll(NUMERIC_FIELDS);
        all.addAll(BOOLEAN_FIELDS);
        all.addAll(CATEGORICAL_FIELDS);
        ALL_SUPPORTED_FIELDS = Collections.unmodifiableSet(all);
    }

    public void validateRules(List<AdminEligibilityRuleDto> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            AdminEligibilityRuleDto rule = rules.get(i);
            int ruleIndex = i + 1;
            validateSingleRule(rule.getCriterionField(), rule.getOperator(), rule.getValue(), ruleIndex, errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Eligibility rule validation failed: " + String.join("; ", errors));
        }
    }

    public void validateEntityRules(List<EligibilityRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            EligibilityRule rule = rules.get(i);
            int ruleIndex = i + 1;
            validateSingleRule(rule.getCriterionField(), rule.getOperator(), rule.getValue(), ruleIndex, errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Eligibility rule validation failed: " + String.join("; ", errors));
        }
    }

    private void validateSingleRule(String field, String operator, String value, int index, List<String> errors) {
        if (field == null || field.trim().isEmpty()) {
            errors.add(String.format("Rule #%d: Criterion field cannot be empty", index));
            return;
        }

        String normalizedField = normalizeField(field);
        if (!ALL_SUPPORTED_FIELDS.contains(normalizedField)) {
            errors.add(String.format("Rule #%d: Unsupported field '%s'. Supported fields are: %s",
                    index, field, String.join(", ", ALL_SUPPORTED_FIELDS)));
            return;
        }

        if (operator == null || operator.trim().isEmpty()) {
            errors.add(String.format("Rule #%d: Operator cannot be empty for field '%s'", index, field));
            return;
        }

        String op = operator.trim().toUpperCase();
        if (!SUPPORTED_OPERATORS.contains(op)) {
            errors.add(String.format("Rule #%d: Unsupported operator '%s' for field '%s'. Allowed: %s",
                    index, operator, field, String.join(", ", SUPPORTED_OPERATORS)));
            return;
        }

        // Operator-specific & field-specific value validation
        if ("EXISTS".equals(op)) {
            // EXISTS does not strictly require a value
            return;
        }

        if (value == null || value.trim().isEmpty()) {
            errors.add(String.format("Rule #%d: Value cannot be empty for operator '%s' on field '%s'", index, op, field));
            return;
        }

        String val = value.trim();

        // Boolean fields
        if (BOOLEAN_FIELDS.contains(normalizedField)) {
            if ("BOOL".equals(op) || "EQ".equals(op) || "NEQ".equals(op)) {
                if (!isBooleanCompatible(val)) {
                    errors.add(String.format("Rule #%d: Field '%s' is a boolean flag; value '%s' must be one of: true, false, yes, no, 1, 0",
                            index, field, val));
                }
            } else {
                errors.add(String.format("Rule #%d: Operator '%s' is not valid for boolean field '%s'. Use BOOL, EQ, NEQ, or EXISTS",
                        index, op, field));
            }
            return;
        }

        // Numeric fields
        if (NUMERIC_FIELDS.contains(normalizedField)) {
            switch (op) {
                case "LT", "LTE", "GT", "GTE", "EQ", "NEQ" -> {
                    if (!isNumeric(val)) {
                        errors.add(String.format("Rule #%d: Numeric field '%s' with operator '%s' requires a valid number, got '%s'",
                                index, field, op, val));
                    } else {
                        double num = Double.parseDouble(val);
                        if ("age".equals(normalizedField) && (num < 0 || num > 130)) {
                            errors.add(String.format("Rule #%d: Age value %.1f is out of realistic bounds (0 - 130)", index, num));
                        } else if ("disabilitypercentage".equals(normalizedField) && (num < 0 || num > 100)) {
                            errors.add(String.format("Rule #%d: Disability percentage %.1f must be between 0 and 100", index, num));
                        } else if (("annualincome".equals(normalizedField) || "income".equals(normalizedField) || "landarea".equals(normalizedField) || "familysize".equals(normalizedField)) && num < 0) {
                            errors.add(String.format("Rule #%d: Field '%s' cannot be negative (got %.1f)", index, field, num));
                        }
                    }
                }
                case "BETWEEN" -> {
                    if (!isValidBetween(val)) {
                        errors.add(String.format("Rule #%d: Malformed BETWEEN value '%s' for field '%s'. Expected format 'min:max' or 'min to max' with min <= max",
                                index, val, field));
                    }
                }
                case "IN", "NOT_IN" -> {
                    String[] tokens = val.split(",");
                    for (String token : tokens) {
                        if (!isNumeric(token.trim())) {
                            errors.add(String.format("Rule #%d: In list for numeric field '%s', item '%s' is not a valid number",
                                    index, field, token.trim()));
                        }
                    }
                }
            }
            return;
        }

        // Categorical fields
        if (CATEGORICAL_FIELDS.contains(normalizedField)) {
            switch (op) {
                case "LT", "LTE", "GT", "GTE", "BETWEEN" -> {
                    errors.add(String.format("Rule #%d: Relational operator '%s' cannot be used on categorical field '%s'",
                            index, op, field));
                }
                case "IN", "NOT_IN" -> {
                    String[] tokens = val.split(",");
                    boolean hasValidToken = false;
                    for (String token : tokens) {
                        if (!token.trim().isEmpty()) {
                            hasValidToken = true;
                            break;
                        }
                    }
                    if (!hasValidToken) {
                        errors.add(String.format("Rule #%d: IN/NOT_IN value for field '%s' must contain at least one item", index, field));
                    }
                }
            }
        }
    }

    private String normalizeField(String field) {
        return field.trim().toLowerCase().replaceAll("[_\\-\\s]", "");
    }

    private boolean isBooleanCompatible(String val) {
        String s = val.toLowerCase().trim();
        return "true".equals(s) || "false".equals(s) || "yes".equals(s) || "no".equals(s)
                || "1".equals(s) || "0".equals(s) || "y".equals(s) || "n".equals(s);
    }

    private boolean isNumeric(String val) {
        if (val == null) return false;
        try {
            Double.parseDouble(val.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidBetween(String val) {
        if (val == null || val.trim().isEmpty()) return false;
        String trimmed = val.trim();
        String[] parts;
        if (trimmed.contains(":")) {
            parts = trimmed.split(":");
        } else if (trimmed.contains(" to ")) {
            parts = trimmed.split(" to ");
        } else if (trimmed.contains("..")) {
            parts = trimmed.split("\\.\\.");
        } else if (trimmed.contains("-") && trimmed.indexOf('-') > 0) {
            parts = trimmed.split("-");
        } else {
            return false;
        }

        if (parts.length != 2) return false;
        if (!isNumeric(parts[0]) || !isNumeric(parts[1])) return false;

        double low = Double.parseDouble(parts[0].trim());
        double high = Double.parseDouble(parts[1].trim());
        return low <= high;
    }
}
