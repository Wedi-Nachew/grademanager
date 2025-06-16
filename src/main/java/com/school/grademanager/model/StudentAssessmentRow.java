package com.school.grademanager.model;

import javafx.beans.property.SimpleIntegerProperty;

public class StudentAssessmentRow {
    private final Assessment assessment;
    private final Double marksObtained;
    private final SimpleIntegerProperty sn = new SimpleIntegerProperty(0);

    public StudentAssessmentRow(Assessment assessment, Double marksObtained) {
        this.assessment = assessment;
        this.marksObtained = marksObtained;
    }

    public int getSn() { return sn.get(); }
    public void setSn(int value) { sn.set(value); }
    public SimpleIntegerProperty snProperty() { return sn; }

    public String getTitle() { return assessment.getTitle(); }
    public String getAssessmentType() { return assessment.getAssessmentType().name(); }
    public double getTotalMarks() { return assessment.getTotalMarks(); }
    public Double getMarksObtained() { return marksObtained; }
    public Assessment getAssessment() { return assessment; }
} 