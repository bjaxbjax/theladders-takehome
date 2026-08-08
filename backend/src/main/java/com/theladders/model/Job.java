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
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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

    @NonNull
    private String title = "";

    private String description;

    @NonNull
    private String company = "";

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "location_id")
    private Location location;

    @NonNull
    private BigDecimal salaryValue = BigDecimal.ZERO;

    @NonNull
    private String salaryCurrency = "";

    @NonNull
    @Enumerated(EnumType.STRING)
    private SalaryPeriod salaryPeriod;

    @NonNull
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @NonNull
    private LocalDate postingDate;

    @NonNull
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    @NonNull
    private String language = "";

    @NonNull
    private Boolean remote = false;

    private String rejectionReason;

    @Transient
    public boolean isApproved() {
        return rejectionReason == null;
    }

    public Job(
            @NonNull String title,
            String description,
            @NonNull String company,
            Location location,
            @NonNull BigDecimal salaryValue,
            @NonNull String salaryCurrency,
            @NonNull SalaryPeriod salaryPeriod,
            @NonNull EmploymentType employmentType,
            @NonNull LocalDate postingDate,
            @NonNull CompanyType companyType,
            @NonNull String language,
            @NonNull Boolean remote) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("title cannot be blank");
        } else if (company.isBlank()) {
            throw new IllegalArgumentException("company cannot be blank");
        } else if (language.isBlank()) {
            throw new IllegalArgumentException("language cannot be blank");
        } else if (salaryValue.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("salaryValue must have no more than 2 decimal places");
        }
        this.title = title;
        this.description = description;
        this.company = company;
        this.location = location;
        this.salaryValue = salaryValue;
        this.salaryCurrency = salaryCurrency;
        this.salaryPeriod = salaryPeriod;
        this.employmentType = employmentType;
        this.postingDate = postingDate;
        this.companyType = companyType;
        this.language = language;
        this.remote = remote;
    }
}
