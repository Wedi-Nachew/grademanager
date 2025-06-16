package com.school.grademanager.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.school.grademanager.model.Assessment;
import com.school.grademanager.model.Result;
import com.school.grademanager.model.Student;
import com.school.grademanager.model.StudentAssessmentRow;
import com.school.grademanager.service.AssessmentService;
import com.school.grademanager.service.DatabaseService;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class StudentDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TextField nameField;
    @FXML private TextField classField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label profileStatusLabel;
    @FXML private TableView<StudentAssessmentRow> assessmentTable;
    @FXML private TableColumn<StudentAssessmentRow, Integer> colSN;
    @FXML private TableColumn<StudentAssessmentRow, String> colTitle;
    @FXML private TableColumn<StudentAssessmentRow, String> colType;
    @FXML private TableColumn<StudentAssessmentRow, Double> colTotalMarks;
    @FXML private TableColumn<StudentAssessmentRow, Double> colMarksObtained;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private ComboBox<String> subjectCombo;
    @FXML private TabPane tabPane;
    @FXML private Tab profileTab;
    @FXML private Tab studentTab;
    @FXML private Tab assessmentTab;
    @FXML private VBox assessmentDetailsBox;
    @FXML private Label assessmentTitleLabel;
    @FXML private Label assessmentTypeLabel;
    @FXML private Label assessmentDateLabel;
    @FXML private Label assessmentSubjectLabel;
    @FXML private Label assessmentMarksLabel;
    @FXML private Label assessmentStatusLabel;
    @FXML private Label courseTitleLabel;
    @FXML private Label instructorLabel;
    @FXML private Label academicYearLabel;
    @FXML private Label totalMarksLabel;
    @FXML private Label totalResultsLabel;

    private ObservableList<StudentAssessmentRow> studentAssessmentRows = FXCollections.observableArrayList();
    private AssessmentService assessmentService = new AssessmentService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // Temporary static reference to the logged-in student
    public static Student loggedInStudent;

    public void initialize() {
        if (loggedInStudent != null) {
            welcomeLabel.setText("Welcome, " + loggedInStudent.getFullName() + "!");
            nameField.setText(loggedInStudent.getFullName());
            classField.setText(loggedInStudent.getClassLevel() + (loggedInStudent.getSection() != null ? loggedInStudent.getSection() : ""));
            emailField.setText(loggedInStudent.getEmail());
            passwordField.setText(loggedInStudent.getPasswordHash());
            loadSemestersAndSubjects();
        }

        // Prevent tabs from being closed
        if (tabPane != null) {
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        }

        // Assessment Table setup
        if (assessmentTable != null) {
            colSN.setCellValueFactory(cellData -> new SimpleIntegerProperty(assessmentTable.getItems().indexOf(cellData.getValue()) + 1).asObject());
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colType.setCellValueFactory(new PropertyValueFactory<>("assessmentType"));
            colTotalMarks.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
            
            // Customize cell factory for colMarksObtained to handle null values
            colMarksObtained.setCellValueFactory(new PropertyValueFactory<>("marksObtained"));
            colMarksObtained.setCellFactory(tc -> new TableCell<StudentAssessmentRow, Double>() {
                @Override
                protected void updateItem(Double marks, boolean empty) {
                    super.updateItem(marks, empty);
                    if (empty || marks == null) {
                        setText("-"); // Display dash for null or empty cells
                    } else {
                        setText(String.format("%.1f", marks));
                    }
                }
            });

            assessmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    showCourseInfoForAssessment(newSel.getAssessment());
                } else {
                    clearCourseInfo();
                }
            });
            
            assessmentService.getResults().addListener((ListChangeListener<? super Result>) c -> {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                        loadAssessmentsForStudent();
                        break;
                    }
                }
            });

            assessmentTable.setItems(studentAssessmentRows);
        }

        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        if (semesterCombo != null && subjectCombo != null) {
            semesterCombo.setOnAction(event -> loadAssessmentsForStudent());
            subjectCombo.setOnAction(event -> loadAssessmentsForStudent());
        }
    }

    private void loadSemestersAndSubjects() {
        semesterCombo.getItems().clear();
        subjectCombo.getItems().clear();

        if (loggedInStudent == null) return;

        try (Connection conn = DatabaseService.getConnection()) {
            // Load semesters from assessments the student's class/section has (matching combined class_level)
            String semesterSql = "SELECT DISTINCT a.semester FROM assessment a " +
                                 "JOIN assessment_class ac ON a.assessment_id = ac.assessment_id " +
                                 "WHERE ac.class_level = ?";

            try (PreparedStatement stmt = conn.prepareStatement(semesterSql)) {
                // Use combined class level and section for matching with assessment_class
                stmt.setString(1, loggedInStudent.getClassLevel() + (loggedInStudent.getSection() != null ? loggedInStudent.getSection() : ""));

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    semesterCombo.getItems().add(rs.getString("semester"));
                }
            }

            // Load subjects from courses the student's grade, stream, and language has
            String subjectSql = "SELECT DISTINCT title FROM course " +
                                 "WHERE grade = ? AND (stream IS NULL OR stream = ?) AND (language IS NULL OR language = ?) ";

            try (PreparedStatement stmt = conn.prepareStatement(subjectSql)) {
                stmt.setString(1, loggedInStudent.getClassLevel());
                stmt.setString(2, loggedInStudent.getStream());
                stmt.setString(3, loggedInStudent.getLanguage());

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    subjectCombo.getItems().add(rs.getString("title"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAssessmentsForStudent() {
        assessmentTable.getItems().clear();
        studentAssessmentRows.clear();
        clearCourseInfo();

        String selectedSemester = semesterCombo.getValue();
        String selectedSubject = subjectCombo.getValue();

        if (loggedInStudent == null || selectedSemester == null || selectedSubject == null) return;

        try (Connection conn = DatabaseService.getConnection()) {
            // Fetch assessments for the student's class/section, selected semester, and subject
            String assessmentSql = "SELECT DISTINCT a.* FROM assessment a " +
                                 "JOIN assessment_class ac ON a.assessment_id = ac.assessment_id " +
                                 "WHERE (ac.class_level = ? OR ac.class_level = ?) " + // Match exact class or grade level
                                 "AND a.semester = ? AND a.subject = ?";

            try (PreparedStatement stmt = conn.prepareStatement(assessmentSql)) {
                String exactClassLevel = loggedInStudent.getClassLevel() + (loggedInStudent.getSection() != null ? loggedInStudent.getSection() : "");
                String gradeLevel = loggedInStudent.getClassLevel(); // Just the grade level
                
                stmt.setString(1, exactClassLevel);
                stmt.setString(2, gradeLevel);
                stmt.setString(3, selectedSemester);
                stmt.setString(4, selectedSubject);

                ResultSet rs = stmt.executeQuery();
                List<Assessment> fetchedAssessments = new java.util.ArrayList<>();
                while (rs.next()) {
                    // Manually load class levels for each assessment fetched
                    List<String> assessmentClassLevels = new java.util.ArrayList<>();
                    String classLevelSql = "SELECT class_level FROM assessment_class WHERE assessment_id = ?";
                    try (PreparedStatement clStmt = conn.prepareStatement(classLevelSql)) {
                        clStmt.setString(1, rs.getString("assessment_id"));
                        ResultSet clRs = clStmt.executeQuery();
                        while(clRs.next()){
                            assessmentClassLevels.add(clRs.getString("class_level"));
                        }
                    }

                    Assessment assessment = new Assessment(
                        rs.getString("assessment_id"),
                        rs.getString("title"),
                        rs.getDate("date").toLocalDate(),
                        rs.getInt("total_marks"),
                        rs.getString("teacher_id"),
                        com.school.grademanager.model.AssessmentType.valueOf(rs.getString("assessment_type")),
                        rs.getString("subject"),
                        rs.getString("academic_year"),
                        rs.getString("semester"),
                        assessmentClassLevels
                    );

                    fetchedAssessments.add(assessment);
                }

                // Now fetch results for these assessments and create StudentAssessmentRows
                for (Assessment assessment : fetchedAssessments) {
                    // Fetch the student's result for this assessment
                    Double marksObtained = null; // Use Double to allow null
                    String resultSql = "SELECT marks_obtained FROM result WHERE assessment_id = ? AND student_id = ?";
                    try (PreparedStatement resultStmt = conn.prepareStatement(resultSql)) {
                        resultStmt.setString(1, assessment.getAssessmentId());
                        resultStmt.setString(2, loggedInStudent.getStudentId());

                        ResultSet resultRs = resultStmt.executeQuery();
                        if (resultRs.next()) {
                            marksObtained = resultRs.getDouble("marks_obtained");
                        }
                    }

                    // Create StudentAssessmentRow and add to the list
                    studentAssessmentRows.add(new StudentAssessmentRow(assessment, marksObtained));
                }
            }

            updateTotals();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCourseInfoForAssessment(Assessment assessment) {
        clearCourseInfo();
        if (assessment == null) return;

        try (Connection conn = DatabaseService.getConnection()) {
            String courseSql = "SELECT c.title, t.full_name FROM course c " +
                               "JOIN teacher_class_subject tcs ON c.grade = tcs.class_level AND c.title = tcs.subject " +
                               "JOIN teacher t ON tcs.teacher_id = t.user_id " +
                               "WHERE c.title = ? AND c.grade = ? AND (c.stream IS NULL OR c.stream = ?) AND (c.language IS NULL OR c.language = ?)";

            try (PreparedStatement stmt = conn.prepareStatement(courseSql)) {
                stmt.setString(1, assessment.getSubject());
                stmt.setString(2, loggedInStudent.getClassLevel());
                stmt.setString(3, loggedInStudent.getStream());
                stmt.setString(4, loggedInStudent.getLanguage());

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    courseTitleLabel.setText("Course: " + rs.getString("title"));
                    instructorLabel.setText("Instructor: " + rs.getString("full_name"));
                    academicYearLabel.setText("Academic Year: " + assessment.getAcademicYear());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearCourseInfo() {
        courseTitleLabel.setText("Course: ");
        instructorLabel.setText("Instructor: ");
        academicYearLabel.setText("Academic Year: ");
    }

    private void updateTotals() {
        if (studentAssessmentRows.isEmpty()) {
            totalMarksLabel.setText("Total Marks: 0");
            totalResultsLabel.setText("Total Obtained: 0");
            return;
        }

        double totalPossible = 0;
        double totalObtained = 0; // Changed to double for calculation
        for (StudentAssessmentRow row : studentAssessmentRows) {
            totalPossible += row.getTotalMarks();
            // Add mark to total only if it's not null
            if (row.getMarksObtained() != null) {
                totalObtained += row.getMarksObtained();
            }
        }
        totalMarksLabel.setText("Total Marks: " + totalPossible);
        totalResultsLabel.setText("Total Obtained: " + totalObtained);
    }

    @FXML
    private void handleSaveProfile() {
        if (loggedInStudent != null) {
            try (Connection conn = DatabaseService.getConnection()) {
                String sql = "UPDATE student SET full_name = ?, email = ?, password_hash = ? WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, emailField.getText());
                    stmt.setString(3, passwordField.getText());
                    stmt.setString(4, loggedInStudent.getUserId());
                    stmt.executeUpdate();
                    
                    loggedInStudent.setFullName(nameField.getText());
                    loggedInStudent.setEmail(emailField.getText());
                    loggedInStudent.setPasswordHash(passwordField.getText());
                    
                    profileStatusLabel.setText("Profile saved successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    profileStatusLabel.setText("Error saving profile!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                profileStatusLabel.setText("Error saving profile!");
            }
        }
    }

    @FXML
    private void handleExportAssessments() {
        new Alert(Alert.AlertType.INFORMATION, "Export Assessments functionality to be implemented.").showAndWait();
    }

    @FXML
    private void handleLogout() {
        Rectangle2D bound = Screen.getPrimary().getBounds();
        double width = bound.getWidth()  - 5;
        double height = bound.getHeight() - 75;
        try {
            loggedInStudent = null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/view/school-theme.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Grade Manager - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleShowPassword() {
        boolean show = showPasswordCheckBox.isSelected();
        passwordTextField.setVisible(show);
        passwordTextField.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
    }

    @FXML
    private void showProfileTab() {
        if (tabPane != null && profileTab != null) {
            tabPane.getSelectionModel().select(profileTab);
        }
    }

    @FXML
    private void showAssessmentsTab() {
        if (tabPane != null && assessmentTab != null) {
            tabPane.getSelectionModel().select(assessmentTab);
            loadAssessmentsForStudent();
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
