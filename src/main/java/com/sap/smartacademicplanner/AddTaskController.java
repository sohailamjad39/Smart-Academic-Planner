package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AddTaskController {

    @FXML private TextField subjectField;
    @FXML private ComboBox<String> taskTypeCombo;
    @FXML private TextField taskTypeField;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField estHoursField;
    @FXML private Label statusLabel;

    private String currentUserEmail;

    public void initialize() {
//        loadSubjects();
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
                estHoursField.setText(oldVal); // Revert if invalid
            }
        });
    }

//    private void loadSubjects() {
//        try (Connection conn = DBConnection.getConnection()) {
//            String sql = "SELECT Name FROM Subjects WHERE UserID = ?";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            // You'll need user email → fetch from DB or session later
//
//            // For now, hardcode sample subjects
//            subjectCombo.getItems().addAll("Math", "CS", "History", "Physics");
//            subjectCombo.getSelectionModel().selectFirst();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            statusLabel.setText("❌ Failed to load subjects.");
//        }
//    }
//
//    public void setUserData(String name, String email) {
//        this.currentUserEmail = email;
//        loadUserSubjects(email); // Load real subjects for the user
//    }
//
//    private void loadUserSubjects(String email) {
//        try (Connection conn = DBConnection.getConnection()) {
//            String sql = "SELECT s.Name FROM Subjects s JOIN Users u ON s.UserID = u.UserID WHERE u.Email = ?";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setString(1, email);
//
//            ResultSet rs = stmt.executeQuery();
//            subjectCombo.getItems().clear();
//
//            while (rs.next()) {
//                subjectCombo.getItems().add(rs.getString("Name"));
//            }
//
//            if (!subjectCombo.getItems().isEmpty()) {
//                subjectCombo.getSelectionModel().selectFirst();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            statusLabel.setText("❌ Failed to load subjects for user.");
//        }
//    }

    @FXML
    private void saveTask() {
        String subject = subjectField.getText();
        if (subject == null || subject.trim().isEmpty()) {
            statusLabel.setText("❌ Subject is required!");
            return;
        }
        String taskType = taskTypeCombo.getValue();
        if (taskType == null || taskType.isEmpty()) {
            statusLabel.setText("❌ Task type must be selected!");
            return;
        }
        LocalDate dueDate = dueDatePicker.getValue();
        String estHoursStr = estHoursField.getText();

        if (subject == null || taskType.isEmpty() || dueDate == null || estHoursStr.isEmpty()) {
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
            // Get User ID from Email
            String userIdSql = "SELECT UserID FROM Users WHERE Email = ?";
            PreparedStatement userIdStmt = conn.prepareStatement(userIdSql);
            userIdStmt.setString(1, currentUserEmail);
            ResultSet rs = userIdStmt.executeQuery();

            if (!rs.next()) {
                statusLabel.setText("❌ User not found!");
                return;
            }

            int userID = rs.getInt("UserID");

            // Get SubjectID from Subject Name
            String subjectSql = "SELECT SubjectID FROM Subjects WHERE Name = ? AND UserID = ?";
            PreparedStatement subjectStmt = conn.prepareStatement(subjectSql);
            subjectStmt.setString(1, subject);
            subjectStmt.setInt(2, userID);

            ResultSet subjectRs = subjectStmt.executeQuery();
            if (!subjectRs.next()) {
                statusLabel.setText("❌ Invalid subject selected");
                return;
            }

            int subjectID = subjectRs.getInt("SubjectID");

            // Insert Task
            String insertSql = "INSERT INTO Tasks (SubjectID, TaskType, DueDate, EstHours) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, subjectID);
            insertStmt.setString(2, taskType);
            insertStmt.setDate(3, java.sql.Date.valueOf(dueDate));
            insertStmt.setInt(4, estHours);

            int rows = insertStmt.executeUpdate();
            if (rows > 0) {
                statusLabel.setText("✅ Task saved successfully!");
                clearFields();
            } else {
                statusLabel.setText("❌ Failed to save task.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        taskTypeField.clear();
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