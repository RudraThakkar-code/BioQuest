package com.gate.bioquest.model;

import java.util.List;

public class MonthStudyPlan {
    private String month;
    private List<String> subjects;
    private List<String> topics;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
}
