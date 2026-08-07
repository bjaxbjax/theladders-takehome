package com.theladders.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmploymentType {
    FULL_TIME("Full-Time"),
    INTERNSHIP("Internship"),
    CONTRACT("Contract"),
    PART_TIME("Part-Time");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static EmploymentType fromLabel(String label) {
        for (EmploymentType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown employment type: " + label);
    }
}
