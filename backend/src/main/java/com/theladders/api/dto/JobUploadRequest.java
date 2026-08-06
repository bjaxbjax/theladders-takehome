package com.theladders.api.dto;

import com.theladders.model.Job;
import com.theladders.model.Location;

import java.time.LocalDate;

public record JobUploadRequest(
        String title,
        String description,
        String company,
        JobLocationRequest location,
        JobSalaryRequest salary,
        String employmentType,
        LocalDate postingDate,
        String companyType,
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
