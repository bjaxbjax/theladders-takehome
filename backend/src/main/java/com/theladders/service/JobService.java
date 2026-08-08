package com.theladders.service;

import com.theladders.storage.JobRepository;
import com.theladders.storage.LocationRepository;
import com.theladders.model.CompanyType;
import com.theladders.model.EmploymentType;
import com.theladders.model.Job;
import com.theladders.model.Location;
import com.theladders.model.SalaryPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.theladders.service.ApprovalCriteria.*;

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
        jobs.forEach(job -> {
            resolveLocation(job, resolvedLocations);
            job.setRejectionReason(approveOrRejectWithMessage(job));
        });
        return jobRepository.saveAll(jobs);
    }

    public Page<Job> search(String title, String country, String sortBy, String sortDirection, int page, int size) {
        Specification<Job> spec = Specification
                .where(isApproved())
                .and(titleContains(title))
                .and(hasCountry(country));
        Pageable pageable = PageRequest.of(page, size, sortFor(sortBy, sortDirection));
        return jobRepository.findAll(spec, pageable);
    }

    private Specification<Job> isApproved() {
        return (root, query, cb) -> cb.isNull(root.get("rejectionReason"));
    }

    private Specification<Job> titleContains(String title) {
        if (title == null || title.isBlank()) {
            return Specification.unrestricted();
        }
        String pattern = "%" + title.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    private Specification<Job> hasCountry(String country) {
        if (country == null || country.isBlank()) {
            return Specification.unrestricted();
        }
        String normalized = country.trim().toLowerCase();
        return (root, query, cb) -> cb.equal(cb.lower(root.join("location").get("country")), normalized);
    }

    private Sort sortFor(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.unsorted();
        }
        String property = switch (sortBy) {
            case "salary" -> "salaryValue";
            case "postingDate" -> "postingDate";
            default -> throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String approveOrRejectWithMessage(Job job) {
        if (!isEligibleLocation(job)) {
            return "Job must be remote or located in the United States or Canada";
        }
        if (job.getEmploymentType() != EmploymentType.FULL_TIME) {
            return "Job must be a full-time position";
        }
        if (!isEligibleSalary(job)) {
            return "Annual salary must be over $100,000 USD, or over $45/hour USD if paid hourly";
        }
        if (job.getCompanyType() == CompanyType.STAFFING_FIRM) {
            return "Job must not be from a staffing firm";
        }
        if (!isEligibleLanguage(job)) {
            return "Job description must be in English, or French if the job is in Canada";
        }
        return null;
    }

    private boolean isEligibleLocation(Job job) {
        if (job.getRemote()) {
            return true;
        }
        String country = job.getLocation().getCountry();
        return country.equals(COUNTRY_US) || country.equals(COUNTRY_CA);
    }

    private boolean isEligibleSalary(Job job) {
        if (!"usd".equalsIgnoreCase(job.getSalaryCurrency())) {
            return false;
        }
        if (job.getSalaryPeriod() == SalaryPeriod.HOURLY) {
            return job.getSalaryValue().compareTo(MINIMUM_HOURLY_RATE_USD) > 0;
        }
        return job.getSalaryValue().compareTo(MINIMUM_ANNUAL_SALARY_USD) > 0;
    }

    private boolean isEligibleLanguage(Job job) {
        String language =  job.getLanguage();
        if (language.equals(LANGUAGE_EN)) {
            return true;
        }
        return job.getLocation().getCountry().equals(COUNTRY_CA) && job.getLanguage().equals(LANGUAGE_FR);
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
        if (resolved.getCountry().equalsIgnoreCase("remote")) {
            resolved = null;
            job.setRemote(true);
        }
        job.setLocation(resolved);
    }

    private record LocationKey(String city, String state, String country) {
    }
}
