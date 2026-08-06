package com.theladders.api;

import com.theladders.api.dto.JobUploadRequest;
import com.theladders.model.Job;
import com.theladders.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobController {
    private final JobService jobService;
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/api/jobs/upload")
    ResponseEntity<List<Job>> uploadJob(@RequestBody List<JobUploadRequest> jobs) {
        List<Job> entities = jobs.stream()
                .map(JobUploadRequest::toEntity)
                .toList();
        List<Job> result = jobService.ingest(entities);
        return ResponseEntity.ok(result);
    }
}
