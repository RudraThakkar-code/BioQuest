package com.gate.bioquest.ai;

import com.gate.bioquest.model.AttemptRecord;
import com.gate.bioquest.service.PerformanceService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromptBuilder {

    private final PerformanceService performanceService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public PromptBuilder(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    public String buildChatGptReportPrompt(String goal, String hoursAvailable) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("BIOQUEST STUDY ANALYSIS REPORT\n\n");
        prompt.append("Student Goal:\n").append(goal != null ? goal : "GATE BT").append("\n\n");
        prompt.append("Hours Available:\n").append(hoursAvailable != null ? hoursAvailable : "2 hours/day").append("\n\n");

        prompt.append("Recent Tests:\n");
        List<AttemptRecord> attempts = getRecentAttempts(5);
        for (int i = 0; i < attempts.size(); i++) {
            AttemptRecord a = attempts.get(i);
            prompt.append("Test ").append(i + 1).append(": ")
                  .append((int)a.getTotalMarks()).append("/").append(a.getTotalAttempted()).append("\n");
        }
        prompt.append("\n");

        Map<String, Map<String, Integer>> perf = performanceService.getPerformanceData();
        
        List<String> weakAreas = new ArrayList<>();
        List<String> strongAreas = new ArrayList<>();

        prompt.append("Topic Accuracy:\n");
        for (Map.Entry<String, Map<String, Integer>> entry : perf.entrySet()) {
            String topic = entry.getKey();
            Map<String, Integer> stats = entry.getValue();
            int attempted = stats.get("attempted");
            int correct = stats.get("correct");
            if (attempted > 0) {
                int accuracy = (int) (((double) correct / attempted) * 100);
                prompt.append(topic).append(": ").append(accuracy).append("%\n");
                if (accuracy < 60) weakAreas.add(topic);
                else if (accuracy >= 80) strongAreas.add(topic);
            }
        }
        prompt.append("\n");

        prompt.append("Weak Areas:\n");
        weakAreas.forEach(w -> prompt.append("- ").append(w).append("\n"));
        prompt.append("\n");

        prompt.append("Strong Areas:\n");
        strongAreas.forEach(s -> prompt.append("- ").append(s).append("\n"));
        prompt.append("\n");

        prompt.append("Request:\nCreate a 1-week study plan and recommend revision priorities for GATE BT preparation based on these metrics.\n");

        return prompt.toString();
    }

    private List<AttemptRecord> getRecentAttempts(int limit) {
        List<AttemptRecord> records = new ArrayList<>();
        try {
            Path dir = Paths.get("data/attempts");
            if (Files.exists(dir)) {
                Files.walk(dir)
                     .filter(Files::isRegularFile)
                     .filter(p -> p.toString().endsWith(".json"))
                     .forEach(p -> {
                         try {
                             records.add(objectMapper.readValue(p.toFile(), AttemptRecord.class));
                         } catch (Exception ignored) {}
                     });
            }
        } catch (IOException ignored) {}
        records.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return records.stream().limit(limit).collect(Collectors.toList());
    }
}
