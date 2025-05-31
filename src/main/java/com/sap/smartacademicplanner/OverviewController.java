package com.sap.smartacademicplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class OverviewController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> subjectCol;
    @FXML private TableColumn<Task, String> taskTypeCol;
    @FXML private TableColumn<Task, LocalDate> dueDateCol;
    @FXML private TableColumn<Task, Integer> estHoursCol;
    @FXML private TableColumn<Task, String> statusCol;

    @FXML private Label statusLabel;

    public void initialize() {
        setupTableColumns();
        loadTasks();
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
            String sql = "SELECT t.*, ts.IsCompleted FROM Tasks t JOIN TaskStatus ts ON t.TaskID = ts.TaskID";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = getSubjectName(rs.getInt("SubjectID"), conn);
                String status = rs.getBoolean("IsCompleted") ? "Completed" : "Pending";

                tasks.add(new Task(
                        rs.getInt("TaskID"),
                        subjectName,
                        rs.getString("TaskType"),
                        rs.getDate("DueDate").toLocalDate(),
                        rs.getInt("EstHours"),
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

    private String getSubjectName(int subjectID, Connection conn) {
        try {
            String sql = "SELECT Name FROM Subjects WHERE SubjectID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subjectID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
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
        loadTasks(); // Reload data
        statusLabel.setText("✅ Data refreshed!");
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }
}