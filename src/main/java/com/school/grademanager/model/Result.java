package com.school.grademanager.model;

public class Result {
    private String resultId;
    private String assessmentId;
    private String studentId;
    private double marksObtained;

    public Result(String resultId, String assessmentId, String studentId, double marksObtained) {
        this.resultId = resultId;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.marksObtained = marksObtained;
    }

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }
}
