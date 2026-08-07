package com.theladders.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SalaryPeriod {
    ANNUAL("Annual"),
    HOURLY("Hourly");

    private final String label;

    SalaryPeriod(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static SalaryPeriod fromLabel(String label) {
        for (SalaryPeriod period : values()) {
            if (period.label.equalsIgnoreCase(label)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Unknown salary period: " + label);
    }
}
