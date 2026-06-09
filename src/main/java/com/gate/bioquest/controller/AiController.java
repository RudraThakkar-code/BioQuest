package com.gate.bioquest.controller;

import com.gate.bioquest.ai.PromptBuilder;
import com.gate.bioquest.ai.StudyPlanner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final PromptBuilder promptBuilder;
    private final StudyPlanner studyPlanner;

    public AiController(PromptBuilder promptBuilder, StudyPlanner studyPlanner) {
        this.promptBuilder = promptBuilder;
        this.studyPlanner = studyPlanner;
    }

    @GetMapping("/report-prompt")
    public ResponseEntity<Map<String, String>> getChatGptReportPrompt(
            @RequestParam(required = false, defaultValue = "GATE BT") String goal,
            @RequestParam(required = false, defaultValue = "2 hours/day") String hours) {
        String prompt = promptBuilder.buildChatGptReportPrompt(goal, hours);
        Map<String, String> response = new HashMap<>();
        response.put("prompt", prompt);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/study-plan")
    public ResponseEntity<Map<String, String>> generateLocalPlan(@RequestBody Map<String, String> request) {
        String goal = request.getOrDefault("goal", "GATE BT");
        String hours = request.getOrDefault("hoursPerDay", "2 hours/day");
        String model = request.getOrDefault("model", "qwen");
        
        String plan = studyPlanner.generateStudyPlan(goal, hours, model);
        
        Map<String, String> response = new HashMap<>();
        response.put("plan", plan);
        return ResponseEntity.ok(response);
    }
}
