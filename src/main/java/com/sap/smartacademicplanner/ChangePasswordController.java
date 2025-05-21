package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private String currentUserEmail;

    public void setUserData(String name, String email) {
        this.currentUserEmail = email;
        System.out.println("Set current user: " + name + " <" + email + ">");
    }

    @FXML
    private void handleChangePassword() {
        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        System.out.println("Current Email: " + currentUserEmail);
        System.out.println("Current Password (input): " + currentPass);
        System.out.println("New Password: " + newPass);
        System.out.println("Confirm Password: " + confirmPass);

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            statusLabel.setText("All fields are required!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            statusLabel.setText("❌ New passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // 🔍 Check current password from DB
            String checkSql = "SELECT Password FROM Users WHERE Email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, currentUserEmail);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("Password");
                System.out.println("✅ Current password in DB: " + dbPassword);

                if (!dbPassword.equals(currentPass)) {
                    statusLabel.setText("❌ Current password is incorrect");
                    return;
                }
            } else {
                statusLabel.setText("❌ User not found!");
                return;
            }

            // ✅ Update password
            String updateSql = "UPDATE Users SET Password = ? WHERE Email = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, newPass);
            updateStmt.setString(2, currentUserEmail);

            int rows = updateStmt.executeUpdate();
            if (rows > 0) {
                statusLabel.setText("✅ Password changed successfully!");
            } else {
                statusLabel.setText("❌ Failed to change password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
            BorderPane dashboardRoot = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserName("User", currentUserEmail); // You can fetch real name from DB later

            Scene scene = new Scene(dashboardRoot, 800, 600);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}