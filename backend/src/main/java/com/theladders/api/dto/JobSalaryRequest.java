package com.theladders.api.dto;

public record JobSalaryRequest(
        Long value,
        String currency,
        String unit
) {
}
