package com.school.grademanager.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school.grademanager.model.Student;
import com.school.grademanager.model.Teacher;
import com.school.grademanager.model.User;
import com.school.grademanager.model.User.Role;

public class UserService {
    private static final String DB_URL = "jdbc:sqlite:school.db";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    public Optional<User> authenticate(String username, String password) {
        try (Connection conn = DatabaseService.getConnection()) {
            // Try teacher
            String sql = "SELECT * FROM teacher WHERE username=? AND password_hash=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    List<String> classLevels = getTeacherClasses(conn, userId);
                    return Optional.of(new Teacher(userId, username, password, fullName, email, classLevels));
                }
            }
            // Try student
            sql = "SELECT * FROM student WHERE username=? AND password_hash=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String studentId = rs.getString("student_id");
                    String fullName = rs.getString("full_name");
                    LocalDate dob = rs.getDate("dob").toLocalDate();
                    String classLevel = rs.getString("class_level");
                    String email = rs.getString("email");
                    String stream = rs.getString("stream");
                    String language = rs.getString("language");
                    String section = rs.getString("section");
                    return Optional.of(new Student(userId, username, password, studentId, fullName, dob, classLevel, email, stream, language, section));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            // Teachers
            String sql = "SELECT * FROM teacher";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    String username = rs.getString("username");
                    String password = rs.getString("password_hash");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    List<String> classLevels = getTeacherClasses(conn, userId);
                    users.add(new Teacher(userId, username, password, fullName, email, classLevels));
                }
            }
            // Students
            sql = "SELECT * FROM student";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    String username = rs.getString("username");
                    String password = rs.getString("password_hash");
                    String studentId = rs.getString("student_id");
                    String fullName = rs.getString("full_name");
                    LocalDate dob = rs.getDate("dob").toLocalDate();
                    String classLevel = rs.getString("class_level");
                    String email = rs.getString("email");
                    String stream = rs.getString("stream");
                    String language = rs.getString("language");
                    String section = rs.getString("section");
                    users.add(new Student(userId, username, password, studentId, fullName, dob, classLevel, email, stream, language, section));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public Map<String, List<String>> getTeacherAssignedClassesAndSections(Connection conn, String userId) throws SQLException {
        Map<String, List<String>> assignedClasses = new HashMap<>();
        String sql = "SELECT class_level, section FROM teacher_class_subject WHERE teacher_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String classLevel = rs.getString("class_level");
                String section = rs.getString("section");
                assignedClasses.computeIfAbsent(classLevel, k -> new ArrayList<>()).add(section);
            }
        }
        return assignedClasses;
    }

    private List<String> getTeacherClasses(Connection conn, String userId) throws SQLException {
        List<String> classLevels = new ArrayList<>();
        String sql = "SELECT DISTINCT class_level FROM teacher_class_subject WHERE teacher_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                classLevels.add(rs.getString("class_level"));
            }
        }
        return classLevels;
    }

    public User authenticateUser(String username, String password) {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String role = rs.getString("role");
                    return new User(userId, username, password, Role.valueOf(role));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> getStudentsByClassAndSection(String classLevel, String section) {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM student WHERE class_level = ? AND section = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, classLevel);
                stmt.setString(2, section);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    students.add(new Student(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("student_id"),
                        rs.getString("full_name"),
                        rs.getDate("dob").toLocalDate(),
                        rs.getString("class_level"),
                        rs.getString("email"),
                        rs.getString("stream"),
                        rs.getString("language"),
                        rs.getString("section")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM teacher";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    List<String> classLevels = getTeacherClasses(conn, userId);
                    teachers.add(new Teacher(
                        userId,
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        classLevels
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teachers;
    }
}
