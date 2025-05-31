package com.sap.smartacademicplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.Stack;

public class MyGoalsController {

    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> subjectCol;
    @FXML
    private TableColumn<Task, String> taskTypeCol;
    @FXML
    private TableColumn<Task, LocalDate> dueDateCol;
    @FXML
    private TableColumn<Task, Integer> estHoursCol;
    @FXML
    private TableColumn<Task, String> priorityCol;
    @FXML
    private TableColumn<Task, String> statusCol;

    @FXML
    private Label subjectDetail;
    @FXML
    private Label taskTypeDetail;
    @FXML
    private Label dueDateDetail;
    @FXML
    private Label estHoursDetail;
    @FXML
    private ComboBox<String> priorityCombo;


    private int selectedTaskId = -1;
    private final Stack<String> navigationStack = new Stack<>();

    public void initialize() {
        setupTableColumns();
        loadTasks();
        priorityCombo.getItems().addAll("High", "Medium", "Low");

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
            // Get current user's UserID
            String sqlGetUser = "SELECT UserID FROM Users WHERE Email = ?";
            PreparedStatement stmtUser = conn.prepareStatement(sqlGetUser);
            stmtUser.setString(1, UserSession.getCurrentUserEmail());

            ResultSet rsUser = stmtUser.executeQuery();
            if (!rsUser.next()) {
                System.out.println("❌ User not found during task load");
                return;
            }

            int userID = rsUser.getInt("UserID");

            // Load tasks by SubjectName
            String sql = "SELECT * FROM Tasks WHERE UserID = ? ORDER BY DueDate ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = "Pending"; // You can join with TaskStatus if needed

                tasks.add(new Task(
                        rs.getInt("TaskID"),
                        rs.getString("SubjectName"), // ✅ Now loaded directly
                        rs.getString("TaskType"),
                        rs.getDate("DueDate").toLocalDate(),
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

    private String getSubjectNameFromID(int subjectID, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT Name FROM Subjects WHERE SubjectID = ?");
        ps.setInt(1, subjectID);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString("Name") : "Unknown";
    }

    @FXML
    private void markAsCompleted() {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE TaskStatus SET IsCompleted = 1 WHERE TaskID = ?");
            stmt.setInt(1, selectedTaskId);
            stmt.executeUpdate();
            loadTasks(); // Refresh table
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
            loadTasks(); // Refresh table
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setHighPriority() {
//        updatePriority("High");
    }

    @FXML
    private void setLowPriority() {
//        updatePriority("Low");
    }

    private void updatePriority(String newPriority) {
        if (selectedTaskId == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Tasks SET Priority = ? WHERE TaskID = ?");
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
        AppNavigator.navigateTo("dashboard-view.fxml", "Dashboard", 800, 600);
    }
}