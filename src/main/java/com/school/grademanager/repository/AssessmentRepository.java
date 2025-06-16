package com.school.grademanager.repository;

import java.util.List;

import com.school.grademanager.model.Assessment;

public interface AssessmentRepository extends Repository<Assessment, String> {
    List<Assessment> findByTeacherId(String teacherId);
    List<Assessment> findByClassLevel(String classLevel);
    boolean isAssessmentNameUnique(String title, String teacherId, String academicYear, String semester);
    double getTotalMarksForClass(String classLevel);
    List<Assessment> findByClassLevels(List<String> classLevels);
} 