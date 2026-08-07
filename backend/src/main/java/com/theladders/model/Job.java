package com.theladders.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title = "";

    private String description;

    private String company = "";

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "location_id")
    private Location location;

    private Long salaryValue = 0L;

    private String salaryCurrency = "";

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private LocalDate postingDate;

    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    private String language = "";

    private Boolean remote = false;

    public Job(
            String title,
            String description,
            String company,
            Location location,
            Long salaryValue,
            String salaryCurrency,
            EmploymentType employmentType,
            LocalDate postingDate,
            CompanyType companyType,
            String language,
            Boolean remote) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        } else if (company == null || company.isEmpty()) {
            throw new IllegalArgumentException("company cannot be null or empty");
        } else if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        } else if (salaryValue == null) {
            throw new IllegalArgumentException("salaryValue cannot be null");
        } else if (salaryCurrency == null) {
            throw new IllegalArgumentException("salaryCurrency cannot be null");
        } else if (employmentType == null) {
            throw new IllegalArgumentException("employmentType cannot be null");
        } else if (postingDate == null) {
            throw new IllegalArgumentException("postingDate cannot be null");
        } else if (companyType == null) {
            throw new IllegalArgumentException("companyType cannot be null");
        } else if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language cannot be null or empty");
        } else if (remote == null) {
            throw new IllegalArgumentException("remote cannot be null");
        }
        this.title = title;
        this.description = description;
        this.company = company;
        this.location = location;
        this.salaryValue = salaryValue;
        this.salaryCurrency = salaryCurrency;
        this.employmentType = employmentType;
        this.postingDate = postingDate;
        this.companyType = companyType;
        this.language = language;
        this.remote = remote;
    }
}
