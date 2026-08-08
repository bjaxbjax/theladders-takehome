package com.theladders.api.dto;

import java.math.BigDecimal;

public record JobSalaryRequest(
        BigDecimal value,
        String currency,
        String unit
) {
}
