package com.gate.bioquest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gate.bioquest.model.Question;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private final String DATA_DIR = "data/questions";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Question> allQuestions = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        loadQuestions();
    }

    public void loadQuestions() throws IOException {
        allQuestions.clear();
        Path basePath = Paths.get(DATA_DIR);
        if (Files.exists(basePath)) {
            Files.walk(basePath)
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".json"))
                 .forEach(this::loadFile);
        }
    }

    private void loadFile(Path path) {
        try {
            List<Question> questions = objectMapper.readValue(path.toFile(), new TypeReference<List<Question>>(){});
            allQuestions.addAll(questions);
        } catch (IOException e) {
            System.err.println("Error reading file: " + path + " - " + e.getMessage());
        }
    }

    public List<Question> getAllQuestions() {
        return allQuestions;
    }

    public List<Question> getQuestionsBySubject(String subject) {
        return allQuestions.stream()
                .filter(q -> q.getSubject().equalsIgnoreCase(subject))
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByTopic(String topic) {
        return allQuestions.stream()
                .filter(q -> q.getTopic().equalsIgnoreCase(topic))
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByTopics(List<String> topics) {
        if (topics == null || topics.isEmpty()) return new ArrayList<>();
        List<String> lowerCaseTopics = topics.stream().map(String::toLowerCase).collect(Collectors.toList());
        return allQuestions.stream()
                .filter(q -> lowerCaseTopics.contains(q.getTopic().toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Question> getRandomQuestionsExcludingTopics(List<String> excludedTopics) {
        if (excludedTopics == null || excludedTopics.isEmpty()) return new ArrayList<>(allQuestions);
        List<String> lowerCaseExcluded = excludedTopics.stream().map(String::toLowerCase).collect(Collectors.toList());
        return allQuestions.stream()
                .filter(q -> !lowerCaseExcluded.contains(q.getTopic().toLowerCase()))
                .collect(Collectors.toList());
    }

    public Question getQuestionById(Long id) {
        return allQuestions.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void updateQuestionPriority(Long id, boolean isCorrect) {
        Question q = getQuestionById(id);
        if (q != null) {
            int currentPriority = q.getRevisionPriority() != null ? q.getRevisionPriority() : 1;
            if (isCorrect) {
                q.setRevisionPriority(Math.max(1, currentPriority - 1));
            } else {
                q.setRevisionPriority(currentPriority + 1);
            }
            saveQuestions();
        }
    }

    private void saveQuestions() {
        // Group questions by subject and topic
        Map<String, Map<String, List<Question>>> grouped = new HashMap<>();
        for (Question q : allQuestions) {
            String subjectFolder = q.getSubject().toLowerCase().replace(" ", "-");
            String topicFile = q.getTopic().toLowerCase().replace(" ", "-") + ".json";
            grouped.computeIfAbsent(subjectFolder, k -> new HashMap<>())
                   .computeIfAbsent(topicFile, k -> new ArrayList<>())
                   .add(q);
        }

        for (Map.Entry<String, Map<String, List<Question>>> subjectEntry : grouped.entrySet()) {
            for (Map.Entry<String, List<Question>> topicEntry : subjectEntry.getValue().entrySet()) {
                try {
                    Path path = Paths.get(DATA_DIR, subjectEntry.getKey(), topicEntry.getKey());
                    Files.createDirectories(path.getParent());
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), topicEntry.getValue());
                } catch (IOException e) {
                    System.err.println("Could not save questions for: " + subjectEntry.getKey() + "/" + topicEntry.getKey());
                }
            }
        }
    }
}
