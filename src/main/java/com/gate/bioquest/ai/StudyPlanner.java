package com.gate.bioquest.ai;

import org.springframework.stereotype.Service;

@Service
public class StudyPlanner {

    private final PromptBuilder promptBuilder;
    private final OllamaClient ollamaClient;

    public StudyPlanner(PromptBuilder promptBuilder, OllamaClient ollamaClient) {
        this.promptBuilder = promptBuilder;
        this.ollamaClient = ollamaClient;
    }

    public String generateStudyPlan(String goal, String hours, String model) {
        String baseReport = promptBuilder.buildChatGptReportPrompt(goal, hours);
        
        String systemInstruction = "You are an expert GATE Biotechnology mentor.\n" +
                "Review the following student performance report and create:\n" +
                "1. A day-by-day Weekly plan\n" +
                "2. A Revision schedule focusing on Weak Areas\n" +
                "3. A Mock test schedule\n" +
                "4. Topic priorities\n\n" +
                "Report:\n" + baseReport;

        return ollamaClient.generate(model != null ? model : "qwen", systemInstruction);
    }
}
