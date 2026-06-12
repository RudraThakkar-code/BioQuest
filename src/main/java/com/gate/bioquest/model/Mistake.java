package com.gate.bioquest.model;

import java.time.LocalDate;

public class Mistake {

    private Long questionId;
    private String topic;
    private Object yourAnswer;
    private Object correctAnswer;
    private LocalDate date;
    private String status; // ACTIVE, REVIEW, MASTERED
    private int timesCorrected;

    public Mistake() {}

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Object getYourAnswer() { return yourAnswer; }
    public void setYourAnswer(Object yourAnswer) { this.yourAnswer = yourAnswer; }

    public Object getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(Object correctAnswer) { this.correctAnswer = correctAnswer; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTimesCorrected() { return timesCorrected; }
    public void setTimesCorrected(int timesCorrected) { this.timesCorrected = timesCorrected; }
}
