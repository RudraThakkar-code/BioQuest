package com.gate.bioquest.model;

public class AttemptRecord {
    private String attemptId;
    private String date;
    private double totalMarks;
    private int totalAttempted;
    private int totalCorrect;
    private double accuracy;
    private int timeUsed; // in seconds

    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getTotalCorrect() { return totalCorrect; }
    public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public int getTimeUsed() { return timeUsed; }
    public void setTimeUsed(int timeUsed) { this.timeUsed = timeUsed; }
}
