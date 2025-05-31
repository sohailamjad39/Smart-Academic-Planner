package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalTasksLabel, inProgressLabel, missedLabel, completedLabel, dueSoonLabel;

    // Called on page load
    @FXML
    public void initialize() {
        if (UserSession.isLoggedIn()) {
            String userName = UserSession.getCurrentUserName();
            welcomeLabel.setText("Welcome " + userName);
            loadTaskStats(); // Load real data instead of dummy values
        } else {
            welcomeLabel.setText("Welcome Guest");
        }
    }

    // Method to manually set username (used during login)
    public void setUserName(String name, String email) {
        System.out.println("Setting welcome message to: Welcome " + name);
        welcomeLabel.setText("Welcome " + name);
        UserSession.login(name, email); // Update global session
        loadTaskStats(); // Load real stats after login
    }

    private void loadTaskStats() {
        try (Connection conn = DBConnection.getConnection()) {
            int userID = getUserIDFromEmail(conn, UserSession.getCurrentUserEmail());

            if (userID == -1) {
                System.out.println("❌ User not found!");
                return;
            }

            // Total tasks
            totalTasksLabel.setText(getCount(conn, "SELECT COUNT(*) FROM Tasks WHERE UserID = ?", userID));

            // Completed tasks
            completedLabel.setText(getCount(conn, """
                SELECT COUNT(*) FROM Tasks t
                JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                WHERE ts.IsCompleted = 1 AND t.UserID = ?""", userID));

            // In progress tasks
            inProgressLabel.setText(getCount(conn, """
                SELECT COUNT(*) FROM Tasks t
                JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                WHERE ts.IsCompleted = 0 AND DueDate > GETDATE() AND t.UserID = ?""", userID));

            // Missed tasks
            missedLabel.setText(getCount(conn, """
                SELECT COUNT(*) FROM Tasks t
                JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                WHERE ts.IsCompleted = 0 AND DueDate <= GETDATE() AND t.UserID = ?""", userID));

            // Due soon tasks (next 7 days)
            dueSoonLabel.setText(getCount(conn, """
                SELECT COUNT(*) FROM Tasks t
                WHERE DueDate BETWEEN GETDATE() AND DATEADD(DAY, 7, GETDATE())
                AND t.UserID = ?""", userID));

        } catch (Exception e) {
            e.printStackTrace();
            loadDummyData(); // Fallback
        }
    }

    private int getUserIDFromEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT UserID FROM Users WHERE Email = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("UserID") : -1;
    }

    private String getCount(Connection conn, String query, int userID) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userID);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getString(1) : "0";
    }

    private void loadDummyData() {
        totalTasksLabel.setText("12");
        inProgressLabel.setText("3");
        missedLabel.setText("1");
        completedLabel.setText("8");
        dueSoonLabel.setText("2");
    }

    @FXML
    private void addNewTask() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToAddTask();
    }

    @FXML
    private void viewGoals() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToMyGoals();
    }

    @FXML
    private void startWeeklyPlanning() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToPlanWeek();
    }

    @FXML
    private void changePassword() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToChangePassword();
    }

    @FXML
    private void editProfile() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-profile-view.fxml"));
            BorderPane editRoot = loader.load();

            EditProfileController controller = loader.getController();
            if (controller != null) {
                controller.setUserData(UserSession.getCurrentUserName(), UserSession.getCurrentUserEmail());
            }

            Scene scene = new Scene(editRoot, 800, 600);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        UserSession.logout();
        AppNavigator.navigateToLogin();
    }

    @FXML
    private void goBack() {
        AppNavigator.goBack();
    }

    @FXML
    private void goForward() {
        System.out.println("Go forward button clicked");
    }

    @FXML
    private void viewAssignments() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToAssignments();
    }

    @FXML
    private void viewOverview() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToOverview();
    }

    @FXML
    private void viewHistory() {
        if (!UserSession.isLoggedIn()) {
            AppNavigator.navigateToLogin();
            return;
        }
        AppNavigator.navigateToHistory();
    }
}