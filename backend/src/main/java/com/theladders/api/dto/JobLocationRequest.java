package com.theladders.api.dto;

public record JobLocationRequest(
        String city,
        String state,
        String country
) {
}
