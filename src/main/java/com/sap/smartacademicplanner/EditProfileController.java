package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private String currentEmail;

    public void setUserData(String name, String email) {
        nameField.setText(name);
        emailField.setText(email);
        currentEmail = email;
    }

    @FXML
    public void saveChanges() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            statusLabel.setText("❌ All fields are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // 🔍 Check if the new email already exists (excluding current user)
            String checkSql = "SELECT COUNT(*) FROM Users WHERE Email = ? AND Email != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, newEmail);
            checkStmt.setString(2, currentEmail);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                statusLabel.setText("❌ Email already taken. Choose another.");
                return;
            }

            // ✅ Update user info
            String sql = "UPDATE Users SET Name = ?, Email = ? WHERE Email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setString(3, currentEmail);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Profile updated successfully!");

                // Update global session
                UserSession.login(newName, newEmail);

                // Go back to dashboard with updated name/email
                Platform.runLater(() -> AppNavigator.navigateToDashboard());
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
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
                BorderPane dashboardRoot = loader.load();

                Scene scene = new Scene(dashboardRoot, 800, 600);
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.show();

                DashboardController controller = loader.getController();
                if (controller != null) {
                    controller.setUserName(UserSession.getCurrentUserName(), UserSession.getCurrentUserEmail());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}