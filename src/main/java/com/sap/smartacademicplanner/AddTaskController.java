package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class AddTaskController {

    @FXML
    private TextField subjectField;
    @FXML
    private ComboBox<String> taskTypeCombo;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextField estHoursField;
    @FXML
    private Label statusLabel;

    private String currentUserEmail = UserSession.getCurrentUserEmail();

    public void initialize() {
        setupTaskTypeOptions();
        setupHourValidation();
    }

    private void setupTaskTypeOptions() {
        taskTypeCombo.getItems().addAll("Assignment", "Quiz", "Exam", "Project", "Other");
        taskTypeCombo.getSelectionModel().selectFirst();
    }

    private void setupHourValidation() {
        estHoursField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d{0,2}")) {
                estHoursField.setText(oldVal);
            }
        });
    }

    @FXML
    private void saveTask() {
        String subject = subjectField.getText().trim();
        String taskType = taskTypeCombo.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        String estHoursStr = estHoursField.getText().trim();

        if (subject.isEmpty() || taskType == null || dueDate == null || estHoursStr.isEmpty()) {
            statusLabel.setText("❌ All fields are required!");
            return;
        }

        int estHours;
        try {
            estHours = Integer.parseInt(estHoursStr);
        } catch (NumberFormatException ex) {
            statusLabel.setText("❌ Estimated hours must be a number");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Get current user ID
            String userIdSql = "SELECT UserID FROM Users WHERE Email = ?";
            PreparedStatement userIdStmt = conn.prepareStatement(userIdSql);
            userIdStmt.setString(1, currentUserEmail);

            ResultSet rsUser = userIdStmt.executeQuery();
            if (!rsUser.next()) {
                statusLabel.setText("❌ User not found!");
                return;
            }

            int userID = rsUser.getInt("UserID");

            // Insert into Tasks
            String insertTaskSql = """
            INSERT INTO Tasks (SubjectName, TaskType, DueDate, EstHours, Priority, UserID)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

            PreparedStatement taskStmt = conn.prepareStatement(insertTaskSql, Statement.RETURN_GENERATED_KEYS);
            taskStmt.setString(1, subject);
            taskStmt.setString(2, taskType);
            taskStmt.setString(3, dueDate.toString()); // ✅ Stored as 'YYYY-MM-DD'
            taskStmt.setInt(4, estHours);
            taskStmt.setString(5, "Medium"); // Default priority
            taskStmt.setInt(6, userID);

            int rowsAffected = taskStmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = taskStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int taskID = generatedKeys.getInt(1);

                    // ✅ Insert into TaskStatus safely
                    String insertStatusSql = "INSERT INTO TaskStatus (TaskID, IsCompleted) VALUES (?, ?)";
                    PreparedStatement statusStmt = conn.prepareStatement(insertStatusSql);
                    statusStmt.setInt(1, taskID);
                    statusStmt.setBoolean(2, false); // Default: not completed
                    statusStmt.executeUpdate();

                    statusLabel.setText("✅ Task saved successfully!");
                    clearFields();
                }
            } else {
                statusLabel.setText("❌ Failed to save task.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    private int getOrCreateSubjectID(String subjectName, int userID, Connection conn) throws SQLException {
        String selectSql = "SELECT SubjectID FROM Subjects WHERE Name = ? AND UserID = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, subjectName);
        selectStmt.setInt(2, userID);

        ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("SubjectID"); // Use existing subject
        }

        // Create new subject
        String insertSubjectSql = "INSERT INTO Subjects (UserID, Name) OUTPUT INSERTED.SubjectID VALUES (?, ?)";
        PreparedStatement insertSubjectStmt = conn.prepareStatement(insertSubjectSql);
        insertSubjectStmt.setInt(1, userID);
        insertSubjectStmt.setString(2, subjectName);

        ResultSet rsNew = insertSubjectStmt.executeQuery();
        if (rsNew.next()) {
            return rsNew.getInt(1); // Return new SubjectID
        }

        return -1; // If no match found
    }

    private void clearFields() {
        subjectField.clear();
        taskTypeCombo.getSelectionModel().clearSelection();
        dueDatePicker.setValue(null);
        estHoursField.clear();
    }

    @FXML
    private void onHoursInput() {
        String text = estHoursField.getText();
        if (!text.matches("\\d*\\.?\\d{0,2}")) {
            estHoursField.setText(text.replaceAll("[^\\d.]", ""));
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
            BorderPane dashboardRoot = loader.load();

            Scene scene = new Scene(dashboardRoot, 800, 600);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}