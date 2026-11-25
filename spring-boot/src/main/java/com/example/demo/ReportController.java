package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final DailyCleanupJob cleanupJob;

    public ReportController(DailyCleanupJob cleanupJob) {
        this.cleanupJob = cleanupJob;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "mode", "backend-only");
    }

    @GetMapping("/report")
    public DailyCleanupJob.JobRunSummary getReport() {
        return cleanupJob.getSummary();
    }

    @PostMapping("/jobs/daily-cleanup/run")
    public DailyCleanupJob.JobRunSummary triggerCleanup() {
        return cleanupJob.executeCleanup();
    }
}
