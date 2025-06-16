package com.school.grademanager.service;

import java.util.List;
import java.util.stream.Collectors;

import com.school.grademanager.controller.TeacherDashboardController;
import com.school.grademanager.model.Assessment;
import com.school.grademanager.model.Result;
import com.school.grademanager.repository.AssessmentRepository;
import com.school.grademanager.repository.ResultRepository;
import com.school.grademanager.repository.impl.AssessmentRepositoryImpl;
import com.school.grademanager.repository.impl.ResultRepositoryImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AssessmentService {
    private final ObservableList<Assessment> assessments = FXCollections.observableArrayList();
    private final ObservableList<Result> results = FXCollections.observableArrayList();
    private final AssessmentRepository assessmentRepository;
    private final ResultRepository resultRepository;

    public AssessmentService() {
        DatabaseService dbService = DatabaseService.getInstance();
        this.assessmentRepository = new AssessmentRepositoryImpl(dbService);
        this.resultRepository = new ResultRepositoryImpl(dbService);
        loadAssessmentsFromDatabase();
        loadResultsFromDatabase();
    }

    private void loadAssessmentsFromDatabase() {
        assessments.clear();
        List<Assessment> loadedAssessments;
            
            if (TeacherDashboardController.loggedInTeacher != null) {
            loadedAssessments = assessmentRepository.findByTeacherId(TeacherDashboardController.loggedInTeacher.getUserId());
            } else {
            loadedAssessments = assessmentRepository.findAll();
        }
        
        assessments.addAll(loadedAssessments);
    }

    public ObservableList<Assessment> getAssessments() {
        return assessments;
    }

    public ObservableList<Result> getResults() {
        return results;
    }

    public void addAssessment(Assessment assessment) {
        assessmentRepository.save(assessment);
            assessments.add(assessment);
    }

    public void addResult(Result result) {
        resultRepository.save(result);
            results.add(result);
    }

    public List<Result> getResultsForStudent(String studentId) {
        return resultRepository.findByStudentId(studentId);
    }

    public List<Result> getResultsForAssessment(String assessmentId) {
        return resultRepository.findByAssessmentId(assessmentId);
    }

    public boolean isAssessmentNameUnique(String title, String teacherId, String academicYear, String semester) {
        return assessmentRepository.isAssessmentNameUnique(title, teacherId, academicYear, semester);
    }

    public void updateAssessment(Assessment updated) {
        assessmentRepository.update(updated);
            for (int i = 0; i < assessments.size(); i++) {
                if (assessments.get(i).getAssessmentId().equals(updated.getAssessmentId())) {
                    assessments.set(i, updated);
                    break;
                }
        }
    }

    public void deleteAssessment(String assessmentId) {
        resultRepository.deleteByAssessmentId(assessmentId);
        assessmentRepository.delete(assessmentId);
                assessments.removeIf(a -> a.getAssessmentId().equals(assessmentId));
                results.removeIf(r -> r.getAssessmentId().equals(assessmentId));
    }

    public List<Assessment> getAssessmentsForClassLevel(String classLevel) {
        return assessmentRepository.findByClassLevel(classLevel);
    }

    public double getTotalMarksForClass(String classLevel) {
        return assessmentRepository.getTotalMarksForClass(classLevel);
    }

    public void updateResult(Result result) {
        resultRepository.update(result);
        results.removeIf(r -> r.getResultId().equals(result.getResultId()));
        results.add(result);
    }

    public void deleteResult(String resultId) {
        resultRepository.delete(resultId);
        results.removeIf(r -> r.getResultId().equals(resultId));
    }

    public Result getResult(String assessmentId, String studentId) {
        return resultRepository.findByAssessmentAndStudent(assessmentId, studentId);
    }

    private void loadResultsFromDatabase() {
        results.clear();
        List<Result> loadedResults;
            
            if (TeacherDashboardController.loggedInTeacher != null) {
            loadedResults = resultRepository.findAll().stream()
                .filter(r -> {
                    Assessment assessment = assessmentRepository.findById(r.getAssessmentId()).orElse(null);
                    return assessment != null && assessment.getTeacherId().equals(TeacherDashboardController.loggedInTeacher.getUserId());
                })
                .collect(Collectors.toList());
            } else {
            loadedResults = resultRepository.findAll();
            }
            
        results.addAll(loadedResults);
    }
}
