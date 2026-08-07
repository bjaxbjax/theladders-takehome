package com.theladders.service;

import com.theladders.data.JobRepository;
import com.theladders.data.LocationRepository;
import com.theladders.model.Job;
import com.theladders.model.Location;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
