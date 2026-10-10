package com.schemesathi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "eligibility_rules")
public class EligibilityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(nullable = false, length = 50)
    private String criterionField;

    @Column(nullable = false, length = 30)
    private String operator;

    @Column(name = "criterion_value", nullable = false, length = 200)
    private String value;

    @Column(length = 200)
    private String displayLabel;

    @Column(length = 500)
    private String description;

    private Boolean mandatory = true;

    private Integer priority = 0;

    public EligibilityRule() {}

    public EligibilityRule(Long id, Scheme scheme, String criterionField, String operator, String value, String displayLabel, String description, Boolean mandatory, Integer priority) {
        this.id = id;
        this.scheme = scheme;
        this.criterionField = criterionField;
        this.operator = operator;
        this.value = value;
        this.displayLabel = displayLabel;
        this.description = description;
        this.mandatory = mandatory != null ? mandatory : true;
        this.priority = priority != null ? priority : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Scheme scheme;
        private String criterionField;
        private String operator;
        private String value;
        private String displayLabel;
        private String description;
        private Boolean mandatory = true;
        private Integer priority = 0;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder scheme(Scheme scheme) { this.scheme = scheme; return this; }
        public Builder criterionField(String criterionField) { this.criterionField = criterionField; return this; }
        public Builder operator(String operator) { this.operator = operator; return this; }
        public Builder value(String value) { this.value = value; return this; }
        public Builder displayLabel(String displayLabel) { this.displayLabel = displayLabel; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder mandatory(Boolean mandatory) { this.mandatory = mandatory; return this; }
        public Builder priority(Integer priority) { this.priority = priority; return this; }

        public EligibilityRule build() {
            return new EligibilityRule(id, scheme, criterionField, operator, value, displayLabel, description, mandatory, priority);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }

    public String getCriterionField() { return criterionField; }
    public void setCriterionField(String criterionField) { this.criterionField = criterionField; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
