package com.gate.bioquest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gate.bioquest.model.Mistake;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MistakeService {

    private final String FILE_PATH = "data/mistakes/mistake-book.json";
    private final ObjectMapper objectMapper;
    private List<Mistake> mistakes = new ArrayList<>();

    public MistakeService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        loadMistakes();
    }

    public void loadMistakes() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                mistakes = objectMapper.readValue(file, new TypeReference<List<Mistake>>() {});
            } catch (IOException e) {
                System.err.println("Error loading mistakes: " + e.getMessage());
                mistakes = new ArrayList<>();
            }
        } else {
            mistakes = new ArrayList<>();
        }
    }

    public void saveMistakes() {
        File file = new File(FILE_PATH);
        try {
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, mistakes);
        } catch (IOException e) {
            System.err.println("Error saving mistakes: " + e.getMessage());
        }
    }

    public void recordMistake(Long questionId, String topic, Object yourAnswer, Object correctAnswer) {
        Mistake mistake = getMistake(questionId);
        if (mistake == null) {
            mistake = new Mistake();
            mistake.setQuestionId(questionId);
            mistake.setTopic(topic);
            mistake.setTimesCorrected(0);
            mistakes.add(mistake);
        }

        mistake.setYourAnswer(yourAnswer);
        mistake.setCorrectAnswer(correctAnswer);
        mistake.setDate(LocalDate.now());
        
        // Reset or set status to ACTIVE when wrong
        mistake.setStatus("ACTIVE");
        
        // Optionally, reset timesCorrected if they get it wrong again?
        // Actually, user wants it to go to ACTIVE. 
        // mistake.setTimesCorrected(0); // We'll reset it to require 3 correct attempts again

        saveMistakes();
    }

    public void recordCorrect(Long questionId) {
        Mistake mistake = getMistake(questionId);
        if (mistake != null) {
            int currentCorrected = mistake.getTimesCorrected() + 1;
            mistake.setTimesCorrected(currentCorrected);

            if (currentCorrected >= 3) {
                mistake.setStatus("MASTERED");
            } else {
                mistake.setStatus("REVIEW");
            }
            saveMistakes();
        }
    }

    public Mistake getMistake(Long questionId) {
        return mistakes.stream()
                .filter(m -> m.getQuestionId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    public List<Mistake> getAllMistakes() {
        return mistakes;
    }

    public List<Mistake> getActiveAndReviewMistakes() {
        return mistakes.stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()) || "REVIEW".equals(m.getStatus()))
                .collect(Collectors.toList());
    }
}
