package com.school.grademanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.school.grademanager.model.Assessment;
import com.school.grademanager.model.AssessmentType;
import com.school.grademanager.repository.AssessmentRepository;
import com.school.grademanager.service.DatabaseService;

public class AssessmentRepositoryImpl implements AssessmentRepository {
    private final DatabaseService databaseService;

    public AssessmentRepositoryImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Assessment save(Assessment assessment) {
        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert assessment
                String sql = "INSERT INTO assessment (assessment_id, title, date, total_marks, teacher_id, assessment_type, subject, academic_year, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, assessment.getAssessmentId());
                    stmt.setString(2, assessment.getTitle());
                    stmt.setDate(3, java.sql.Date.valueOf(assessment.getDate()));
                    stmt.setDouble(4, assessment.getTotalMarks());
                    stmt.setString(5, assessment.getTeacherId());
                    stmt.setString(6, assessment.getAssessmentType().name());
                    stmt.setString(7, assessment.getSubject());
                    stmt.setString(8, assessment.getAcademicYear());
                    stmt.setString(9, assessment.getSemester());
                    stmt.executeUpdate();
                }

                // Insert class levels
                sql = "INSERT INTO assessment_class (assessment_id, class_level) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (String classLevel : assessment.getClassLevels()) {
                        stmt.setString(1, assessment.getAssessmentId());
                        stmt.setString(2, classLevel);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                return assessment;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving assessment", e);
        }
    }

    @Override
    public Optional<Assessment> findById(String id) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM assessment WHERE assessment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Assessment assessment = mapResultSetToAssessment(rs);
                    assessment.setClassLevels(loadClassLevels(conn, id));
                    return Optional.of(assessment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding assessment by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Assessment> findAll() {
        List<Assessment> assessments = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM assessment";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Assessment assessment = mapResultSetToAssessment(rs);
                    assessment.setClassLevels(loadClassLevels(conn, assessment.getAssessmentId()));
                    assessments.add(assessment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all assessments", e);
        }
        return assessments;
    }

    @Override
    public void delete(String id) {
        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete class levels
                String sql = "DELETE FROM assessment_class WHERE assessment_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, id);
                    stmt.executeUpdate();
                }

                // Delete assessment
                sql = "DELETE FROM assessment WHERE assessment_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, id);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting assessment", e);
        }
    }

    @Override
    public void update(Assessment assessment) {
        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update assessment
                String sql = "UPDATE assessment SET title = ?, total_marks = ?, assessment_type = ? WHERE assessment_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, assessment.getTitle());
                    stmt.setDouble(2, assessment.getTotalMarks());
                    stmt.setString(3, assessment.getAssessmentType().name());
                    stmt.setString(4, assessment.getAssessmentId());
                    stmt.executeUpdate();
                }

                // Update class levels
                sql = "DELETE FROM assessment_class WHERE assessment_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, assessment.getAssessmentId());
                    stmt.executeUpdate();
                }

                sql = "INSERT INTO assessment_class (assessment_id, class_level) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (String classLevel : assessment.getClassLevels()) {
                        stmt.setString(1, assessment.getAssessmentId());
                        stmt.setString(2, classLevel);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating assessment", e);
        }
    }

    @Override
    public List<Assessment> findByTeacherId(String teacherId) {
        List<Assessment> assessments = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM assessment WHERE teacher_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teacherId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Assessment assessment = mapResultSetToAssessment(rs);
                    assessment.setClassLevels(loadClassLevels(conn, assessment.getAssessmentId()));
                    assessments.add(assessment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding assessments by teacher id", e);
        }
        return assessments;
    }

    @Override
    public List<Assessment> findByClassLevel(String classLevel) {
        List<Assessment> assessments = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT a.* FROM assessment a " +
                        "JOIN assessment_class ac ON a.assessment_id = ac.assessment_id " +
                        "WHERE ac.class_level = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, classLevel);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Assessment assessment = mapResultSetToAssessment(rs);
                    assessment.setClassLevels(loadClassLevels(conn, assessment.getAssessmentId()));
                    assessments.add(assessment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding assessments by class level", e);
        }
        return assessments;
    }

    @Override
    public boolean isAssessmentNameUnique(String title, String teacherId, String academicYear, String semester) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT COUNT(*) FROM assessment WHERE title = ? AND teacher_id = ? AND academic_year = ? AND semester = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, teacherId);
                stmt.setString(3, academicYear);
                stmt.setString(4, semester);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking assessment name uniqueness", e);
        }
        return true;
    }

    @Override
    public double getTotalMarksForClass(String classLevel) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT SUM(a.total_marks) FROM assessment a " +
                        "JOIN assessment_class ac ON a.assessment_id = ac.assessment_id " +
                        "WHERE ac.class_level = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, classLevel);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total marks for class", e);
        }
        return 0.0;
    }

    @Override
    public List<Assessment> findByClassLevels(List<String> classLevels) {
        List<Assessment> assessments = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT DISTINCT a.* FROM assessment a " +
                        "JOIN assessment_class ac ON a.assessment_id = ac.assessment_id " +
                        "WHERE ac.class_level IN (" + String.join(",", classLevels.stream().map(s -> "?").toArray(String[]::new)) + ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < classLevels.size(); i++) {
                    stmt.setString(i + 1, classLevels.get(i));
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Assessment assessment = mapResultSetToAssessment(rs);
                    assessment.setClassLevels(loadClassLevels(conn, assessment.getAssessmentId()));
                    assessments.add(assessment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding assessments by class levels", e);
        }
        return assessments;
    }

    private List<String> loadClassLevels(Connection conn, String assessmentId) throws SQLException {
        List<String> classLevels = new ArrayList<>();
        String sql = "SELECT class_level FROM assessment_class WHERE assessment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, assessmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                classLevels.add(rs.getString("class_level"));
            }
        }
        return classLevels;
    }

    private Assessment mapResultSetToAssessment(ResultSet rs) throws SQLException {
        return new Assessment(
            rs.getString("assessment_id"),
            rs.getString("title"),
            rs.getDate("date").toLocalDate(),
            rs.getDouble("total_marks"),
            rs.getString("teacher_id"),
            AssessmentType.valueOf(rs.getString("assessment_type")),
            rs.getString("subject"),
            rs.getString("academic_year"),
            rs.getString("semester"),
            new ArrayList<>() // Class levels will be loaded separately
        );
    }
} 