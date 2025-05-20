package com.sap.smartacademicplanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private String currentEmail;

    public void initialize() {
        // You can load user data here from DB or pass it via method
    }

    public void setUserData(String name, String email) {
        nameField.setText(name);
        emailField.setText(email);
        currentEmail = email;
    }

    @FXML
    private void saveChanges() {
        String newName = nameField.getText();
        String newEmail = emailField.getText();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            statusLabel.setText("All fields are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Users SET Name = ?, Email = ? WHERE Email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setString(3, currentEmail);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                statusLabel.setText("✅ Profile updated successfully!");

                // Go back to dashboard with updated name
                Platform.runLater(() -> {
                    AppNavigator.updateDashboardName(newName, newEmail);
                    goToDashboard();
                });
            } else {
                statusLabel.setText("❌ Failed to update profile.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.goBack(); // Or navigate directly
    }
}