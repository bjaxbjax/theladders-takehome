package com.theladders.service;

import com.theladders.storage.JobRepository;
import com.theladders.storage.LocationRepository;
import com.theladders.model.CompanyType;
import com.theladders.model.EmploymentType;
import com.theladders.model.Job;
import com.theladders.model.Location;
import com.theladders.model.SalaryPeriod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final LocationRepository locationRepository;

    public JobService(JobRepository jobRepository, LocationRepository locationRepository) {
        this.jobRepository = jobRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public List<Job> ingest(List<Job> jobs) {
        Map<LocationKey, Location> resolvedLocations = new HashMap<>();
        jobs.forEach(job -> resolveLocation(job, resolvedLocations));
        return jobRepository.saveAll(jobs);
    }

    private static final Set<String> US_COUNTRY_NAMES =
            Set.of("us", "usa", "united states", "united states of america");
    private static final Set<String> CANADA_COUNTRY_NAMES = Set.of("ca", "can", "canada");
    private static final Set<String> ENGLISH_NAMES = Set.of("en", "eng", "english");
    private static final Set<String> FRENCH_NAMES = Set.of("fr", "fre", "fra", "french");
    private static final long MINIMUM_ANNUAL_SALARY_USD = 100000;
    private static final long MINIMUM_HOURLY_RATE_USD = 45;

    private boolean approval(Job job) {
        return isEligibleLocation(job)
                && job.getEmploymentType() == EmploymentType.FULL_TIME
                && isEligibleSalary(job)
                && job.getCompanyType() != CompanyType.STAFFING_FIRM
                && isEligibleLanguage(job);
    }

    private boolean isEligibleLocation(Job job) {
        if (job.getRemote()) {
            return true;
        }
        return isCountry(job.getLocation(), US_COUNTRY_NAMES) || isCountry(job.getLocation(), CANADA_COUNTRY_NAMES);
    }

    private boolean isEligibleSalary(Job job) {
        if (!"usd".equalsIgnoreCase(job.getSalaryCurrency())) {
            return false;
        }
        if (job.getSalaryPeriod() == SalaryPeriod.HOURLY) {
            return job.getSalaryValue() > MINIMUM_HOURLY_RATE_USD;
        }
        return job.getSalaryValue() > MINIMUM_ANNUAL_SALARY_USD;
    }

    private boolean isEligibleLanguage(Job job) {
        if (matches(job.getLanguage(), ENGLISH_NAMES)) {
            return true;
        }
        return isCountry(job.getLocation(), CANADA_COUNTRY_NAMES) && matches(job.getLanguage(), FRENCH_NAMES);
    }

    private boolean isCountry(Location location, Set<String> countryNames) {
        return location != null && matches(location.getCountry(), countryNames);
    }

    private boolean matches(String value, Set<String> names) {
        return value != null && names.contains(value.trim().toLowerCase());
    }

    private void resolveLocation(Job job, Map<LocationKey, Location> resolvedLocations) {
        Location location = job.getLocation();
        if (location == null) {
            return;
        }
        LocationKey key = new LocationKey(location.getCity(), location.getState(), location.getCountry());
        Location resolved = resolvedLocations.computeIfAbsent(key, k -> {
            List<Location> existing = locationRepository
                    .findByCityAndStateAndCountry(k.city(), k.state(), k.country());
            return existing.isEmpty() ? location : existing.getFirst();
        });
        job.setLocation(resolved);
    }

    private record LocationKey(String city, String state, String country) {
    }
}
