package com.schemesathi.entity;

import java.util.Set;

public enum ApplicationStatus {
    PLANNING("Planning to Apply"),
    READY_TO_APPLY("Ready to Apply"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    ACTION_REQUIRED("Action Required"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isValidTransition(ApplicationStatus target) {
        if (target == null || this == target) {
            return true;
        }

        return switch (this) {
            case PLANNING -> Set.of(READY_TO_APPLY, SUBMITTED, CANCELLED).contains(target);
            case READY_TO_APPLY -> Set.of(PLANNING, SUBMITTED, CANCELLED).contains(target);
            case SUBMITTED -> Set.of(UNDER_REVIEW, ACTION_REQUIRED, APPROVED, REJECTED, CANCELLED).contains(target);
            case UNDER_REVIEW -> Set.of(ACTION_REQUIRED, APPROVED, REJECTED, CANCELLED).contains(target);
            case ACTION_REQUIRED -> Set.of(SUBMITTED, UNDER_REVIEW, REJECTED, CANCELLED).contains(target);
            case APPROVED -> Set.of(COMPLETED, CANCELLED).contains(target);
            case REJECTED -> Set.of(PLANNING, CANCELLED).contains(target);
            case COMPLETED -> false; // Terminal state
            case CANCELLED -> Set.of(PLANNING).contains(target); // Reopening
        };
    }
}
