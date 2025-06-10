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

public class AssignmentsController {

    @FXML private TableView<Task> assignmentTable;
    @FXML private TableColumn<Task, String> subjectCol;
    @FXML private TableColumn<Task, String> taskTypeCol;
    @FXML private TableColumn<Task, LocalDate> dueDateCol;
    @FXML private TableColumn<Task, Integer> estHoursCol;
    @FXML private TableColumn<Task, String> statusCol;

    @FXML private Label subjectDetail;
    @FXML private Label dueDateDetail;
    @FXML private Label estHoursDetail;

    private int selectedTaskId = -1;

    public void initialize() {
        setupTableColumns();
        loadAssignments();

        // Handle row selection
        assignmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                subjectDetail.setText(newSelection.getSubject());
                dueDateDetail.setText(newSelection.getDueDate().toString());
                estHoursDetail.setText(String.valueOf(newSelection.getEstHours()));
                selectedTaskId = newSelection.getTaskID();
            }
        });
    }

    private void setupTableColumns() {
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        taskTypeCol.setCellValueFactory(new PropertyValueFactory<>("taskType"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        estHoursCol.setCellValueFactory(new PropertyValueFactory<>("estHours"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadAssignments() {
        ObservableList<Task> assignments = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT t.*, ts.IsCompleted 
                FROM Tasks t
                LEFT JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                WHERE t.UserID = ? AND t.TaskType = 'Assignment'
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, UserSession.getCurrentUserID());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isCompleted = rs.getObject("IsCompleted", Boolean.class) != null && rs.getBoolean("IsCompleted");
                String status = isCompleted ? "Completed" : "Pending";

                assignments.add(new Task(
                        rs.getInt("TaskID"),
                        rs.getString("SubjectName"),
                        rs.getString("TaskType"),
                        rs.getDate("DueDate").toLocalDate(),
                        rs.getInt("EstHours"),
                        rs.getString("Priority"),
                        status
                ));
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to load assignments");
            e.printStackTrace();
        }

        assignmentTable.setItems(assignments);
    }

    @FXML
    private void markAsCompleted() {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            // Check if TaskStatus exists first
            String checkSql = "SELECT * FROM TaskStatus WHERE TaskID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, selectedTaskId);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Update existing TaskStatus
                String updateSql = "UPDATE TaskStatus SET IsCompleted = 1 WHERE TaskID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, selectedTaskId);
                updateStmt.executeUpdate();
            } else {
                // Insert new TaskStatus if missing
                String insertSql = "INSERT INTO TaskStatus (TaskID, IsCompleted) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, selectedTaskId);
                insertStmt.setBoolean(2, true);
                insertStmt.executeUpdate();
            }

            loadAssignments(); // Refresh table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteTask() {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.prepareStatement("DELETE FROM Tasks WHERE TaskID = " + selectedTaskId).executeUpdate();
            conn.prepareStatement("DELETE FROM TaskStatus WHERE TaskID = " + selectedTaskId).executeUpdate();
            loadAssignments(); // Refresh list

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }
}