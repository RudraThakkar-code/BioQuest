package com.gate.bioquest.model;

import java.util.Map;

public class Blueprint {
    private String name;
    private Integer duration; // in minutes
    private Map<String, Integer> subjects; // subject name -> number of questions

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Map<String, Integer> getSubjects() { return subjects; }
    public void setSubjects(Map<String, Integer> subjects) { this.subjects = subjects; }
}
