package com.theladders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theladders.model.Job;
import com.theladders.model.Location;

import java.time.LocalDate;

public record JobUploadRequest(
        String title,
        String description,
        String company,
        JobLocationRequest location,
        JobSalaryRequest salary,
        @JsonProperty("employment_type") String employmentType,
        @JsonProperty("posting_date") LocalDate postingDate,
        @JsonProperty("company_type") String companyType,
        String language,
        boolean remote
) {
    public JobUploadRequest {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }
    }

    public Job toEntity() {
        return new Job(
                title,
                description,
                company,
                new Location(location.city(), location.state(), location.country()),
                salary.value(),
                salary.currency(),
                employmentType,
                postingDate,
                companyType,
                language,
                remote
        );
    }
}
