package com.gate.bioquest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gate.bioquest.model.QuestionProgress;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionProgressService {

    private final String FILE_PATH = "data/progress/question-progress.json";
    private final ObjectMapper objectMapper;
    private List<QuestionProgress> progressList = new ArrayList<>();

    public QuestionProgressService() {
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
                progressList = objectMapper.readValue(file, new TypeReference<List<QuestionProgress>>() {});
            } catch (IOException e) {
                System.err.println("Error loading question progress: " + e.getMessage());
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
            System.err.println("Error saving question progress: " + e.getMessage());
        }
    }

    public void updateProgress(Long questionId, boolean isCorrect) {
        QuestionProgress qp = getProgress(questionId);
        if (qp == null) {
            qp = new QuestionProgress();
            qp.setQuestionId(questionId);
            qp.setAttempts(0);
            qp.setCorrectAttempts(0);
            qp.setIncorrectAttempts(0);
            qp.setDifficultyScore(1);
            progressList.add(qp);
        }

        qp.setAttempts(qp.getAttempts() + 1);
        qp.setLastSeen(LocalDate.now());

        if (isCorrect) {
            qp.setCorrectAttempts(qp.getCorrectAttempts() + 1);
            qp.setDifficultyScore(Math.max(1, qp.getDifficultyScore() - 1));
        } else {
            qp.setIncorrectAttempts(qp.getIncorrectAttempts() + 1);
            qp.setDifficultyScore(Math.min(5, qp.getDifficultyScore() + 1));
        }

        saveProgress();
    }

    public QuestionProgress getProgress(Long questionId) {
        return progressList.stream()
                .filter(qp -> qp.getQuestionId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    public List<QuestionProgress> getAllProgress() {
        return progressList;
    }
}
