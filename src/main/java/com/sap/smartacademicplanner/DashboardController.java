package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Stack;


public class DashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalTasksLabel, inProgressLabel, missedLabel, completedLabel, dueSoonLabel;

    // Navigation history stack
    private final Stack<String> navigationStack = new Stack<>();
    private String currentPage = "dashboard-view.fxml";
    private String currentUserEmail;


    public void setUserName(String name, String email) {
        System.out.println("Setting welcome message to: Welcome " + name);
        System.out.println("welcomeLabel is null? " + (welcomeLabel == null));

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome " + name);
        } else {
            System.out.println("❌ welcomeLabel is null! fx:id might be missing or mismatched.");
        }

        this.currentUserEmail = email;
        loadDummyData();
    }

    private void loadDummyData() {
        totalTasksLabel.setText("12");
        inProgressLabel.setText("3");
        missedLabel.setText("1");
        completedLabel.setText("8");
        dueSoonLabel.setText("2");
    }

    // Navigation methods
    @FXML
    private void goBack() {
        if (!navigationStack.isEmpty()) {
            currentPage = navigationStack.pop();
            loadPage(currentPage, "Dashboard");
        }
    }

    @FXML
    private void goForward() {
        // Future enhancement: forward stack
    }

    @FXML
    private void editProfile() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/edit-profile-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 600);
            Stage stage = new Stage();
            stage.setTitle("EduPlanner | Edit Profile");
            stage.setScene(scene);
            stage.show();

            // Pass user data to edit profile controller
            EditProfileController controller = fxmlLoader.getController();
            controller.setUserData(welcomeLabel.getText().replace("Welcome ", ""), currentUserEmail); // Make sure to store and pass email too

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void changePassword() {
        navigationStack.push(currentPage);
        AppNavigator.navigateToChangePassword(currentUserEmail, welcomeLabel.getText().replace("Welcome ", ""));
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 600);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startWeeklyPlanning() {
        navigationStack.push(currentPage);
        AppNavigator.navigateToPlanWeek(currentUserEmail, welcomeLabel.getText().replace("Welcome ", ""));
    }

    @FXML
    private void viewGoals() {
        navigationStack.push(currentPage);
        loadPage("goals-view.fxml", "My Goals");
    }

    @FXML
    private void viewAssignments() {
        navigationStack.push(currentPage);
        loadPage("assignments-view.fxml", "Assignments");
    }

    @FXML
    private void viewOverview() {
        navigationStack.push(currentPage);
        loadPage("overview-view.fxml", "Overview");
    }

    @FXML
    private void viewHistory() {
        navigationStack.push(currentPage);
        loadPage("history-view.fxml", "History");
    }

    @FXML
    private void openSettings() {
        navigationStack.push(currentPage);
        loadPage("settings-view.fxml", "Settings");
    }

    @FXML
    private void addNewTask() {
        navigationStack.push(currentPage);
        AppNavigator.navigateToAddTask(currentUserEmail, welcomeLabel.getText().replace("Welcome ", ""));
    }

    @FXML
    public void initialize() {
        System.out.println("Intialized DashboardController");
    }

    private void loadPage(String fxmlFile, String title) {
        AppNavigator.navigateTo(fxmlFile, title, 400, 600);
    }
}