package com.gate.bioquest.service;

import com.gate.bioquest.model.EvaluationResult;
import com.gate.bioquest.model.Question;
import com.gate.bioquest.model.Submission;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EvaluationService {

    private final QuestionBankService questionBankService;
    private final ProgressService progressService;
    private final QuestionProgressService questionProgressService;
    private final MistakeService mistakeService;

    public EvaluationService(QuestionBankService questionBankService,
                             ProgressService progressService,
                             QuestionProgressService questionProgressService,
                             MistakeService mistakeService) {
        this.questionBankService = questionBankService;
        this.progressService = progressService;
        this.questionProgressService = questionProgressService;
        this.mistakeService = mistakeService;
    }

    public EvaluationResult evaluate(Submission submission) {
        EvaluationResult result = new EvaluationResult();
        List<EvaluationResult.QuestionResult> questionResults = new ArrayList<>();
        double totalMarks = 0;
        int totalAttempted = 0;
        int totalCorrect = 0;

        for (Submission.QuestionResponse response : submission.getResponses()) {
            Question question = questionBankService.getQuestionById(response.getQuestionId());
            if (question == null) continue;

            EvaluationResult.QuestionResult qResult = new EvaluationResult.QuestionResult();
            qResult.setQuestion(question);
            qResult.setUserResponse(response.getUserResponse());

            boolean isAttempted = response.getUserResponse() != null && !response.getUserResponse().toString().trim().isEmpty();
            if (response.getUserResponse() instanceof List && ((List<?>) response.getUserResponse()).isEmpty()) {
                isAttempted = false;
            }

            if (!isAttempted) {
                qResult.setCorrect(false);
                qResult.setMarksAwarded(0);
            } else {
                totalAttempted++;
                boolean isCorrect = false;
                double marks = 0;

                switch (question.getType()) {
                    case "MCQ":
                        isCorrect = evaluateMCQ(question, response.getUserResponse());
                        if (isCorrect) {
                            marks = question.getMarks();
                        } else {
                            // Negative marking: 1/3 of the marks
                            marks = - (question.getMarks() / 3.0);
                        }
                        break;
                    case "MSQ":
                        isCorrect = evaluateMSQ(question, response.getUserResponse());
                        if (isCorrect) {
                            marks = question.getMarks();
                        } else {
                            marks = 0; // No negative marking
                        }
                        break;
                    case "NAT":
                        isCorrect = evaluateNAT(question, response.getUserResponse());
                        if (isCorrect) {
                            marks = question.getMarks();
                        } else {
                            marks = 0; // No negative marking
                        }
                        break;
                }

                qResult.setCorrect(isCorrect);
                qResult.setMarksAwarded(marks);
                totalMarks += marks;
                if (isCorrect) totalCorrect++;
            }
            questionResults.add(qResult);
            
            if (isAttempted) {
                questionBankService.updateQuestionPriority(question.getId(), qResult.isCorrect());

                // Confidence Logic
                com.gate.bioquest.model.ConfidenceLevel conf = response.getConfidence() != null
                        ? response.getConfidence() : com.gate.bioquest.model.ConfidenceLevel.GUESS; // default to guess if missing

                int masteryAdjustment = 0;
                if (qResult.isCorrect()) {
                    if (conf == com.gate.bioquest.model.ConfidenceLevel.CONFIDENT) masteryAdjustment = 20;
                    else masteryAdjustment = 10;
                } else {
                    if (conf == com.gate.bioquest.model.ConfidenceLevel.CONFIDENT) masteryAdjustment = -25;
                    else masteryAdjustment = -10;
                }

                progressService.updateTopicWithConfidence(question.getSubject(), question.getTopic(), masteryAdjustment, qResult.isCorrect());
                questionProgressService.updateProgress(question.getId(), qResult.isCorrect());

                if (qResult.isCorrect()) {
                    mistakeService.recordCorrect(question.getId());
                } else {
                    mistakeService.recordMistake(question.getId(), question.getTopic(), response.getUserResponse(), question.getAnswer());
                }
            }
        }

        result.setQuestionResults(questionResults);
        result.setTotalMarks(totalMarks);
        result.setTotalAttempted(totalAttempted);
        result.setTotalCorrect(totalCorrect);

        return result;
    }

    private boolean evaluateMCQ(Question question, Object userResponse) {
        try {
            List<Integer> expected = (List<Integer>) question.getAnswer();
            List<Integer> actual = extractIntegerList(userResponse);
            if (actual.size() != 1 || expected.isEmpty()) return false;
            return expected.get(0).equals(actual.get(0));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean evaluateMSQ(Question question, Object userResponse) {
        try {
            List<Integer> expected = (List<Integer>) question.getAnswer();
            List<Integer> actual = extractIntegerList(userResponse);
            Set<Integer> expectedSet = new HashSet<>(expected);
            Set<Integer> actualSet = new HashSet<>(actual);
            return expectedSet.equals(actualSet);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean evaluateNAT(Question question, Object userResponse) {
        try {
            double expected = Double.parseDouble(question.getAnswer().toString());
            double actual = Double.parseDouble(userResponse.toString());
            double tolerance = question.getTolerance() != null ? question.getTolerance() : 0.0;
            return Math.abs(expected - actual) <= tolerance;
        } catch (Exception e) {
            return false;
        }
    }

    private List<Integer> extractIntegerList(Object obj) {
        List<Integer> result = new ArrayList<>();
        if (obj instanceof List) {
            for (Object item : (List<?>) obj) {
                if (item instanceof Integer) {
                    result.add((Integer) item);
                } else if (item instanceof String) {
                    result.add(Integer.parseInt((String) item));
                }
            }
        }
        return result;
    }
}
