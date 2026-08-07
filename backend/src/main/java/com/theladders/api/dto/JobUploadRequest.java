package com.theladders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theladders.model.CompanyType;
import com.theladders.model.EmploymentType;
import com.theladders.model.Job;
import com.theladders.model.Location;
import com.theladders.model.SalaryPeriod;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

public record JobUploadRequest(
        String title,
        String description,
        String company,
        @JsonDeserialize(using = JobLocationRequestDeserializer.class) JobLocationRequest location,
        @JsonDeserialize(using = JobSalaryRequestDeserializer.class) JobSalaryRequest salary,
        @JsonProperty("employment_type") String employmentType,
        @JsonProperty("posting_date") LocalDate postingDate,
        @JsonProperty("company_type") String companyType,
        String language,
        Boolean remote
) {
    public Job toEntity() {
        return new Job(
                title,
                description,
                company,
                location == null ? null : new Location(location.city(), location.state(), location.country()),
                salary == null ? null : salary.value(),
                salary == null ? null : salary.currency(),
                salary == null || salary.unit() == null ? SalaryPeriod.ANNUAL : SalaryPeriod.fromLabel(salary.unit()),
                employmentType == null ? null : EmploymentType.fromLabel(employmentType),
                postingDate,
                companyType == null ? null : CompanyType.fromLabel(companyType),
                language,
                remote
        );
    }
}
