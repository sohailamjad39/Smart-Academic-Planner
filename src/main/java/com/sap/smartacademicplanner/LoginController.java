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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Email and Password are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Name FROM Users WHERE Email = ? AND Password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String userName = rs.getString("Name");
                String userEmail = email;

                // Save session
                UserSession.login(userName, userEmail);

                // Navigate to dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
                BorderPane dashboardRoot = loader.load();

                DashboardController controller = loader.getController();
                if (controller != null) {
                    controller.setUserName(userName, userEmail);
                }

                Scene scene = new Scene(dashboardRoot, 800, 600);
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                statusLabel.setText("❌ Invalid credentials!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    private void switchToSignUp() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/signup-view.fxml"));
            BorderPane signUpRoot = fxmlLoader.load();

            Scene scene = new Scene(signUpRoot, 500, 600);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}