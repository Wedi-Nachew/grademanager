package com.school.grademanager.repository;

import java.util.List;

import com.school.grademanager.model.Result;

public interface ResultRepository extends Repository<Result, String> {
    List<Result> findByStudentId(String studentId);
    List<Result> findByAssessmentId(String assessmentId);
    Result findByAssessmentAndStudent(String assessmentId, String studentId);
    void deleteByAssessmentId(String assessmentId);
} 