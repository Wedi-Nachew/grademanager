package com.school.grademanager.model;

import java.time.LocalDate;
import java.util.List;

public class Assessment {
    private String assessmentId;
    private String title;
    private LocalDate date;
    private double totalMarks;
    private String teacherId;
    private AssessmentType assessmentType;
    private String subject;
    private String academicYear;
    private String semester;
    private List<String> classLevels;

    public Assessment(String assessmentId, String title, LocalDate date, double totalMarks, String teacherId, AssessmentType assessmentType, String subject, String academicYear, String semester, List<String> classLevels) {
        this.assessmentId = assessmentId;
        this.title = title;
        this.date = date;
        this.totalMarks = totalMarks;
        this.teacherId = teacherId;
        this.assessmentType = assessmentType;
        this.subject = subject;
        this.academicYear = academicYear;
        this.semester = semester;
        this.classLevels = classLevels;
    }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public AssessmentType getAssessmentType() { return assessmentType; }
    public void setAssessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public List<String> getClassLevels() { return classLevels; }
    public void setClassLevels(List<String> classLevels) { this.classLevels = classLevels; }
}
