package com.school.grademanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.school.grademanager.model.Result;
import com.school.grademanager.repository.ResultRepository;
import com.school.grademanager.service.DatabaseService;

public class ResultRepositoryImpl implements ResultRepository {
    private final DatabaseService databaseService;

    public ResultRepositoryImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Result save(Result result) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "INSERT INTO result (result_id, assessment_id, student_id, marks_obtained) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, result.getResultId());
                stmt.setString(2, result.getAssessmentId());
                stmt.setString(3, result.getStudentId());
                stmt.setDouble(4, result.getMarksObtained());
                stmt.executeUpdate();
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving result", e);
        }
    }

    @Override
    public Optional<Result> findById(String id) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM result WHERE result_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Result> findAll() {
        List<Result> results = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM result";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all results", e);
        }
        return results;
    }

    @Override
    public void delete(String id) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "DELETE FROM result WHERE result_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting result", e);
        }
    }

    @Override
    public void update(Result result) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "UPDATE result SET marks_obtained = ? WHERE result_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, result.getMarksObtained());
                stmt.setString(2, result.getResultId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating result", e);
        }
    }

    @Override
    public List<Result> findByStudentId(String studentId) {
        List<Result> results = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM result WHERE student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding results by student id", e);
        }
        return results;
    }

    @Override
    public List<Result> findByAssessmentId(String assessmentId) {
        List<Result> results = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM result WHERE assessment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, assessmentId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding results by assessment id", e);
        }
        return results;
    }

    @Override
    public Result findByAssessmentAndStudent(String assessmentId, String studentId) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM result WHERE assessment_id = ? AND student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, assessmentId);
                stmt.setString(2, studentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return mapResultSetToResult(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result by assessment and student", e);
        }
        return null;
    }

    @Override
    public void deleteByAssessmentId(String assessmentId) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "DELETE FROM result WHERE assessment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, assessmentId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting results by assessment id", e);
        }
    }

    private Result mapResultSetToResult(ResultSet rs) throws SQLException {
        return new Result(
            rs.getString("result_id"),
            rs.getString("assessment_id"),
            rs.getString("student_id"),
            rs.getDouble("marks_obtained")
        );
    }
} 