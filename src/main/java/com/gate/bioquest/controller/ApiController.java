package com.gate.bioquest.controller;

import com.gate.bioquest.model.AttemptRecord;
import com.gate.bioquest.model.Blueprint;
import com.gate.bioquest.model.EvaluationResult;
import com.gate.bioquest.model.Question;
import com.gate.bioquest.model.Submission;
import com.gate.bioquest.service.EvaluationService;
import com.gate.bioquest.service.ExamEngineService;
import com.gate.bioquest.service.PerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow requests from our vanilla frontend
public class ApiController {

    private final ExamEngineService examEngineService;
    private final EvaluationService evaluationService;
    private final PerformanceService performanceService;

    public ApiController(ExamEngineService examEngineService, EvaluationService evaluationService, PerformanceService performanceService) {
        this.examEngineService = examEngineService;
        this.evaluationService = evaluationService;
        this.performanceService = performanceService;
    }

    @GetMapping("/blueprint/{name}")
    public ResponseEntity<Blueprint> getBlueprint(@PathVariable String name) {
        try {
            Blueprint bp = examEngineService.loadBlueprint(name);
            return bp != null ? ResponseEntity.ok(bp) : ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/exam/{blueprintName}")
    public ResponseEntity<List<Question>> startExam(@PathVariable String blueprintName) {
        try {
            List<Question> questions = examEngineService.generateExam(blueprintName);
            return ResponseEntity.ok(questions);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<EvaluationResult> submitExam(@RequestBody Submission submission) {
        EvaluationResult result = evaluationService.evaluate(submission);
        performanceService.updatePerformance(result);
        
        // Build Attempt Record
        AttemptRecord record = new AttemptRecord();
        record.setAttemptId(submission.getAttemptId());
        record.setDate(java.time.LocalDateTime.now().toString());
        record.setTotalMarks(result.getTotalMarks());
        record.setTotalAttempted(result.getTotalAttempted());
        record.setTotalCorrect(result.getTotalCorrect());
        record.setAccuracy(result.getTotalAttempted() > 0 ? ((double)result.getTotalCorrect() / result.getTotalAttempted()) * 100 : 0.0);
        record.setTimeUsed(submission.getTimeUsed() != null ? submission.getTimeUsed() : 0);

        // Save Attempt Record
        try {
            Path path = Paths.get("data/attempts", submission.getAttemptId() + ".json");
            Files.createDirectories(path.getParent());
            new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(), record);
        } catch (IOException e) {
            System.err.println("Could not save attempt: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<AttemptRecord>> getAttempts() {
        List<AttemptRecord> records = new ArrayList<>();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            Path dir = Paths.get("data/attempts");
            if (Files.exists(dir)) {
                Files.walk(dir)
                     .filter(Files::isRegularFile)
                     .filter(p -> p.toString().endsWith(".json"))
                     .forEach(p -> {
                         try {
                             AttemptRecord rec = mapper.readValue(p.toFile(), AttemptRecord.class);
                             records.add(rec);
                         } catch (Exception e) {
                             System.err.println("Error reading attempt: " + p);
                         }
                     });
            }
        } catch (IOException e) {
            System.err.println("Error walking attempts directory: " + e.getMessage());
        }
        // Sort descending by date
        records.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return ResponseEntity.ok(records);
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Map<String, Integer>>> getPerformance() {
        return ResponseEntity.ok(performanceService.getPerformanceData());
    }
}
