package com.gate.bioquest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gate.bioquest.model.TopicProgress;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final String FILE_PATH = "data/progress/topic-progress.json";
    private final ObjectMapper objectMapper;
    private List<TopicProgress> progressList = new ArrayList<>();

    public ProgressService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        loadProgress();
    }

    public void loadProgress() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                progressList = objectMapper.readValue(file, new TypeReference<List<TopicProgress>>() {});
            } catch (IOException e) {
                System.err.println("Error loading progress: " + e.getMessage());
                progressList = new ArrayList<>();
            }
        } else {
            progressList = new ArrayList<>();
        }
    }

    public void saveProgress() {
        File file = new File(FILE_PATH);
        try {
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, progressList);
        } catch (IOException e) {
            System.err.println("Error saving progress: " + e.getMessage());
        }
    }

    public void updateTopicWithConfidence(String subject, String topic, int masteryAdjustment, boolean isCorrect) {
        TopicProgress tp = getProgress(subject, topic);
        if (tp == null) {
            tp = new TopicProgress();
            tp.setSubject(subject);
            tp.setTopic(topic);
            tp.setMastery(0);
            tp.setTimesTested(0);
            tp.setAccuracy(0.0);
            progressList.add(tp);
        }

        tp.setTimesTested(tp.getTimesTested() + 1);

        // Simple moving average for accuracy
        double currentTotal = (tp.getAccuracy() / 100.0) * (tp.getTimesTested() - 1);
        double newTotal = currentTotal + (isCorrect ? 1.0 : 0.0);
        tp.setAccuracy((newTotal / tp.getTimesTested()) * 100.0);

        tp.setLastReviewed(LocalDate.now());

        updateMasteryAndNextReviewWithAdjustment(tp, masteryAdjustment);
        updateStatusAndPriority(tp);

        saveProgress();
    }

    private void updateMasteryAndNextReviewWithAdjustment(TopicProgress tp, int masteryAdjustment) {
        int currentMastery = tp.getMastery();
        currentMastery += masteryAdjustment;

        // Clamp mastery
        currentMastery = Math.max(0, Math.min(100, currentMastery));
        tp.setMastery(currentMastery);

        // Adjust next review based on new mastery
        if (currentMastery > 80) {
            tp.setNextReview(LocalDate.now().plusDays(7));
        } else if (currentMastery > 50) {
            tp.setNextReview(LocalDate.now().plusDays(3));
        } else {
            tp.setNextReview(LocalDate.now().plusDays(1));
        }
    }

    private void updateStatusAndPriority(TopicProgress tp) {
        int mastery = tp.getMastery();
        if (mastery <= 20) {
            tp.setStatus("NEW");
        } else if (mastery <= 40) {
            tp.setStatus("LEARNING");
        } else if (mastery <= 60) {
            tp.setStatus("FAMILIAR");
        } else if (mastery <= 80) {
            tp.setStatus("STRONG");
        } else {
            tp.setStatus("MASTERED");
        }

        calculatePriority(tp);
    }

    private void calculatePriority(TopicProgress tp) {
        // Priority = (100 - mastery) + days overdue
        int masteryFactor = 100 - tp.getMastery();
        long daysOverdue = 0;

        if (tp.getNextReview() != null && LocalDate.now().isAfter(tp.getNextReview())) {
            daysOverdue = ChronoUnit.DAYS.between(tp.getNextReview(), LocalDate.now());
        }

        tp.setRevisionPriority(masteryFactor + (int) daysOverdue);
    }

    public List<TopicProgress> findWeakTopics() {
        return progressList.stream()
                .filter(tp -> tp.getMastery() <= 40)
                .collect(Collectors.toList());
    }

    public List<TopicProgress> findDueTopics() {
        LocalDate today = LocalDate.now();
        return progressList.stream()
                .filter(tp -> tp.getNextReview() != null && !tp.getNextReview().isAfter(today))
                .collect(Collectors.toList());
    }

    public TopicProgress getProgress(String subject, String topic) {
        return progressList.stream()
                .filter(tp -> tp.getSubject().equalsIgnoreCase(subject) && tp.getTopic().equalsIgnoreCase(topic))
                .findFirst()
                .orElse(null);
    }

    public List<TopicProgress> getAllProgress() {
        return progressList;
    }
}
