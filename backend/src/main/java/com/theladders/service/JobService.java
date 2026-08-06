package com.theladders.service;

import com.theladders.data.JobRepository;
import com.theladders.model.Job;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    private final JobRepository jobRepository;
    public JobService (JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> ingest(List<Job> jobs) {
        return jobRepository.saveAll(jobs);
    }
}
