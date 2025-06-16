package com.school.grademanager.controller;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school.grademanager.model.Assessment;
import com.school.grademanager.model.AssessmentType;
import com.school.grademanager.model.Result;
import com.school.grademanager.model.Student;
import com.school.grademanager.model.Teacher;
import com.school.grademanager.service.AssessmentService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TeacherDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label profileStatusLabel;
    @FXML private TableView<Assessment> assessmentTable;
    @FXML private TableColumn<String, String> colAssessmentTitle;
    @FXML private TableColumn<String, String> colAssessmentDate;
    @FXML private TableColumn<Integer, Integer> colAssessmentTotal;
    @FXML private TableView<ResultRow> resultTable;
    @FXML private TableColumn<String, String> colResultStudent;
    @FXML private TableColumn<ResultRow, Double> colResultMarks;
    @FXML private TextField searchField;
    @FXML private TabPane tabPane;
    @FXML private Tab profileTab;
    @FXML private Tab studentTab;
    @FXML private Tab assessmentTab;
    @FXML private TableView<StudentGradebookRow> gradebookTable;
    @FXML private ComboBox<String> classFilterCombo;
    @FXML private TextField studentSearchField;

    private ObservableList<Student> students = FXCollections.observableArrayList();
    private ObservableList<ResultRow> resultRows = FXCollections.observableArrayList();
    private ObservableList<StudentGradebookRow> gradebookRows = FXCollections.observableArrayList();
    private AssessmentService assessmentService = new AssessmentService();
    private Map<String, List<String>> teacherAssignedClassesAndSections;

    // Temporary static reference to the logged-in user
    public static Teacher loggedInTeacher;

    public void initialize() {
        if (loggedInTeacher != null) {
            welcomeLabel.setText("Welcome, " + loggedInTeacher.getFullName() + "!");
            nameField.setText(loggedInTeacher.getFullName());
            emailField.setText(loggedInTeacher.getEmail());
            passwordField.setText(loggedInTeacher.getPasswordHash());
            loadStudentsForTeacher();
        }
        setupClassFilter();
        setupStudentSearch();
        setupGradebookTable();

        // Prevent tabs from being closed
        if (tabPane != null) {
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        }

        // Assessment Table setup
        if (assessmentTable != null) {
            colAssessmentTitle.setCellValueFactory(new PropertyValueFactory("title"));
            colAssessmentDate.setCellValueFactory(new PropertyValueFactory("date"));
            colAssessmentTotal.setCellValueFactory(new PropertyValueFactory("totalMarks"));
            assessmentTable.setItems(assessmentService.getAssessments());
            assessmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> showResultsForAssessment(newSel));
        }

        // Result Table setup
        if (resultTable != null) {
            colResultStudent.setCellValueFactory(new PropertyValueFactory("studentName"));
            colResultMarks.setCellValueFactory(new PropertyValueFactory<>("marksObtained"));
            colResultMarks.setCellFactory(tc -> new javafx.scene.control.cell.TextFieldTableCell<>(new javafx.util.converter.DoubleStringConverter()) {
                @Override
                public void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("-");
                        setGraphic(null);
                    } else {
                        setText(String.format("%.1f", item));
                    }
                }
            });
            resultTable.setItems(resultRows);
        }

        // Add search/filter functionality
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSearchStudents());
        }

        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    private void loadStudentsForTeacher() {
        students.clear();
        if (loggedInTeacher == null) return;

        // Fetch assigned classes and sections using the new UserService method
        try (java.sql.Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
             com.school.grademanager.service.UserService userService = new com.school.grademanager.service.UserService();
             teacherAssignedClassesAndSections = userService.getTeacherAssignedClassesAndSections(conn, loggedInTeacher.getUserId());
        } catch (Exception e) {
            // Handle error loading assigned classes
            return;
        }

        if (teacherAssignedClassesAndSections.isEmpty()) return; // Return if no classes assigned

        // Build SQL query to select students from assigned grades and sections
        StringBuilder sql = new StringBuilder("SELECT * FROM student WHERE (");
        boolean firstGrade = true;
        for (Map.Entry<String, List<String>> entry : teacherAssignedClassesAndSections.entrySet()) {
            if (!firstGrade) sql.append(" OR ");
            sql.append("(class_level = ? AND section IN (");
            boolean firstSection = true;
            for (String section : entry.getValue()) {
                if (!firstSection) sql.append(",");
                sql.append("?");
                firstSection = false;
            }
            sql.append("))");
            firstGrade = false;
        }
        sql.append(")");

        System.out.println("SQL Query for loading students: " + sql.toString()); // Log SQL query

        try (java.sql.Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
             try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                System.out.print("Parameters: "); // Log parameters
                for (Map.Entry<String, List<String>> entry : teacherAssignedClassesAndSections.entrySet()) {
                    stmt.setString(paramIndex++, entry.getKey());
                    System.out.print("Grade: " + entry.getKey() + ", ");
                    for (String section : entry.getValue()) {
                        stmt.setString(paramIndex++, section);
                        System.out.print("Section: " + section + ", ");
                    }
                }
                System.out.println(); // New line after parameters

                java.sql.ResultSet rs = stmt.executeQuery();
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
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }
    }

    private void setupClassFilter() {
        classFilterCombo.getItems().clear();
        classFilterCombo.getItems().add("Select Class");
        if (teacherAssignedClassesAndSections != null) {
             for (Map.Entry<String, List<String>> entry : teacherAssignedClassesAndSections.entrySet()) {
                 String grade = entry.getKey();
                 // Only add grade+section combinations if a specific section is assigned
                 for (String section : entry.getValue()) {
                     if (section != null) {
                        classFilterCombo.getItems().add(grade + section); // Add grade + section (e.g., 10A)
                     }
                 }
             }
        }
        classFilterCombo.setValue("Select Class");
        classFilterCombo.setOnAction(e -> filterAndSearchStudents());
    }

    private void setupStudentSearch() {
        studentSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSearchStudents());
    }

    private void filterAndSearchStudents() {
        String selectedFilter = classFilterCombo.getValue();
        String search = studentSearchField.getText() == null ? "" : studentSearchField.getText().toLowerCase();
        ObservableList<Student> filtered = FXCollections.observableArrayList();

        if (selectedFilter == null || selectedFilter.equals("Select Class")) {
            // Show all students loaded for the teacher, filtered by search term
            for (Student s : students) {
                boolean searchMatch = s.getFullName().toLowerCase().contains(search) || s.getStudentId().toLowerCase().contains(search);
                if (searchMatch) filtered.add(s);
            }
        } else {
            // Filter by selected grade/section and search term
            String selectedGrade = selectedFilter;
            String selectedSection = null;

            // Correctly parse grade and section from filter (e.g., "10B")
            int gradeLength = 0;
            while (gradeLength < selectedFilter.length() && Character.isDigit(selectedFilter.charAt(gradeLength))) {
                gradeLength++;
            }

            if (gradeLength > 0 && gradeLength < selectedFilter.length()) {
                 selectedGrade = selectedFilter.substring(0, gradeLength);
                 selectedSection = selectedFilter.substring(gradeLength);
            }

            for (Student s : students) {
                boolean gradeMatch = s.getClassLevel().equals(selectedGrade);
                boolean sectionMatch = (selectedSection == null) || (s.getSection() != null && s.getSection().equals(selectedSection));
                boolean searchMatch = s.getFullName().toLowerCase().contains(search) || s.getStudentId().toLowerCase().contains(search);

                if (gradeMatch && sectionMatch && searchMatch) filtered.add(s);
            }
        }

        refreshGradebookRows(filtered);
        setupGradebookTable(); // Keep setupGradebookTable() here to ensure columns are updated
    }

    private void refreshGradebookRows() {
        refreshGradebookRows(students);
    }

    private void refreshGradebookRows(ObservableList<Student> studentList) {
        gradebookRows.clear();
        for (Student s : studentList) {
            gradebookRows.add(new StudentGradebookRow(s, assessmentService));
        }
    }

    private void setupGradebookTable() {
        gradebookTable.getColumns().clear();
        ObservableList<Assessment> allAssessments = assessmentService.getAssessments();
        String selectedClassFilter = classFilterCombo != null ? classFilterCombo.getValue() : null;

        TableColumn<StudentGradebookRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().studentIdProperty());
        TableColumn<StudentGradebookRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
         // Display Class and Section in one column
        TableColumn<StudentGradebookRow, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
             cellData.getValue().student.getClassLevel() + (cellData.getValue().student.getSection() != null ? cellData.getValue().student.getSection() : "")
        ));

        gradebookTable.getColumns().addAll(idCol, nameCol, classCol);
        // Only show assessments that apply to the selected class filter
        for (Assessment a : allAssessments) {
            // Check if the assessment is applicable to the currently selected class filter
            boolean isAssessmentApplicableToFilter = false;
            if (selectedClassFilter == null || selectedClassFilter.equals("Select Class")) {
                 // If "Select Class" is selected, show assessments applicable to ANY of the teacher's assigned classes/sections
                 if (teacherAssignedClassesAndSections != null) {
                     for (Map.Entry<String, List<String>> entry : teacherAssignedClassesAndSections.entrySet()) {
                         String grade = entry.getKey();
                         for (String section : entry.getValue()) {
                             String combined = grade + (section != null ? section : " (All Sections)");
                             if (a.getClassLevels() != null && (a.getClassLevels().contains(combined) || a.getClassLevels().contains(grade))) { // Also check just grade
                                 isAssessmentApplicableToFilter = true;
                                 break;
                             }
                         }
                         if(isAssessmentApplicableToFilter) break;
                     }
                 }
            } else {
                // If a specific class filter is selected (e.g., "10B" or "10 (All Sections)"), check if the assessment is applicable to THIS filter
                 if (a.getClassLevels() != null && a.getClassLevels().contains(selectedClassFilter)) {
                     isAssessmentApplicableToFilter = true;
                 }
            }

            if (isAssessmentApplicableToFilter) {
                TableColumn<StudentGradebookRow, Double> markCol = new TableColumn<>(a.getTitle());
                // Use cellValueFactory with ObjectProperty and cellFactory to handle nulls
                markCol.setCellValueFactory(cellData -> cellData.getValue().getMarkProperty(a.getAssessmentId())); // Use ObjectProperty
                markCol.setCellFactory(tc -> new javafx.scene.control.cell.TextFieldTableCell<>(new javafx.util.converter.DoubleStringConverter()) {
                     @Override
                     public void updateItem(Double item, boolean empty) {
                         super.updateItem(item, empty);
                         if (empty || item == null) {
                             setText("-");
                             setGraphic(null);
                         } else {
                             setText(String.format("%.1f", item));
                         }
                     }
                });
                markCol.setOnEditCommit(event -> {
                    StudentGradebookRow row = event.getRowValue();
                    Assessment assessment = a; // Use the assessment from the outer loop
                    Double enteredMark = event.getNewValue(); // Can be null if cell is cleared

                    if (enteredMark != null && (enteredMark > assessment.getTotalMarks() || enteredMark < 0)) {
                        new Alert(Alert.AlertType.ERROR, (enteredMark > assessment.getTotalMarks() ? "Mark cannot exceed maximum: " + assessment.getTotalMarks() : "Mark cannot be less than 0.")).showAndWait();
                        gradebookTable.refresh(); // Simple refresh for now
                    } else {
                        row.setMark(assessment.getAssessmentId(), enteredMark != null ? enteredMark : null);
                        
                        // Save to database immediately
                        try (Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
                            String checkSql = "SELECT result_id FROM result WHERE assessment_id=? AND student_id=?";
                            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                                checkStmt.setString(1, assessment.getAssessmentId());
                                checkStmt.setString(2, row.student.getStudentId());
                                ResultSet rs = checkStmt.executeQuery();
                                
                                if (rs.next()) {
                                    // Update existing result
                                    String updateSql = "UPDATE result SET marks_obtained=? WHERE result_id=?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                        updateStmt.setDouble(1, enteredMark != null ? enteredMark : 0.0);
                                        updateStmt.setString(2, rs.getString("result_id"));
                                        updateStmt.executeUpdate();
                                    }
                                } else {
                                    // Insert new result
                                    String insertSql = "INSERT INTO result (result_id, assessment_id, student_id, marks_obtained) VALUES (?, ?, ?, ?)";
                                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                        String resultId = "r" + assessment.getAssessmentId() + row.student.getStudentId();
                                        insertStmt.setString(1, resultId);
                                        insertStmt.setString(2, assessment.getAssessmentId());
                                        insertStmt.setString(3, row.student.getStudentId());
                                        insertStmt.setDouble(4, enteredMark != null ? enteredMark : 0.0);
                                        insertStmt.executeUpdate();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            new Alert(Alert.AlertType.ERROR, "Failed to save mark: " + e.getMessage()).showAndWait();
                            gradebookTable.refresh(); // Refresh to show original value
                        }
                    }
                });
                markCol.setEditable(true);
                gradebookTable.getColumns().add(markCol);
            }
        }
        gradebookTable.setEditable(true);
        gradebookTable.setItems(gradebookRows);
    }

    private void showResultsForAssessment(Assessment assessment) {
        resultRows.clear();
        if (assessment != null) {
            // Fetch latest results from DB
            java.util.Map<String, Double> studentMarks = new java.util.HashMap<>();
            try (java.sql.Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
                String sql = "SELECT student_id, marks_obtained FROM result WHERE assessment_id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, assessment.getAssessmentId());
                    java.sql.ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        studentMarks.put(rs.getString("student_id"), rs.getDouble("marks_obtained"));
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace(); // Removed logger
            }
            for (Student s : students) {
                boolean isApplicable = false;
                if (assessment.getClassLevels() != null) {
                    for (String applicableClassSection : assessment.getClassLevels()) {
                        String applicableGrade = applicableClassSection;
                        String applicableSection = null;

                        // Parse grade and section from the applicable class/section string
                        int gradeLength = 0;
                        while (gradeLength < applicableClassSection.length() && Character.isDigit(applicableClassSection.charAt(gradeLength))) {
                            gradeLength++;
                        }

                        if (gradeLength > 0 && gradeLength < applicableClassSection.length()) {
                             applicableGrade = applicableClassSection.substring(0, gradeLength);
                             applicableSection = applicableClassSection.substring(gradeLength);
                        } else if (applicableClassSection.endsWith(" (All Sections)")) {
                             applicableGrade = applicableClassSection.substring(0, applicableClassSection.indexOf(" (All Sections)"));
                             applicableSection = null; // Treat as all sections
                        }

                        boolean gradeMatch = s.getClassLevel().equals(applicableGrade);
                        boolean sectionMatch = (applicableSection == null) || (s.getSection() != null && s.getSection().equals(applicableSection));

                        if (gradeMatch && sectionMatch) {
                            isApplicable = true;
                            break; // Found a match, no need to check other applicable classes for this student
                        }
                    }
                }

                if (isApplicable) {
                    Double mark = studentMarks.getOrDefault(s.getStudentId(), 0.0);
                    resultRows.add(new ResultRow(s.getFullName(), mark));
                }
            }
        }
    }

    @FXML
    private void handleSaveProfile() {
        if (loggedInTeacher != null) {
            try (java.sql.Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
                String sql = "UPDATE teacher SET full_name = ?, email = ?, password_hash = ? WHERE user_id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, emailField.getText());
                    stmt.setString(3, passwordField.getText());
                    stmt.setString(4, loggedInTeacher.getUserId());
                    stmt.executeUpdate();
                    
                    // Update the logged in teacher object
            loggedInTeacher.setFullName(nameField.getText());
            loggedInTeacher.setEmail(emailField.getText());
            loggedInTeacher.setPasswordHash(passwordField.getText());
                    
            profileStatusLabel.setText("Profile saved successfully!");
                }
            } catch (Exception e) {
                // e.printStackTrace(); // Removed logger
                profileStatusLabel.setText("Error saving profile!");
            }
        }
    }

    @FXML
    private void handleAddAssessment() {
        Dialog<Assessment> dialog = new Dialog<>();
        dialog.setTitle("Add Assessment");
        dialog.setHeaderText("Enter assessment details:");
        Label nameLabel = new Label("Assessment Name:");
        TextField nameField = new TextField();
        Label typeLabel = new Label("Assessment Type:");
        ComboBox<AssessmentType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(AssessmentType.CONTINUOUS, AssessmentType.FINAL);
        typeCombo.getSelectionModel().selectFirst();
        Label maxMarkLabel = new Label("Maximum Mark:");
        TextField maxMarkField = new TextField();
        Label subjectLabel = new Label("Subject:");
        ComboBox<String> subjectCombo = new ComboBox<>();
        Label semesterLabel = new Label("Semester:");
        ComboBox<String> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll("1st Semester", "2nd Semester");
        semesterCombo.getSelectionModel().selectFirst();
        Label classLabel = new Label("Applicable Classes:");
        ListView<String> classList = new ListView<>();
        classList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Populate subjects from teacher's assignments
        try (java.sql.Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
            String sql = "SELECT DISTINCT subject FROM teacher_class_subject WHERE teacher_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, loggedInTeacher.getUserId());
                java.sql.ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    subjectCombo.getItems().add(rs.getString("subject"));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
        }

        // Populate classList with grade+section combinations
        if (teacherAssignedClassesAndSections != null) {
            for (Map.Entry<String, List<String>> entry : teacherAssignedClassesAndSections.entrySet()) {
                String grade = entry.getKey();
                for (String section : entry.getValue()) {
                    if (section != null) {
                        classList.getItems().add(grade + section);
                    } else {
                        classList.getItems().add(grade + " (All Sections)");
                    }
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0); grid.add(nameField, 1, 0);
        grid.add(typeLabel, 0, 1); grid.add(typeCombo, 1, 1);
        grid.add(maxMarkLabel, 0, 2); grid.add(maxMarkField, 1, 2);
        grid.add(subjectLabel, 0, 3); grid.add(subjectCombo, 1, 3);
        grid.add(semesterLabel, 0, 4); grid.add(semesterCombo, 1, 4);
        grid.add(classLabel, 0, 5); grid.add(classList, 1, 5);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String title = nameField.getText().trim();
                AssessmentType type = typeCombo.getValue();
                String subject = subjectCombo.getValue();
                String semester = semesterCombo.getValue();
                Double maxMark;
                try {
                    maxMark = Double.parseDouble(maxMarkField.getText().trim());
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Maximum mark must be a number.").showAndWait();
                    return null;
                }
                if (title.isEmpty() || subject == null || classList.getSelectionModel().getSelectedItems().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Assessment name, subject, and at least one class are required.").showAndWait();
                    return null;
                }
                String academicYear = "2024/2025";
                if (!assessmentService.isAssessmentNameUnique(title, loggedInTeacher.getUserId(), academicYear, semester)) {
                    new Alert(Alert.AlertType.ERROR, "Assessment name must be unique for this course and semester.").showAndWait();
            return null;
                }

                // Validate total marks for each selected class/section
                for (String classSection : classList.getSelectionModel().getSelectedItems()) {
                    Double currentTotal = assessmentService.getTotalMarksForClass(classSection);
                    if (currentTotal + maxMark > 100) {
                        new Alert(Alert.AlertType.ERROR, 
                            String.format("Total marks for %s would exceed 100 (current total: %.1f, new assessment: %.1f)", 
                                classSection, currentTotal, maxMark)).showAndWait();
                        return null;
                    }
                }

                String assessmentId = "A" + System.currentTimeMillis();

                return new Assessment(
                    assessmentId,
                    title,
                    LocalDate.now(),
                    maxMark,
                    loggedInTeacher.getUserId(),
                    type,
                    subject,
                    academicYear,
                    semester,
                    new java.util.ArrayList<>(classList.getSelectionModel().getSelectedItems())
                );
                }
                return null;
            });
        Optional<Assessment> result = dialog.showAndWait();
        result.ifPresent(a -> { assessmentService.addAssessment(a); setupGradebookTable(); });
    }

    @FXML
    private void handleEditAssessment() {
        Assessment selected = assessmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select an assessment to edit.").showAndWait();
            return;
        }
        Dialog<Assessment> dialog = new Dialog<>();
        dialog.setTitle("Edit Assessment");
        dialog.setHeaderText("Edit assessment details:");
        Label nameLabel = new Label("Assessment Name:");
        TextField nameField = new TextField(selected.getTitle());
        Label typeLabel = new Label("Assessment Type:");
        ComboBox<AssessmentType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(AssessmentType.CONTINUOUS, AssessmentType.FINAL);
        typeCombo.setValue(selected.getAssessmentType());
        Label maxMarkLabel = new Label("Maximum Mark:");
        TextField maxMarkField = new TextField(String.valueOf(selected.getTotalMarks()));
        Label semesterLabel = new Label("Semester:");
        ComboBox<String> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll("1st Semester", "2nd Semester");
        semesterCombo.setValue(selected.getSemester());
        Label classLabel = new Label("Applicable Classes:");
        ListView<String> classList = new ListView<>();
        classList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Populate classList with grade+section combinations
        if (teacherAssignedClassesAndSections != null) {
            for (Map.Entry<String, List<String>> entry : teacherAssignedClassesAndSections.entrySet()) {
                String grade = entry.getKey();
                for (String section : entry.getValue()) {
                    if (section != null) {
                        classList.getItems().add(grade + section);
                    } else {
                        classList.getItems().add(grade + " (All Sections)");
                    }
                }
            }
        }

        // Select previously assigned classes
        if (selected.getClassLevels() != null) {
            for (String assignedClass : selected.getClassLevels()) {
                classList.getSelectionModel().select(assignedClass);
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0); grid.add(nameField, 1, 0);
        grid.add(typeLabel, 0, 1); grid.add(typeCombo, 1, 1);
        grid.add(maxMarkLabel, 0, 2); grid.add(maxMarkField, 1, 2);
        grid.add(semesterLabel, 0, 3); grid.add(semesterCombo, 1, 3);
        grid.add(classLabel, 0, 4); grid.add(classList, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String title = nameField.getText().trim();
                AssessmentType type = typeCombo.getValue();
                String semester = semesterCombo.getValue();
                Double maxMark;
                try {
                    maxMark = Double.parseDouble(maxMarkField.getText().trim());
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Maximum mark must be a number.").showAndWait();
                    return null;
                }
                if (title.isEmpty() || classList.getSelectionModel().getSelectedItems().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Assessment name and at least one class are required.").showAndWait();
                    return null;
                }

                // Validate total marks for each selected class/section
                for (String classSection : classList.getSelectionModel().getSelectedItems()) {
                    Double currentTotal = assessmentService.getTotalMarksForClass(classSection);
                    if (selected.getClassLevels().contains(classSection)) {
                        currentTotal -= selected.getTotalMarks();
                    }
                    if (currentTotal + maxMark > 100) {
                        new Alert(Alert.AlertType.ERROR, 
                            String.format("Total marks for %s would exceed 100 (current total: %.1f, new assessment: %.1f)", 
                                classSection, currentTotal, maxMark)).showAndWait();
                        return null;
                    }
                }

                return new Assessment(
                    selected.getAssessmentId(),
                    title,
                    selected.getDate(),
                    maxMark,
                    selected.getTeacherId(),
                    type,
                    selected.getSubject(),
                    selected.getAcademicYear(),
                    semester,
                    new java.util.ArrayList<>(classList.getSelectionModel().getSelectedItems())
                );
            }
            return null;
        });
        Optional<Assessment> result = dialog.showAndWait();
        result.ifPresent(a -> { assessmentService.updateAssessment(a); setupGradebookTable(); });
    }

    @FXML
    private void handleDeleteAssessment() {
        Assessment selected = assessmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an assessment to delete.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Assessment");
        confirm.setContentText("Are you sure you want to delete the assessment '" + selected.getTitle() + "'?\n" +
                             "This will also delete all associated results and cannot be undone.");

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    assessmentService.deleteAssessment(selected.getAssessmentId());
                    setupGradebookTable();
                    new Alert(Alert.AlertType.INFORMATION, "Assessment deleted successfully.").showAndWait();
                } catch (Exception e) {
                    // e.printStackTrace(); // Removed logger
                    new Alert(Alert.AlertType.ERROR, "Failed to delete assessment: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void handleExportResultsForClass() {
                TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Export Results for Class");
        dialog.setHeaderText("Enter class level to export:");
                Optional<String> result = dialog.showAndWait();
        result.ifPresent(classLevel -> {
            java.util.List<Student> classStudents = students.filtered(s -> s.getClassLevel().equals(classLevel));
            java.util.List<Assessment> classAssessments = assessmentService.getAssessmentsForClassLevel(classLevel);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Results for Class");
            fileChooser.setInitialFileName(classLevel + "_results.csv");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("Student Name");
                    for (Assessment a : classAssessments) {
                        writer.write("," + a.getTitle());
                    }
                    writer.write("\n");
                    for (Student s : classStudents) {
                        writer.write(s.getFullName());
                        for (Assessment a : classAssessments) {
                            Double mark = assessmentService.getResults().stream()
                                .filter(r -> r.getAssessmentId().equals(a.getAssessmentId()) && r.getStudentId().equals(s.getStudentId()))
                                .mapToDouble(Result::getMarksObtained).findFirst().orElse(0.0);
                            writer.write("," + mark);
                        }
                        writer.write("\n");
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Results exported successfully.").showAndWait();
                } catch (Exception e) {
                    // e.printStackTrace(); // Removed logger
                    new Alert(Alert.AlertType.ERROR, "Failed to export results.").showAndWait();
                }
            }
        });
    }

    @FXML
    private void handleExportResultsForQuiz() {
        Assessment selected = assessmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select an assessment to export results.").showAndWait();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Results for Quiz");
        fileChooser.setInitialFileName(selected.getTitle() + "_results.csv");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Student Name,Marks Obtained\n");
                for (ResultRow row : resultRows) {
                    writer.write(row.getStudentName() + "," + row.getMarksObtained() + "\n");
                }
                new Alert(Alert.AlertType.INFORMATION, "Results exported successfully.").showAndWait();
            } catch (Exception e) {
                // e.printStackTrace(); // Removed logger
            }
        }
    }

    @FXML
    private void handleLogout() {
        Rectangle2D bound = Screen.getPrimary().getBounds();
        double width = bound.getWidth()  - 5;
        double height = bound.getHeight() - 75;
        try {
            TeacherDashboardController.loggedInTeacher = null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/view/school-theme.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Grade Manager - Login");
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
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
    private void showStudentTab() {
        if (tabPane != null && studentTab != null) {
            tabPane.getSelectionModel().select(studentTab);
        }
    }

    @FXML
    private void showAssessmentTab() {
        if (tabPane != null && assessmentTab != null) {
            tabPane.getSelectionModel().select(assessmentTab);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleSaveAllGrades() {
        try (java.sql.Connection conn = com.school.grademanager.service.DatabaseService.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try {
                for (StudentGradebookRow row : gradebookRows) {
                    Student s = row.student;
                    for (Assessment a : assessmentService.getAssessments()) {
                        if (a.getClassLevels() != null && a.getClassLevels().contains(s.getClassLevel())) {
                            Double mark = row.getMarkProperty(a.getAssessmentId()).get();
                            // Upsert result
                            String checkSql = "SELECT result_id FROM result WHERE assessment_id=? AND student_id=?";
                            try (java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                                checkStmt.setString(1, a.getAssessmentId());
                                checkStmt.setString(2, s.getStudentId());
                                java.sql.ResultSet rs = checkStmt.executeQuery();
                                if (rs.next()) {
                                    // Update
                                    String updateSql = "UPDATE result SET marks_obtained=? WHERE result_id=?";
                                    try (java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                        updateStmt.setDouble(1, mark != null ? mark : 0.0);
                                        updateStmt.setString(2, rs.getString("result_id"));
                                        updateStmt.executeUpdate();
                                    }
                                } else {
                                    // Insert
                                    String insertSql = "INSERT INTO result (result_id, assessment_id, student_id, marks_obtained) VALUES (?, ?, ?, ?)";
                                    try (java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                        insertStmt.setString(1, "r" + a.getAssessmentId() + s.getStudentId());
                                        insertStmt.setString(2, a.getAssessmentId());
                                        insertStmt.setString(3, s.getStudentId());
                                        insertStmt.setDouble(4, mark != null ? mark : 0.0);
                                        insertStmt.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
                conn.commit(); // Commit transaction
                new Alert(Alert.AlertType.INFORMATION, "All grades saved successfully.").showAndWait();
            } catch (Exception e) {
                conn.rollback(); // Rollback on error
                throw e;
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Removed logger
            new Alert(Alert.AlertType.ERROR, "Failed to save grades: " + e.getMessage()).showAndWait();
        }
    }

    public static class ResultRow {
        private String studentName;
        private double marksObtained;
        public ResultRow(String studentName, double marksObtained) {
            this.studentName = studentName;
            this.marksObtained = marksObtained;
        }
        public String getStudentName() { return studentName; }
        public double getMarksObtained() { return marksObtained; }
    }

    public static class StudentGradebookRow {
        private final Student student;
        private final AssessmentService assessmentService; // Store AssessmentService instance
        private final javafx.beans.property.SimpleStringProperty studentId;
        private final javafx.beans.property.SimpleStringProperty fullName;
        private final javafx.beans.property.SimpleStringProperty classLevel;
        private final java.util.Map<String, javafx.beans.property.SimpleObjectProperty<Double>> marks = new java.util.HashMap<>();

        public StudentGradebookRow(Student student, AssessmentService assessmentService) {
            this.student = student;
            this.assessmentService = assessmentService; // Assign instance
            this.studentId = new javafx.beans.property.SimpleStringProperty(student.getStudentId());
            this.fullName = new javafx.beans.property.SimpleStringProperty(student.getFullName());
            this.classLevel = new javafx.beans.property.SimpleStringProperty(student.getClassLevel());

            // Fetch results for this student from the AssessmentService
            List<Result> studentResults = assessmentService.getResultsForStudent(student.getStudentId());
            java.util.Map<String, Double> studentMarksMap = new java.util.HashMap<>();
            for (Result result : studentResults) {
                studentMarksMap.put(result.getAssessmentId(), result.getMarksObtained());
            }

            // Populate marks for all relevant assessments
            for (Assessment a : assessmentService.getAssessments()) {
                 // Only consider assessments applicable to this student's class/section
                boolean isApplicable = false;
                 if (a.getClassLevels() != null) {
                     String studentClassSection = student.getClassLevel() + (student.getSection() != null ? student.getSection() : " (All Sections)");
                      if (a.getClassLevels().contains(studentClassSection) || a.getClassLevels().contains(student.getClassLevel())) { // Check both combined and just grade
                         isApplicable = true;
                      }
                 }

                if (isApplicable) {
                    // Use the mark from the fetched results, or null if not found
                    Double mark = studentMarksMap.get(a.getAssessmentId());
                    marks.put(a.getAssessmentId(), new javafx.beans.property.SimpleObjectProperty<>(mark));
                }
            }
        }

        public javafx.beans.property.StringProperty studentIdProperty() { return studentId; }
        public javafx.beans.property.StringProperty fullNameProperty() { return fullName; }
        public javafx.beans.property.StringProperty classLevelProperty() { return classLevel; }

        public javafx.beans.property.ObjectProperty<Double> getMarkProperty(String assessmentId) {
            // Return existing property or a new one initialized to null if the assessment is relevant but no mark property exists yet
             if (!marks.containsKey(assessmentId)) {
                 // Check if the assessment is applicable to this student's class/section before adding a property
                 for (Assessment a : this.assessmentService.getAssessments()) { // Use the stored assessmentService instance
                     if (a.getAssessmentId().equals(assessmentId)) {
                         if (a.getClassLevels() != null) {
                             String studentClassSection = student.getClassLevel() + (student.getSection() != null ? student.getSection() : " (All Sections)");
                             if (a.getClassLevels().contains(studentClassSection) || a.getClassLevels().contains(student.getClassLevel())) {
                                 marks.put(assessmentId, new javafx.beans.property.SimpleObjectProperty<>(null));
                                 break;
                             }
                         }
                     }
                 }
             }
            return marks.get(assessmentId); // Returns null if assessmentId is not in marks map (not applicable or no assessment found)
        }

        public void setMark(String assessmentId, Double mark) {
            if (marks.containsKey(assessmentId)) {
                marks.get(assessmentId).set(mark);
            } else {
                 marks.put(assessmentId, new javafx.beans.property.SimpleObjectProperty<>(mark));
            }
        }

        public void saveAllMarks(AssessmentService assessmentService) {
            for (java.util.Map.Entry<String, javafx.beans.property.SimpleObjectProperty<Double>> entry : marks.entrySet()) {
                String assessmentId = entry.getKey();
                Double mark = entry.getValue().get();

                if (mark == null) {
                    Result existingResult = assessmentService.getResult(assessmentId, this.student.getStudentId());
                    if (existingResult != null) {
                        assessmentService.deleteResult(existingResult.getResultId());
                    }
                } else {
                    Result existingResult = assessmentService.getResult(assessmentId, this.student.getStudentId());
                    if (existingResult != null) {
                        existingResult.setMarksObtained(mark);
                        assessmentService.updateResult(existingResult);
                    } else {
                        Result newResult = new Result(
                            "r" + assessmentId + this.student.getStudentId(),
                            assessmentId,
                            this.student.getStudentId(),
                            mark
                        );
                        assessmentService.addResult(newResult);
                    }
                }
            }
        }
    }
}
