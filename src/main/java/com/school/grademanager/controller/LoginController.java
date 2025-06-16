package com.school.grademanager.controller;

import com.school.grademanager.model.Student;
import com.school.grademanager.model.Teacher;
import com.school.grademanager.model.User;
import com.school.grademanager.service.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Rectangle2D bound = Screen.getPrimary().getBounds();
        double width = bound.getWidth()  - 5;
        double height = bound.getHeight() - 75;
        userService.authenticate(username, password).ifPresentOrElse(user -> {
            try {
                String fxml;
                if (username.equals("admin")) {
                    fxml = "/view/admin_dashboard.fxml";
                } else if (user.getRole() == User.Role.TEACHER) {
                    TeacherDashboardController.loggedInTeacher = (Teacher) user;
                    fxml = "/view/teacher_dashboard.fxml";
                } else {
                    StudentDashboardController.loggedInStudent = (Student) user;
                    fxml = "/view/student_dashboard.fxml";
                }
                Parent dashboard = FXMLLoader.load(getClass().getResource(fxml));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(dashboard, width, height));
                stage.setTitle("Grade Manager - Dashboard");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/school-icon.png")));
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load dashboard.");
                alert.showAndWait();
            }
        }, () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid username or password.");
            alert.showAndWait();
        });
    }
}
