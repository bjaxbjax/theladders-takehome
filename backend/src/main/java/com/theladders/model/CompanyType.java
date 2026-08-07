package com.theladders.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CompanyType {
    DIRECT_EMPLOYER("Direct Employer"),
    STAFFING_FIRM("Staffing Firm"),
    CONSULTING_AGENCY("Consulting Agency");

    private final String label;

    CompanyType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CompanyType fromLabel(String label) {
        for (CompanyType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown company type: " + label);
    }
}
