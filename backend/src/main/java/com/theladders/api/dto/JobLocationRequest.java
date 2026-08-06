package com.theladders.api.dto;

public record JobLocationRequest(
        String city,
        String state,
        String country
) {
    public JobLocationRequest {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }
}
