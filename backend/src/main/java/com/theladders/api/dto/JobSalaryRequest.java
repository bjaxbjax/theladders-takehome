package com.theladders.api.dto;

public record JobSalaryRequest(
        Long value,
        String currency
) {
    public JobSalaryRequest {
        if (value == null) {
            throw new IllegalArgumentException("JobSalaryRequest value cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("JobSalaryRequest currency cannot be null");
        }
    }
}
