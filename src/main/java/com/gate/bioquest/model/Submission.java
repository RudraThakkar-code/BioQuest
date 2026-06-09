package com.gate.bioquest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Submission {
    private String attemptId;
    private Integer timeUsed; // in seconds
    private List<QuestionResponse> responses;

    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public Integer getTimeUsed() { return timeUsed; }
    public void setTimeUsed(Integer timeUsed) { this.timeUsed = timeUsed; }

    public List<QuestionResponse> getResponses() { return responses; }
    public void setResponses(List<QuestionResponse> responses) { this.responses = responses; }

    public static class QuestionResponse {
        private Long questionId;
        private Object userResponse; // List<Integer> for MCQ/MSQ, Double/String for NAT
        private String status; // answered, not_answered, marked_review, answered_review

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Object getUserResponse() { return userResponse; }
        public void setUserResponse(Object userResponse) { this.userResponse = userResponse; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
