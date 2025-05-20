package com.sap.smartacademicplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AppNavigator.setStage(stage);
        AppNavigator.navigateTo("signup-view.fxml", "Sign Up", 400, 600);
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}