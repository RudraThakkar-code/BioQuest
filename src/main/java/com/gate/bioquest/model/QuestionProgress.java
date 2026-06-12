package com.gate.bioquest.model;

import java.time.LocalDate;

public class QuestionProgress {

    private Long questionId;
    private int attempts;
    private int correctAttempts;
    private int incorrectAttempts;
    private int difficultyScore;
    private LocalDate lastSeen;

    public QuestionProgress() {
        this.difficultyScore = 1; // Default
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getCorrectAttempts() { return correctAttempts; }
    public void setCorrectAttempts(int correctAttempts) { this.correctAttempts = correctAttempts; }

    public int getIncorrectAttempts() { return incorrectAttempts; }
    public void setIncorrectAttempts(int incorrectAttempts) { this.incorrectAttempts = incorrectAttempts; }

    public int getDifficultyScore() { return difficultyScore; }
    public void setDifficultyScore(int difficultyScore) { this.difficultyScore = difficultyScore; }

    public LocalDate getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDate lastSeen) { this.lastSeen = lastSeen; }
}
