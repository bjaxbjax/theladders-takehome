package com.theladders.service;

import java.util.Set;

final class ApprovalCriteria {
    static final Set<String> US_COUNTRY_NAMES =
            Set.of("us", "usa", "united states", "united states of america");
    static final Set<String> CANADA_COUNTRY_NAMES = Set.of("ca", "can", "canada");
    static final Set<String> ENGLISH_NAMES = Set.of("en", "eng", "english");
    static final Set<String> FRENCH_NAMES = Set.of("fr", "fre", "fra", "french");
    static final long MINIMUM_ANNUAL_SALARY_USD = 100000;
    static final long MINIMUM_HOURLY_RATE_USD = 45;

    private ApprovalCriteria() {
    }
}
