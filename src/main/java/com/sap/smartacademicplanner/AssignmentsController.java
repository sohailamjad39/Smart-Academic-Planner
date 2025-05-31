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

    private final ObservableList<Task> assignmentData = FXCollections.observableArrayList();

    public void initialize() {
        setupTableColumns();
        loadAssignments();

        // Handle row selection
        assignmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                subjectDetail.setText(newSelection.getSubject());
                dueDateDetail.setText(newSelection.getDueDate().toString());
                estHoursDetail.setText(String.valueOf(newSelection.getEstHours()));
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
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT t.*, ts.IsCompleted FROM Tasks t JOIN TaskStatus ts ON t.TaskID = ts.TaskID WHERE t.TaskType = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "Assignment");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = getSubjectName(rs.getInt("SubjectID"), conn);
                boolean isCompleted = rs.getBoolean("IsCompleted");
                String status = isCompleted ? "Completed" : "Pending";

                assignmentData.add(new Task(
                        rs.getInt("TaskID"),
                        subjectName,
                        rs.getString("TaskType"),
                        rs.getDate("DueDate").toLocalDate(),
                        rs.getInt("EstHours"),
                        status
                ));
            }

            assignmentTable.setItems(assignmentData);

        } catch (Exception e) {
            System.out.println("❌ Failed to load assignments");
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
    private void markAsCompleted() {
        Task selected = assignmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE TaskStatus SET IsCompleted = 1 WHERE TaskID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getTaskID());
            stmt.executeUpdate();
            loadAssignments(); // Refresh table
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteTask() {
        Task selected = assignmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.prepareStatement("DELETE FROM Tasks WHERE TaskID = " + selected.getTaskID()).executeUpdate();
            conn.prepareStatement("DELETE FROM TaskStatus WHERE TaskID = " + selected.getTaskID()).executeUpdate();
            loadAssignments(); // Refresh table
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }
}