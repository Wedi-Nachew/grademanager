package com.school.grademanager.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.school.grademanager.model.Student;
import com.school.grademanager.model.Teacher;
import com.school.grademanager.service.DatabaseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class AdminDashboardController {
    // Student Management
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colStudentName;
    @FXML private TableColumn<Student, String> colStudentGrade;
    @FXML private TableColumn<Student, String> colStudentStream;
    @FXML private TableColumn<Student, String> colStudentLanguage;
    @FXML private TableColumn<Student, String> colStudentEmail;
    @FXML private TextField studentNameField;
    @FXML private ComboBox<String> studentGradeCombo;
    @FXML private ComboBox<String> studentStreamCombo;
    @FXML private ComboBox<String> studentLanguageCombo;
    @FXML private ComboBox<String> studentSectionCombo;
    @FXML private TextField studentEmailField;
    @FXML private ListView<String> studentSubjectsList;

    // Teacher Management
    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, String> colTeacherId;
    @FXML private TableColumn<Teacher, String> colTeacherName;
    @FXML private TableColumn<Teacher, String> colTeacherEmail;
    @FXML private TextField teacherNameField;
    @FXML private TextField teacherEmailField;
    @FXML private TextField teacherSubjectField;
    @FXML private ListView<String> teacherClassesList;

    // Assignment Management
    @FXML private ComboBox<String> assignTeacherCombo;
    @FXML private ComboBox<String> assignClassCombo;
    @FXML private ComboBox<String> assignSubjectCombo;
    @FXML private ComboBox<String> assignStreamCombo;
    @FXML private ComboBox<String> assignLanguageCombo;
    @FXML private ComboBox<String> assignSectionCombo;
    @FXML private TableView<AssignmentRow> assignmentTable;
    @FXML private TableColumn<AssignmentRow, String> colAssignTeacher;
    @FXML private TableColumn<AssignmentRow, String> colAssignClass;
    @FXML private TableColumn<AssignmentRow, String> colAssignSubject;

    @FXML private MenuItem logoutMenuItem;
    @FXML private TabPane tabPane;
    @FXML private Tab studentTab;
    @FXML private Tab teacherTab;
    @FXML private Tab assignmentTab;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private ObservableList<Teacher> teacherList = FXCollections.observableArrayList();
    private ObservableList<AssignmentRow> assignmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up grade, stream, language, section combos
        studentGradeCombo.getItems().setAll("9", "10", "11", "12");
        studentStreamCombo.getItems().setAll("Natural", "Social");
        studentLanguageCombo.getItems().setAll("Amharic", "Tigrigna");
        studentSectionCombo.getItems().setAll("A", "B", "C", "D"); // Example sections
        // Hide stream/language combos for grades 9/10
        studentGradeCombo.setOnAction(e -> updateStreamLanguageVisibility());
        updateStreamLanguageVisibility();

        // Prevent tabs from being closed
        if (tabPane != null) {
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        }

        // Set up table columns
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colStudentGrade.setCellValueFactory(new PropertyValueFactory<>("classLevel"));
        colStudentStream.setCellValueFactory(new PropertyValueFactory<>("stream"));
        colStudentLanguage.setCellValueFactory(new PropertyValueFactory<>("language"));
        colStudentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        studentTable.setItems(studentList);
        loadStudents();
        // Student selection listener to populate form
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                studentNameField.setText(newSel.getFullName());
                studentGradeCombo.setValue(newSel.getClassLevel());
                updateStreamLanguageVisibility();
                studentStreamCombo.setValue(newSel.getStream());
                studentLanguageCombo.setValue(newSel.getLanguage());
                studentSectionCombo.setValue(newSel.getSection()); // Assuming getSection()
                studentEmailField.setText(newSel.getEmail());
                loadSubjectsForStudent(newSel);
            } else {
                // Clear fields if no student is selected
                studentNameField.clear();
                studentGradeCombo.setValue(null);
                studentStreamCombo.setValue(null);
                studentLanguageCombo.setValue(null);
                studentSectionCombo.setValue(null);
                studentEmailField.clear();
                studentSubjectsList.getItems().clear();
            }
        });
        // Listener for default email generation
        studentNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                generateDefaultEmail(newVal.trim());
            } else {
                studentEmailField.clear();
            }
        });
        // Set up teacher table columns
        colTeacherId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colTeacherName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colTeacherEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        teacherTable.setItems(teacherList);
        loadTeachers();
        // Assignment tab setup
        loadAssignmentCombos();
        assignClassCombo.setOnAction(e -> populateSubjectsForAssignClass());
        assignStreamCombo.setOnAction(e -> populateSubjectsForAssignClass());
        assignLanguageCombo.setOnAction(e -> populateSubjectsForAssignClass());
        assignSectionCombo.setOnAction(e -> populateSubjectsForAssignClass());
        colAssignTeacher.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().teacherName));
        colAssignClass.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().classLevel + (cell.getValue().section != null ? " Section " + cell.getValue().section : "")));
        colAssignSubject.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().subject));
        assignmentTable.setItems(assignmentList);
        loadAssignments();
        assignmentTable.setRowFactory(tv -> new TableRow<>());

        // Listener for assignment table selection to populate form
        assignmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // Populate teacher combo - find the teacher in the combo box items by name
                String teacherName = newSel.teacherName;
                for (String item : assignTeacherCombo.getItems()) {
                    if (item.contains(teacherName)) {
                        assignTeacherCombo.setValue(item);
                        break;
                    }
                }
                assignClassCombo.setValue(newSel.classLevel);
                assignSectionCombo.setValue(newSel.section != null ? newSel.section : "(All)"); // Set (All) if section is null
                // Note: Subject combo population depends on class/stream/language selection,
                // which happens when class combo value is set. May need to re-select class
                // or manually populate subject here if dependencies are complex.
                 populateSubjectsForAssignClass(); // Repopulate subjects based on selected class/stream/language
                 assignSubjectCombo.setValue(newSel.subject);
            } else {
                // Clear fields if no assignment is selected
                assignTeacherCombo.setValue(null);
                assignClassCombo.setValue(null);
                assignSectionCombo.setValue(null);
                assignSubjectCombo.setValue(null);
            }
        });
    }

    private void updateStreamLanguageVisibility() {
        String grade = studentGradeCombo.getValue();
        boolean show = "11".equals(grade) || "12".equals(grade);
        studentStreamCombo.setDisable(!show);
        studentLanguageCombo.setDisable(!show);
    }

    private void loadStudents() {
        studentList.clear();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM student";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    studentList.add(new Student(
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
                        rs.getString("section") // Assuming 'section' column in DB
                    ));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    @FXML
    private void handleAddStudent() {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "INSERT INTO student (user_id, username, password_hash, student_id, full_name, dob, class_level, email, stream, language, section) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String userId = java.util.UUID.randomUUID().toString();
                String fullName = studentNameField.getText();
                String username = generateUsername(fullName); // Generate username from name
                String password = "pass123"; // Default password
                String studentId = "S" + System.currentTimeMillis();
                LocalDate dob = LocalDate.of(2000, 1, 1); // Placeholder
                String grade = studentGradeCombo.getValue();
                String email = studentEmailField.getText();
                String stream = ("11".equals(grade) || "12".equals(grade)) ? studentStreamCombo.getValue() : null;
                String language = ("11".equals(grade) || "12".equals(grade)) ? studentLanguageCombo.getValue() : null;
                String section = studentSectionCombo.getValue();
                stmt.setString(1, userId);
                stmt.setString(2, username);
                stmt.setString(3, password);
                stmt.setString(4, studentId);
                stmt.setString(5, fullName);
                stmt.setDate(6, java.sql.Date.valueOf(dob));
                stmt.setString(7, grade);
                stmt.setString(8, email);
                stmt.setString(9, stream);
                stmt.setString(10, language);
                stmt.setString(11, section);
                stmt.executeUpdate();
                loadStudents();
                new Alert(Alert.AlertType.INFORMATION, "Student added.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to add student.").showAndWait();
        }
    }

    @FXML
    private void handleEditStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a student to edit.").showAndWait();
            return;
        }
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "UPDATE student SET full_name=?, class_level=?, email=?, stream=?, language=?, section=? WHERE user_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String grade = studentGradeCombo.getValue();
                String stream = ("11".equals(grade) || "12".equals(grade)) ? studentStreamCombo.getValue() : null;
                String language = ("11".equals(grade) || "12".equals(grade)) ? studentLanguageCombo.getValue() : null;
                String section = studentSectionCombo.getValue();
                stmt.setString(1, studentNameField.getText());
                stmt.setString(2, grade);
                stmt.setString(3, studentEmailField.getText());
                stmt.setString(4, stream);
                stmt.setString(5, language);
                stmt.setString(6, section);
                stmt.setString(7, selected.getUserId());
                stmt.executeUpdate();
                loadStudents();
                new Alert(Alert.AlertType.INFORMATION, "Student updated.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to update student.").showAndWait();
        }
    }

    @FXML
    private void handleDeleteStudent() {
        Student selected = (Student) studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a student to delete.").showAndWait();
            return;
        }
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "DELETE FROM student WHERE user_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, selected.getUserId());
                stmt.executeUpdate();
                loadStudents();
                new Alert(Alert.AlertType.INFORMATION, "Student deleted.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    private void loadTeachers() {
        teacherList.clear();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT * FROM teacher WHERE username != 'admin'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    teacherList.add(new Teacher(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        new java.util.ArrayList<>() // classLevels not needed for admin view
                    ));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    @FXML
    private void handleAddTeacher() {
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "INSERT INTO teacher (user_id, username, password_hash, full_name, email) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Generate a shorter teacher ID: T + timestamp (last 6 digits)
                String userId = "T" + String.format("%06d", System.currentTimeMillis() % 1000000);
                String username = teacherEmailField.getText();
                String password = "pass123"; // Default password
                String fullName = teacherNameField.getText();
                String email = teacherEmailField.getText();
                stmt.setString(1, userId);
                stmt.setString(2, username);
                stmt.setString(3, password);
                stmt.setString(4, fullName);
                stmt.setString(5, email);
                stmt.executeUpdate();
                
                // Clear the input fields
                teacherNameField.clear();
                teacherEmailField.clear();
                
                // Refresh both teacher lists
                loadTeachers();
                loadAssignmentCombos();
                
                new Alert(Alert.AlertType.INFORMATION, "Teacher added.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to add teacher.").showAndWait();
        }
    }

    @FXML
    private void handleEditTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a teacher to edit.").showAndWait();
            return;
        }
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "UPDATE teacher SET full_name=?, email=? WHERE user_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teacherNameField.getText());
                stmt.setString(2, teacherEmailField.getText());
                stmt.setString(3, selected.getUserId());
                stmt.executeUpdate();
                loadTeachers();
                new Alert(Alert.AlertType.INFORMATION, "Teacher updated.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to update teacher.").showAndWait();
        }
    }

    @FXML
    private void handleDeleteTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a teacher to delete.").showAndWait();
            return;
        }
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "DELETE FROM teacher WHERE user_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, selected.getUserId());
                stmt.executeUpdate();
                loadTeachers();
                new Alert(Alert.AlertType.INFORMATION, "Teacher deleted.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    // --- Assignment Management Logic ---
    private void loadAssignmentCombos() {
        assignTeacherCombo.getItems().clear();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT user_id, full_name FROM teacher WHERE username != 'admin'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    assignTeacherCombo.getItems().add(rs.getString("user_id") + ": " + rs.getString("full_name"));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
        // Classes (grades)
        assignClassCombo.getItems().setAll("9", "10", "11", "12");
        // Sections
        assignSectionCombo.getItems().setAll("A", "B", "C", "D", "(All)"); // Added (All) option
        // Streams and Languages
        assignStreamCombo.getItems().setAll("Natural", "Social");
        assignLanguageCombo.getItems().setAll("Amharic", "Tigrigna", "(All)"); // Added (All) option
        // Subjects will be populated when a class is selected
        assignSubjectCombo.getItems().clear();
    }

    private void populateSubjectsForAssignClass() {
        assignSubjectCombo.getItems().clear();
        String selectedGrade = assignClassCombo.getValue();
        String selectedStream = assignStreamCombo.getValue();
        String selectedLanguage = assignLanguageCombo.getValue();
        String selectedSection = assignSectionCombo.getValue();

        if (selectedGrade == null) {
            assignStreamCombo.setDisable(true);
            assignLanguageCombo.setDisable(true);
            assignSectionCombo.setDisable(true);
            return;
        }

        try (Connection conn = DatabaseService.getConnection()) {
            String sql;
            if (selectedGrade.equals("9") || selectedGrade.equals("10")) {
                // For grades 9 and 10, only filter by grade
                sql = "SELECT DISTINCT title FROM course WHERE grade = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, selectedGrade);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        assignSubjectCombo.getItems().add(rs.getString("title"));
                    }
                }
                assignStreamCombo.setDisable(true);
                assignLanguageCombo.setDisable(true);
                assignSectionCombo.setDisable(false);
            } else {
                // For grades 11 and 12, filter by grade, stream, and language (if selected)
                assignStreamCombo.setDisable(false);
                assignLanguageCombo.setDisable(false);
                assignSectionCombo.setDisable(false);

                // First, get common subjects (like Physical Education) that are available for all streams and languages
                sql = "SELECT DISTINCT title FROM course WHERE grade = ? AND stream IS NULL AND language IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, selectedGrade);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        assignSubjectCombo.getItems().add(rs.getString("title"));
                    }
                }

                // Then get stream and language specific subjects
                StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT title FROM course WHERE grade = ?");
                List<String> params = new ArrayList<>();
                params.add(selectedGrade);

                if (selectedStream != null && !selectedStream.isEmpty()) {
                    sqlBuilder.append(" AND (stream = ? OR stream IS NULL)");
                    params.add(selectedStream);
                }
                if (selectedLanguage != null && !selectedLanguage.isEmpty() && !selectedLanguage.equals("(All)")) {
                    sqlBuilder.append(" AND (language = ? OR language IS NULL)");
                    params.add(selectedLanguage);
                }

                sql = sqlBuilder.toString();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setString(i + 1, params.get(i));
                    }
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String subject = rs.getString("title");
                        if (!assignSubjectCombo.getItems().contains(subject)) {
                            assignSubjectCombo.getItems().add(subject);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    @FXML
    private void handleAssignTeacher() {
        String teacherComboVal = assignTeacherCombo.getValue();
        String classLevel = assignClassCombo.getValue();
        String subject = assignSubjectCombo.getValue();
        String section = assignSectionCombo.getValue(); // Get selected section

        if (teacherComboVal == null || classLevel == null || subject == null || section == null) {
            new Alert(Alert.AlertType.WARNING, "Please select teacher, class, section, and subject.").showAndWait();
            return;
        }
        String teacherId = teacherComboVal.split(":")[0];
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "INSERT INTO teacher_class_subject (teacher_id, class_level, subject, section) VALUES (?, ?, ?, ?)"; // Include section in INSERT
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teacherId);
                stmt.setString(2, classLevel);
                stmt.setString(3, subject);
                stmt.setString(4, section.equals("(All)") ? null : section); // Store null if (All) is selected
                stmt.executeUpdate();
                loadAssignments();
                new Alert(Alert.AlertType.INFORMATION, "Assignment added.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to add assignment.").showAndWait();
        }
    }

    private void loadAssignments() {
        assignmentList.clear();
        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "SELECT t.user_id, t.full_name, a.class_level, a.subject, a.section, a.id FROM teacher_class_subject a JOIN teacher t ON a.teacher_id = t.user_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    assignmentList.add(new AssignmentRow(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("full_name"),
                        rs.getString("class_level"),
                        rs.getString("subject"),
                        rs.getString("section")
                    ));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    @FXML
    private void handleDeleteAssignment() {
        AssignmentRow selected = assignmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an assignment to delete.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Assignment");
        confirm.setContentText("Are you sure you want to delete the assignment for:\n" +
                             "Teacher: " + selected.teacherName + "\n" +
                             "Class: " + selected.classLevel + "\n" +
                             "Subject: " + selected.subject + "\n\n" +
                             "This will remove the link between this teacher, class, subject, and section,\n" +
                             "and delete associated student marks for this combination. The assessment record itself will NOT be deleted.\n" +
                             "This action cannot be undone.");

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseService.getConnection()) {
                    conn.setAutoCommit(false); // Start transaction
                    try {
                        // 1. Find assessment IDs for this teacher, subject, and applicable to this class/section
                        String getAssessmentsSql = "SELECT DISTINCT ac.assessment_id FROM assessment_class ac " +
                                                   "JOIN assessment a ON ac.assessment_id = a.assessment_id " +
                                                   "WHERE a.teacher_id = ? AND a.subject = ? AND ac.class_level = ?";

                        List<String> assessmentIds = new ArrayList<>();
                        try (PreparedStatement stmt = conn.prepareStatement(getAssessmentsSql)) {
                            stmt.setString(1, selected.teacherId);
                            stmt.setString(2, selected.subject);
                            stmt.setString(3, selected.classLevel + (selected.section != null && !selected.section.equals("(All)") ? selected.section : " (All Sections)")); // Construct class_level string
                            ResultSet rs = stmt.executeQuery();
                            while (rs.next()) {
                                assessmentIds.add(rs.getString("assessment_id"));
                            }
                        }

                        if (!assessmentIds.isEmpty()) {
                            // 2. Delete results for students in this class/section for these assessments
                            // We need to find the student IDs in this class/section first.
                             String getStudentIdsSql = "SELECT user_id FROM student WHERE class_level = ? AND (section = ? OR ? IS NULL AND section IS NULL)";
                             List<String> studentIds = new ArrayList<>();
                             try (PreparedStatement stmt = conn.prepareStatement(getStudentIdsSql)) {
                                stmt.setString(1, selected.classLevel);
                                if (selected.section != null && !selected.section.equals("(All)")) {
                                    stmt.setString(2, selected.section);
                                    stmt.setString(3, null); // section IS NULL part
                                } else {
                                    stmt.setString(2, null); // section = ? part
                                    stmt.setString(3, null); // section IS NULL part - handle (All)
                                }
                                 ResultSet rs = stmt.executeQuery();
                                 while(rs.next()){
                                     studentIds.add(rs.getString("user_id"));
                                 }
                             }

                            if (!studentIds.isEmpty()) {
                                 String deleteResultsSql = "DELETE FROM result WHERE assessment_id IN (" +
                                                           String.join(",", Collections.nCopies(assessmentIds.size(), "?")) + ") AND student_id IN (" +
                                                           String.join(",", Collections.nCopies(studentIds.size(), "?")) + ")";

                                 try (PreparedStatement stmt = conn.prepareStatement(deleteResultsSql)) {
                                     int paramIndex = 1;
                                     for (String id : assessmentIds) { stmt.setString(paramIndex++, id); }
                                     for (String id : studentIds) { stmt.setString(paramIndex++, id); }
                                     stmt.executeUpdate();
                                 }
                            }

                            // 3. Delete specific entries in assessment_class for this class/section
                            String deleteAssessmentClassSql = "DELETE FROM assessment_class WHERE assessment_id IN (" +
                                                              String.join(",", Collections.nCopies(assessmentIds.size(), "?")) + ") AND class_level = ?";
                            try (PreparedStatement stmt = conn.prepareStatement(deleteAssessmentClassSql)) {
                                 int paramIndex = 1;
                                 for (String id : assessmentIds) { stmt.setString(paramIndex++, id); }
                                 stmt.setString(paramIndex++, selected.classLevel + (selected.section != null && !selected.section.equals("(All)") ? selected.section : " (All Sections)")); // Construct class_level string again
                                 stmt.executeUpdate();
                            }
                        }

                        // 4. Delete the assignment
                        String deleteAssignmentSql = "DELETE FROM teacher_class_subject WHERE id=?";
                        try (PreparedStatement stmt = conn.prepareStatement(deleteAssignmentSql)) {
                            stmt.setInt(1, selected.id);
                            stmt.executeUpdate();
                        }

                        conn.commit(); // Commit transaction
                        loadAssignments();
                        new Alert(Alert.AlertType.INFORMATION, "Assignment link and associated data deleted successfully.").showAndWait();
                    } catch (Exception e) {
                        conn.rollback(); // Rollback on error
                        throw e; // Re-throw to be caught by the outer catch block
                    } finally {
                        conn.setAutoCommit(true); // Reset auto-commit
                    }
                } catch (Exception e) {
                    // e.printStackTrace(); // Removed logger
                    new Alert(Alert.AlertType.ERROR, "Failed to delete assignment: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void loadSubjectsForStudent(Student student) {
        studentSubjectsList.getItems().clear();
        if (student == null) return;
        try (Connection conn = DatabaseService.getConnection()) {
            String sql;
            if (student.getClassLevel().equals("9") || student.getClassLevel().equals("10")) {
                sql = "SELECT title FROM course WHERE grade = ?";
            } else {
                sql = "SELECT title FROM course WHERE grade = ? AND stream = ? AND (language IS NULL OR language = ?)";
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student.getClassLevel());
                if (student.getClassLevel().equals("9") || student.getClassLevel().equals("10")) {
                    // nothing more
                } else {
                    stmt.setString(2, student.getStream());
                    stmt.setString(3, student.getLanguage());
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    studentSubjectsList.getItems().add(rs.getString("title"));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    private void generateDefaultEmail(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            studentEmailField.clear();
            return;
        }
        String[] names = fullName.trim().toLowerCase().split("\\s+");
        if (names.length > 1) {
            String firstName = names[0];
            String lastName = names[names.length - 1];
            studentEmailField.setText(firstName + "." + lastName + "@school.com");
        } else {
            studentEmailField.setText(names[0] + "@school.com");
        }
    }
    private String generateUsername(String fullName) {
         // Simple username generation based on email for now
         if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String email = studentEmailField.getText(); // Assumes email is already generated or entered
        if (email != null && !email.isEmpty()) {
            return email.split("@")[0]; // Use part before @ as username
        } else {
            // Fallback: use a simple version of the name
            return fullName.trim().toLowerCase().replaceAll("\\s+", "");
        }
    }

    @FXML
    private void handleEditAssignment() {
        AssignmentRow selected = assignmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select an assignment to edit.").showAndWait();
            return;
        }

        String teacherComboVal = assignTeacherCombo.getValue();
        String classLevel = assignClassCombo.getValue();
        String subject = assignSubjectCombo.getValue();
        String section = assignSectionCombo.getValue();

        if (teacherComboVal == null || classLevel == null || subject == null || section == null) {
            new Alert(Alert.AlertType.WARNING, "Please select teacher, class, section, and subject for editing.").showAndWait();
            return;
        }

        String teacherId = teacherComboVal.split(":")[0];

        try (Connection conn = DatabaseService.getConnection()) {
            String sql = "UPDATE teacher_class_subject SET teacher_id=?, class_level=?, subject=?, section=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teacherId);
                stmt.setString(2, classLevel);
                stmt.setString(3, subject);
                stmt.setString(4, section.equals("(All)") ? null : section); // Store null if (All) is selected
                stmt.setInt(5, selected.id);
                stmt.executeUpdate();
                loadAssignments();
                new Alert(Alert.AlertType.INFORMATION, "Assignment updated.").showAndWait();
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to update assignment.").showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        Rectangle2D bound = Screen.getPrimary().getBounds();
        double width = bound.getWidth()  - 5;
        double height = bound.getHeight() - 75;
        try {
            // Load the login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            // Get the current scene and set the root
            // Assuming the AdminDashboard is in a Scene
            Stage stage = (Stage) studentTable.getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/view/school-theme.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Grade Manager - Login");
            stage.show();
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to logout.").showAndWait();
        }
    }

    @FXML
    private void showTeacherTab() {
        if (tabPane != null && teacherTab != null) {
            tabPane.getSelectionModel().select(teacherTab);
        }
    }

    @FXML
    private void showStudentTab() {
        if (tabPane != null && studentTab != null) {
            tabPane.getSelectionModel().select(studentTab);
        }
    }

    @FXML
    private void showAssignmentTab() {
        if (tabPane != null && assignmentTab != null) {
            tabPane.getSelectionModel().select(assignmentTab);
        }
    }

    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
    }

    public static class AssignmentRow {
        public final int id;
        public final String teacherId;
        public final String teacherName;
        public final String classLevel;
        public final String subject;
        public final String section;
        public AssignmentRow(int id, String teacherId, String teacherName, String classLevel, String subject, String section) {
            this.id = id;
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.classLevel = classLevel;
            this.subject = subject;
            this.section = section;
        }
    }
} 