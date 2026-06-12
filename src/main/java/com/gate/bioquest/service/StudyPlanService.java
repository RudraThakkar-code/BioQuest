package com.gate.bioquest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gate.bioquest.model.MonthStudyPlan;
import com.gate.bioquest.model.WeekStudyPlan;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class StudyPlanService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeekStudyPlan getCurrentWeekPlan() throws IOException {
        File file = new File("data/study-plan/current-week.json");
        if (!file.exists()) return null;
        return objectMapper.readValue(file, WeekStudyPlan.class);
    }

    public MonthStudyPlan getCurrentMonthPlan() throws IOException {
        File file = new File("data/study-plan/current-month.json");
        if (!file.exists()) return null;
        return objectMapper.readValue(file, MonthStudyPlan.class);
    }

    public MonthStudyPlan getPreviousMonthPlan() throws IOException {
        File file = new File("data/study-plan/previous-month.json");
        if (!file.exists()) return null;
        return objectMapper.readValue(file, MonthStudyPlan.class);
    }
}
