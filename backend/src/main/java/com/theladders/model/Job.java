package com.theladders.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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

    private String employmentType = "";

    private LocalDate postingDate;

    private String companyType = "";

    private String language = "";

    private boolean remote = false;

    public Job(
            String title,
            String description,
            String company,
            Location location,
            Long salaryValue,
            String salaryCurrency,
            String employmentType,
            LocalDate postingDate,
            String companyType,
            String language,
            boolean remote) {
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
