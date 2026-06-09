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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExamEngineService(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
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
