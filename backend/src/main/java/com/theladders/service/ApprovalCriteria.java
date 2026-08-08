package com.theladders.service;

import java.math.BigDecimal;

final class ApprovalCriteria {
    static final String COUNTRY_US = "USA";
    static final String COUNTRY_CA = "Canada";
    static final String LANGUAGE_EN = "English";
    static final String LANGUAGE_FR = "French";
    static final BigDecimal MINIMUM_ANNUAL_SALARY_USD = BigDecimal.valueOf(100000);
    static final BigDecimal MINIMUM_HOURLY_RATE_USD = BigDecimal.valueOf(45);

    private ApprovalCriteria() {
    }
}
