package com.gate.bioquest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gate.bioquest.model.Blueprint;
import com.gate.bioquest.model.Question;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamEngineService {

    private final QuestionBankService questionBankService;
    private final ProgressService progressService;
    private final StudyPlanService studyPlanService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExamEngineService(QuestionBankService questionBankService, ProgressService progressService, StudyPlanService studyPlanService) {
        this.questionBankService = questionBankService;
        this.progressService = progressService;
        this.studyPlanService = studyPlanService;
    }

    public Blueprint loadBlueprint(String name) throws IOException {
        File file = new File("data/blueprints/" + name + ".json");
        if (!file.exists()) return null;
        return objectMapper.readValue(file, Blueprint.class);
    }

    public List<Question> generateExam(String blueprintName) throws IOException {
        Blueprint blueprint = loadBlueprint(blueprintName);
        if (blueprint == null) throw new IllegalArgumentException("Blueprint not found");

        List<Question> examQuestions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : blueprint.getSubjects().entrySet()) {
            String subject = entry.getKey();
            int count = entry.getValue();

            List<Question> subjectQuestions = questionBankService.getQuestionsBySubject(subject);
            Collections.shuffle(subjectQuestions);
            
            examQuestions.addAll(subjectQuestions.stream()
                    .limit(count)
                    .collect(Collectors.toList()));
        }
        
        // Remove answer and explanation from questions sent to frontend
        return examQuestions.stream().map(this::sanitizeQuestion).collect(Collectors.toList());
    }

    public List<Question> generateWeeklyExam(int totalQuestions) throws IOException {
        int currentCount = (int) Math.round(totalQuestions * 0.60);
        int weakCount = (int) Math.round(totalQuestions * 0.20);
        int dueCount = (int) Math.round(totalQuestions * 0.20);

        List<String> currentTopics = studyPlanService.getCurrentWeekPlan() != null ? studyPlanService.getCurrentWeekPlan().getTopics() : Collections.emptyList();
        List<String> weakTopics = progressService.findWeakTopics().stream().map(t -> t.getTopic()).collect(Collectors.toList());
        List<String> dueTopics = progressService.findDueTopics().stream().map(t -> t.getTopic()).collect(Collectors.toList());

        return buildExam(totalQuestions, currentCount, currentTopics, weakCount, weakTopics, dueCount, dueTopics, 0, Collections.emptyList());
    }

    public List<Question> generateMonthlyExam(int totalQuestions) throws IOException {
        int currentCount = (int) Math.round(totalQuestions * 0.40);
        int prevCount = (int) Math.round(totalQuestions * 0.30);
        int weakCount = (int) Math.round(totalQuestions * 0.20);
        int randomCount = totalQuestions - currentCount - prevCount - weakCount; // Remaining

        List<String> currentTopics = studyPlanService.getCurrentMonthPlan() != null ? studyPlanService.getCurrentMonthPlan().getTopics() : Collections.emptyList();
        List<String> prevTopics = studyPlanService.getPreviousMonthPlan() != null ? studyPlanService.getPreviousMonthPlan().getTopics() : Collections.emptyList();
        List<String> weakTopics = progressService.findWeakTopics().stream().map(t -> t.getTopic()).collect(Collectors.toList());

        List<String> excludedFromRandom = new ArrayList<>();
        excludedFromRandom.addAll(currentTopics);
        excludedFromRandom.addAll(prevTopics);

        return buildExam(totalQuestions, currentCount, currentTopics, weakCount, weakTopics, prevCount, prevTopics, randomCount, excludedFromRandom);
    }

    public List<Question> generateRevisionExam(int totalQuestions) {
        int dueCount = (int) Math.round(totalQuestions * 0.80);
        int weakCount = totalQuestions - dueCount;

        List<String> dueTopics = progressService.findDueTopics().stream().map(t -> t.getTopic()).collect(Collectors.toList());
        List<String> weakTopics = progressService.findWeakTopics().stream().map(t -> t.getTopic()).collect(Collectors.toList());

        return buildExam(totalQuestions, dueCount, dueTopics, weakCount, weakTopics, 0, Collections.emptyList(), 0, Collections.emptyList());
    }

    private List<Question> buildExam(int totalQuestions,
            int primaryCount, List<String> primaryTopics,
            int secondaryCount, List<String> secondaryTopics,
            int tertiaryCount, List<String> tertiaryTopics,
            int randomCount, List<String> excludedFromRandom) {

        List<Question> examQuestions = new ArrayList<>();

        // Helper to add questions
        int actualPrimary = addQuestionsFromTopics(examQuestions, primaryTopics, primaryCount, Collections.emptyList());
        int actualSecondary = addQuestionsFromTopics(examQuestions, secondaryTopics, secondaryCount, examQuestions);
        int actualTertiary = addQuestionsFromTopics(examQuestions, tertiaryTopics, tertiaryCount, examQuestions);

        // Initial pass for random questions (if applicable)
        if (randomCount > 0) {
             addRandomQuestions(examQuestions, excludedFromRandom, randomCount, examQuestions);
        }

        int missing = totalQuestions - examQuestions.size();

        // Fallback 1: fill shortages from Primary Topics first
        if (missing > 0 && !primaryTopics.isEmpty()) {
            addQuestionsFromTopics(examQuestions, primaryTopics, missing, examQuestions);
            missing = totalQuestions - examQuestions.size();
        }

        // Fallback 2: fill shortages from Random Old Topics
        if (missing > 0) {
            addRandomQuestions(examQuestions, excludedFromRandom, missing, examQuestions);
            missing = totalQuestions - examQuestions.size();
        }

        // Fallback 3: if still missing, just add any random questions
        if (missing > 0) {
             addRandomQuestions(examQuestions, Collections.emptyList(), missing, examQuestions);
        }

        Collections.shuffle(examQuestions);
        return examQuestions.stream().limit(totalQuestions).map(this::sanitizeQuestion).collect(Collectors.toList());
    }

    private int addQuestionsFromTopics(List<Question> currentExam, List<String> topics, int limit, List<Question> exclude) {
        if (limit <= 0 || topics == null || topics.isEmpty()) return 0;
        List<Question> available = questionBankService.getQuestionsByTopics(topics);
        available.removeAll(exclude);
        Collections.shuffle(available);
        int toAdd = Math.min(limit, available.size());
        currentExam.addAll(available.subList(0, toAdd));
        return toAdd;
    }

    private int addRandomQuestions(List<Question> currentExam, List<String> excludedTopics, int limit, List<Question> exclude) {
        if (limit <= 0) return 0;
        List<Question> available = questionBankService.getRandomQuestionsExcludingTopics(excludedTopics);
        available.removeAll(exclude);
        Collections.shuffle(available);
        int toAdd = Math.min(limit, available.size());
        currentExam.addAll(available.subList(0, toAdd));
        return toAdd;
    }

    private Question sanitizeQuestion(Question q) {
        Question sanitized = new Question();
        sanitized.setId(q.getId());
        sanitized.setType(q.getType());
        sanitized.setMarks(q.getMarks());
        sanitized.setSubject(q.getSubject());
        sanitized.setTopic(q.getTopic());
        sanitized.setQuestion(q.getQuestion());
        sanitized.setOptions(q.getOptions());
        // Do not copy answer, explanation, source, tolerance, priority
        return sanitized;
    }
}
