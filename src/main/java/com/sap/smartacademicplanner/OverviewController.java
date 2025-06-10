package com.sap.smartacademicplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class OverviewController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> subjectCol;
    @FXML private TableColumn<Task, String> taskTypeCol;
    @FXML private TableColumn<Task, LocalDate> dueDateCol;
    @FXML private TableColumn<Task, Integer> estHoursCol;
    @FXML private TableColumn<Task, String> statusCol;

    // ✅ Summary Labels - update these dynamically
    @FXML private Label totalTasksLabel;
    @FXML private Label completedLabel;
    @FXML private Label pendingLabel;
    @FXML private Label dueSoonLabel;

    @FXML private Label statusLabel;

    public void initialize() {
        setupTableColumns();
        loadTasks();
        loadTaskStats();
    }

    private void setupTableColumns() {
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        taskTypeCol.setCellValueFactory(new PropertyValueFactory<>("taskType"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        estHoursCol.setCellValueFactory(new PropertyValueFactory<>("estHours"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            int userID = UserSession.getCurrentUserID();

            String sql = """
            SELECT t.*, ts.IsCompleted 
            FROM Tasks t
            LEFT JOIN TaskStatus ts ON t.TaskID = ts.TaskID
            WHERE t.UserID = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isCompleted = rs.getObject("IsCompleted", Boolean.class) != null && rs.getBoolean("IsCompleted");
                String status = isCompleted ? "Completed" : "Pending";

                tasks.add(new Task(
                        rs.getInt("TaskID"),
                        rs.getString("SubjectName"),
                        rs.getString("TaskType"),
                        LocalDate.parse(rs.getString("DueDate")), // ✅ Safe parsing
                        rs.getInt("EstHours"),
                        rs.getString("Priority"),
                        status
                ));
            }

            taskTable.setItems(tasks);

        } catch (Exception e) {
            System.out.println("❌ Failed to load tasks");
            statusLabel.setText("Failed to load tasks.");
            e.printStackTrace();
        }
    }

    private void loadTaskStats() {
        try (Connection conn = DBConnection.getConnection()) {
            int userID = UserSession.getCurrentUserID();

            // Total Tasks
            String totalSql = "SELECT COUNT(*) FROM Tasks WHERE UserID = ?";
            totalTasksLabel.setText("Total Tasks: " + getCount(conn, totalSql, userID));

            // Completed Tasks
            String completedSql = """
            SELECT COUNT(*) FROM Tasks t
            JOIN TaskStatus ts ON t.TaskID = ts.TaskID
            WHERE ts.IsCompleted = 1 AND t.UserID = ?
            """;
            completedLabel.setText("Completed: " + getCount(conn, completedSql, userID));

            // Pending Tasks
            String pendingSql = """
            SELECT COUNT(*) FROM Tasks t
            LEFT JOIN TaskStatus ts ON t.TaskID = ts.TaskID
            WHERE ts.IsCompleted IS NULL OR ts.IsCompleted = 0 AND t.UserID = ?
            """;
            pendingLabel.setText("Pending: " + getCount(conn, pendingSql, userID));

            // Due Soon (Next 7 days)
            String dueSoonSql = """
            SELECT COUNT(*) FROM Tasks t
            WHERE DueDate >= DATE('now') 
              AND DueDate <= DATE('now', '+7 days')
              AND t.UserID = ?
            """;
            dueSoonLabel.setText("Due Soon: " + getCount(conn, dueSoonSql, userID));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCount(Connection conn, String query, int userID) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        stmt.setInt(1, userID);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getString(1) : "0";
    }

    @FXML
    private void addNewTask() {
        AppNavigator.navigateToAddTask();
    }

    @FXML
    private void viewGoals() {
        AppNavigator.navigateToMyGoals();
    }

    @FXML
    private void refreshData() {
        loadTasks();
        loadTaskStats();
        statusLabel.setText("✅ Data refreshed!");
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }
}