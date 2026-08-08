package com.theladders.api;

import com.theladders.api.dto.JobUploadRequest;
import com.theladders.api.dto.JobUploadResult;
import com.theladders.model.Job;
import com.theladders.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/api/jobs")
    ResponseEntity<List<Job>> getJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        return ResponseEntity.ok(jobService.search(title, country, sortBy, sortDirection));
    }

    @PostMapping("/api/jobs/upload")
    ResponseEntity<List<JobUploadResult>> uploadJob(@RequestBody List<JobUploadRequest> jobs) {
        List<Job> toIngest = new ArrayList<>();
        LinkedHashMap<Integer, String> errorsByIndex = new LinkedHashMap<>();

        for (int i = 0; i < jobs.size(); i++) {
            try {
                toIngest.add(jobs.get(i).toEntity());
            } catch (Exception e) {
                errorsByIndex.put(i, rootCauseMessage(e));
            }
        }

        List<Job> saved = jobService.ingest(toIngest);

        List<JobUploadResult> results = new ArrayList<>();
        int savedIndex = 0;
        for (int i = 0; i < jobs.size(); i++) {
            if (errorsByIndex.containsKey(i)) {
                results.add(JobUploadResult.error(jobs.get(i), errorsByIndex.get(i)));
            } else {
                results.add(JobUploadResult.success(saved.get(savedIndex++)));
            }
        }

        return ResponseEntity.ok(results);
    }

    private static String rootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
