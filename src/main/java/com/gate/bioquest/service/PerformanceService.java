package com.gate.bioquest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gate.bioquest.model.EvaluationResult;
import com.gate.bioquest.model.Question;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class PerformanceService {
    private final String PERFORMANCE_FILE = "data/performance/performance.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Topic -> Stats (attempted, correct)
    private Map<String, Map<String, Integer>> performanceData = new HashMap<>();

    @PostConstruct
    public void init() {
        loadPerformanceData();
    }

    private void loadPerformanceData() {
        File file = new File(PERFORMANCE_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                performanceData = objectMapper.readValue(file, new TypeReference<Map<String, Map<String, Integer>>>(){});
            } catch (IOException e) {
                System.err.println("Could not load performance data: " + e.getMessage());
            }
        }
    }

    public void updatePerformance(EvaluationResult result) {
        for (EvaluationResult.QuestionResult qr : result.getQuestionResults()) {
            Question q = qr.getQuestion();
            if (q == null) continue;

            String topic = q.getTopic();
            performanceData.putIfAbsent(topic, new HashMap<>(Map.of("attempted", 0, "correct", 0)));

            boolean isAttempted = qr.getUserResponse() != null && !qr.getUserResponse().toString().trim().isEmpty() && !(qr.getUserResponse() instanceof java.util.List && ((java.util.List<?>)qr.getUserResponse()).isEmpty());

            if (isAttempted) {
                Map<String, Integer> stats = performanceData.get(topic);
                stats.put("attempted", stats.get("attempted") + 1);
                if (qr.isCorrect()) {
                    stats.put("correct", stats.get("correct") + 1);
                }
            }
        }
        savePerformanceData();
    }

    private void savePerformanceData() {
        try {
            Path path = Paths.get(PERFORMANCE_FILE);
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), performanceData);
        } catch (IOException e) {
            System.err.println("Could not save performance data: " + e.getMessage());
        }
    }

    public Map<String, Map<String, Integer>> getPerformanceData() {
        return performanceData;
    }
}
