package com.gate.bioquest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {
    private Long id;
    private String type; // MCQ, MSQ, NAT
    private Integer marks;
    private String subject;
    private String topic;
    private String difficulty;
    private String source;
    private String question;
    private List<String> options;
    private Object answer; // List<Integer> for MCQ/MSQ, Double for NAT
    private Double tolerance;
    private String explanation;
    private Integer revisionPriority = 1;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Object getAnswer() { return answer; }
    public void setAnswer(Object answer) { this.answer = answer; }

    public Double getTolerance() { return tolerance; }
    public void setTolerance(Double tolerance) { this.tolerance = tolerance; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getRevisionPriority() { return revisionPriority; }
    public void setRevisionPriority(Integer revisionPriority) { this.revisionPriority = revisionPriority; }
}
