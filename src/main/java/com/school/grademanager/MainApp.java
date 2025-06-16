package com.school.grademanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            Rectangle2D bound = Screen.getPrimary().getBounds();
            double width = bound.getWidth()  - 5;
            double height = bound.getHeight() - 75;
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, width, height); // More moderate size
            scene.getStylesheets().add(getClass().getResource("/view/school-theme.css").toExternalForm());
            primaryStage.setTitle("Grade Manager - Login");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/school-icon.png")));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
