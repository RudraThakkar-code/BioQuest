package com.gate.bioquest.model;

import java.util.List;

public class EvaluationResult {
    private double totalMarks;
    private int totalAttempted;
    private int totalCorrect;
    private List<QuestionResult> questionResults;

    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getTotalCorrect() { return totalCorrect; }
    public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }

    public List<QuestionResult> getQuestionResults() { return questionResults; }
    public void setQuestionResults(List<QuestionResult> questionResults) { this.questionResults = questionResults; }

    public static class QuestionResult {
        private Question question;
        private Object userResponse;
        private boolean isCorrect;
        private double marksAwarded;

        public Question getQuestion() { return question; }
        public void setQuestion(Question question) { this.question = question; }

        public Object getUserResponse() { return userResponse; }
        public void setUserResponse(Object userResponse) { this.userResponse = userResponse; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }

        public double getMarksAwarded() { return marksAwarded; }
        public void setMarksAwarded(double marksAwarded) { this.marksAwarded = marksAwarded; }
    }
}
