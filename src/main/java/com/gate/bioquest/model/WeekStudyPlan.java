package com.gate.bioquest.model;

import java.util.List;

public class WeekStudyPlan {
    private int week;
    private List<String> subjects;
    private List<String> topics;

    public int getWeek() { return week; }
    public void setWeek(int week) { this.week = week; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
}
