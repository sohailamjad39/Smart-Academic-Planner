package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class DashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalTasksLabel, inProgressLabel, missedLabel, completedLabel, dueSoonLabel;

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
            if (userID == -1) return;

            String today = LocalDate.now().toString();
            String nextWeek = LocalDate.now().plusDays(7).toString();

            // DEBUG: Print critical values
            System.out.println("=== TASK STATS DEBUG ===");
            System.out.println("User ID: " + userID);
            System.out.println("Today: " + today);
            System.out.println("Next Week: " + nextWeek);

            // Total Tasks
            String total = getCount(conn, "SELECT COUNT(*) FROM Tasks WHERE UserID = ?", userID);
            totalTasksLabel.setText(total);
            System.out.println("Total Tasks: " + total);

            // Completed Tasks
            String completed = getCount(conn, """
                    SELECT COUNT(*) FROM Tasks t
                    JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                    WHERE ts.IsCompleted = 1 AND t.UserID = ?""", userID);
            completedLabel.setText(completed);
            System.out.println("Completed: " + completed);

            // Due Soon
            String dueSoon = getCount(conn, """
                            SELECT COUNT(*) FROM Tasks t
                            JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                            WHERE ts.IsCompleted = 0 
                              AND DATE(t.DueDate) BETWEEN ? AND ?
                              AND t.UserID = ?""",
                    today, nextWeek, userID);
            dueSoonLabel.setText(dueSoon);
            System.out.println("Due Soon: " + dueSoon);

            // In Progress Tasks = Uncompleted and due after today
            String inProgress = getCount(conn, """
                            SELECT COUNT(*) FROM Tasks t
                            JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                            WHERE ts.IsCompleted = 0 
                              AND DATE(t.DueDate) > DATE(?)
                              AND t.UserID = ?""",
                    today, userID);

            inProgressLabel.setText(inProgress);
            System.out.println("In Progress: " + inProgress);

            // Missed Tasks
            String missed = getCount(conn, """
                            SELECT COUNT(*) FROM Tasks t
                            JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                            WHERE ts.IsCompleted = 0 
                              AND DATE(t.DueDate) < ?
                              AND t.UserID = ?""",
                    today, userID);

            missedLabel.setText(missed);
            System.out.println("Missed: " + missed);

            // DEBUG: Run a diagnostic query
            runDiagnosticQuery(conn, userID, today, nextWeek);

        } catch (Exception e) {
            e.printStackTrace();
            loadDummyData();
        }
    }

    // Add this new method for diagnostic query
    private void runDiagnosticQuery(Connection conn, int userID, String today, String nextWeek) throws SQLException {
        String diagQuery = """
                SELECT 
                    t.TaskID,
                    t.SubjectName,
                    t.DueDate,
                    ts.IsCompleted,
                    CASE 
                        WHEN DATE(t.DueDate) < ? THEN 'Missed'
                        WHEN DATE(t.DueDate) BETWEEN ? AND ? THEN 'Due Soon'
                        WHEN DATE(t.DueDate) > ? THEN 'In Progress'
                        ELSE 'Other'
                    END AS Category
                FROM Tasks t
                JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                WHERE t.UserID = ?""";

        try (PreparedStatement stmt = conn.prepareStatement(diagQuery)) {
            stmt.setString(1, today);
            stmt.setString(2, today);
            stmt.setString(3, nextWeek);
            stmt.setString(4, nextWeek);
            stmt.setInt(5, userID);

            ResultSet rs = stmt.executeQuery();
            System.out.println("\n=== TASK CATEGORY DIAGNOSTICS ===");
            while (rs.next()) {
                System.out.printf("Task %d: %s | Due: %s | Completed: %s | Category: %s%n",
                        rs.getInt("TaskID"),
                        rs.getString("SubjectName"),
                        rs.getString("DueDate"),
                        rs.getBoolean("IsCompleted") ? "Yes" : "No",
                        rs.getString("Category"));
            }
        }
    }

    // Updated helper method to handle date parameters
    // Updated helper method to handle parameters in correct order
    private String getCount(Connection conn, String query, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Bind parameters in the order they appear in the query
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) params[i]);
                } else if (params[i] instanceof String) {
                    stmt.setString(i + 1, (String) params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : "0";
            }
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