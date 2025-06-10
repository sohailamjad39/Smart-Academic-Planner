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

public class MyGoalsController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> subjectCol;
    @FXML private TableColumn<Task, String> taskTypeCol;
    @FXML private TableColumn<Task, LocalDate> dueDateCol;
    @FXML private TableColumn<Task, Integer> estHoursCol;
    @FXML private TableColumn<Task, String> priorityCol;
    @FXML private TableColumn<Task, String> statusCol;

    @FXML private Label subjectDetail;
    @FXML private Label taskTypeDetail;
    @FXML private Label dueDateDetail;
    @FXML private Label estHoursDetail;
    @FXML private ComboBox<String> priorityCombo;

    private int selectedTaskId = -1;

    public void initialize() {
        setupTableColumns();
        loadTasks();

        // Populate priority combo box
        priorityCombo.getItems().addAll("High", "Medium", "Low");
        priorityCombo.getSelectionModel().selectFirst();

        // Handle row selection
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                subjectDetail.setText(newSelection.getSubject());
                taskTypeDetail.setText(newSelection.getTaskType());
                dueDateDetail.setText(newSelection.getDueDate().toString());
                estHoursDetail.setText(String.valueOf(newSelection.getEstHours()));
                priorityCombo.setValue(newSelection.getPriority());

                selectedTaskId = newSelection.getTaskID();
            }
        });
    }

    private void setupTableColumns() {
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        taskTypeCol.setCellValueFactory(new PropertyValueFactory<>("taskType"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        estHoursCol.setCellValueFactory(new PropertyValueFactory<>("estHours"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
            SELECT t.*, ts.IsCompleted 
            FROM Tasks t
            LEFT JOIN TaskStatus ts ON t.TaskID = ts.TaskID
            WHERE t.UserID = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, UserSession.getCurrentUserID());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isCompleted = rs.getObject("IsCompleted", Boolean.class) != null && rs.getBoolean("IsCompleted");
                String status = isCompleted ? "Completed" : "Pending";

                tasks.add(new Task(
                        rs.getInt("TaskID"),
                        rs.getString("SubjectName"),
                        rs.getString("TaskType"),
                        LocalDate.parse(rs.getString("DueDate")), // ✅ Safe LocalDate parsing
                        rs.getInt("EstHours"),
                        rs.getString("Priority"),
                        status
                ));
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to load tasks");
            e.printStackTrace();
        }

        taskTable.setItems(tasks);
    }

    @FXML
    private void markAsCompleted() {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            // Check if TaskStatus already has an entry
            String checkSql = "SELECT * FROM TaskStatus WHERE TaskID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, selectedTaskId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // TaskStatus exists → just update
                String updateSql = "UPDATE TaskStatus SET IsCompleted = 1 WHERE TaskID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, selectedTaskId);
                updateStmt.executeUpdate();
            } else {
                // No TaskStatus yet → insert first
                String insertSql = "INSERT INTO TaskStatus (TaskID, IsCompleted) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, selectedTaskId);
                insertStmt.setBoolean(2, true);
                insertStmt.executeUpdate();
            }

            loadTasks(); // Refresh list

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteTask() {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            // ❌ Broken: Trying to delete from Tasks first
            // ✅ Fixed: Delete from TaskStatus first
            conn.prepareStatement("DELETE FROM TaskStatus WHERE TaskID = " + selectedTaskId).executeUpdate();
            conn.prepareStatement("DELETE FROM Tasks WHERE TaskID = " + selectedTaskId).executeUpdate();

            loadTasks(); // Refresh list

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setHighPriority() {
        updatePriority("High");
    }

    @FXML
    private void setLowPriority() {
        updatePriority("Low");
    }

    private void updatePriority(String newPriority) {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Tasks SET Priority = ? WHERE TaskID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPriority);
            stmt.setInt(2, selectedTaskId);
            stmt.executeUpdate();
            loadTasks(); // Refresh list
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }
}