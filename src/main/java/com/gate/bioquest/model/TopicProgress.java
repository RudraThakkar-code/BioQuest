package com.gate.bioquest.model;

import java.time.LocalDate;

public class TopicProgress {

    private String subject;
    private String topic;

    private double accuracy;
    private int timesTested;

    private int mastery;

    private LocalDate lastReviewed;
    private LocalDate nextReview;

    private String status;
    private int revisionPriority;

    public TopicProgress() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public int getTimesTested() { return timesTested; }
    public void setTimesTested(int timesTested) { this.timesTested = timesTested; }

    public int getMastery() { return mastery; }
    public void setMastery(int mastery) { this.mastery = mastery; }

    public LocalDate getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(LocalDate lastReviewed) { this.lastReviewed = lastReviewed; }

    public LocalDate getNextReview() { return nextReview; }
    public void setNextReview(LocalDate nextReview) { this.nextReview = nextReview; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRevisionPriority() { return revisionPriority; }
    public void setRevisionPriority(int revisionPriority) { this.revisionPriority = revisionPriority; }
}
