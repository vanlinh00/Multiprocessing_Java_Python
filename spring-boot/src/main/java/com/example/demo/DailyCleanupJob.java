package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DailyCleanupJob {

    private final AtomicInteger totalRuns = new AtomicInteger(0);
    private final AtomicInteger failedRuns = new AtomicInteger(0);
    private volatile Instant lastRunAt;
    private volatile Integer lastExitCode;
    private volatile List<String> lastOutput = Collections.emptyList();

    // Runs every 30 seconds (30,000 milliseconds)
    @Scheduled(fixedRate = 30000)
    public void runCleanupJob() {
        executeCleanup();
    }

    // Also runs daily at 2:00 AM via cron
    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyMaintenance() {
        executeCleanup();
    }

    public synchronized JobRunSummary executeCleanup() {
        System.out.println("Job started: calling external cleanup script...");
        List<String> outputLines = new ArrayList<>();
        Integer exitCode = null;

        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "../cleanup.py");
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Cleanup Job] " + line);
                    outputLines.add(line);
                }
            }

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                failedRuns.incrementAndGet();
                throw new IllegalStateException("Cleanup process timed out after 15 seconds");
            }

            exitCode = process.exitValue();
            if (exitCode != 0) {
                failedRuns.incrementAndGet();
            }
            System.out.println("Job finished with exit code: " + exitCode);
        } catch (Exception e) {
            failedRuns.incrementAndGet();
            System.out.println("Job failed: " + e.getMessage());
            outputLines.add("ERROR: " + e.getMessage());
        } finally {
            totalRuns.incrementAndGet();
            lastRunAt = Instant.now();
            lastExitCode = exitCode;
            lastOutput = Collections.unmodifiableList(outputLines);
        }

        return getSummary();
    }

    public JobRunSummary getSummary() {
        return new JobRunSummary(
                totalRuns.get(),
                failedRuns.get(),
                lastRunAt != null ? lastRunAt.toString() : null,
                lastExitCode,
                lastOutput
        );
    }

    public record JobRunSummary(
            int totalRuns,
            int failedRuns,
            String lastRunAt,
            Integer lastExitCode,
            List<String> lastOutput
    ) {}
}
