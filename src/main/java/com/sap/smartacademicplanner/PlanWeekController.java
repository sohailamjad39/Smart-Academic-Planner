package com.sap.smartacademicplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PlanWeekController {

    @FXML private TextField hoursField;
    @FXML private TextField assignCountField;
    @FXML private TextField quizCountField;
    @FXML private TextArea planOutputArea;
    @FXML private Label statusLabel;

    private String currentUserEmail;

    public void setUserData(String name, String email) {
        this.currentUserEmail = email;
    }

    @FXML
    private void generatePlan() {
        String hoursStr = hoursField.getText();
        String assignStr = assignCountField.getText();
        String quizStr = quizCountField.getText();

        if (hoursStr.isEmpty() || assignStr.isEmpty() || quizStr.isEmpty()) {
            statusLabel.setText("All fields are required!");
            return;
        }

        try {
            double freeHoursPerDay = Double.parseDouble(hoursStr);
            int assignmentCount = Integer.parseInt(assignStr);
            int quizCount = Integer.parseInt(quizStr);

            // 🔁 Clear previous output
            planOutputArea.clear();

            // 🧮 Basic planning logic (you can improve this later!)
            double totalStudyTime = freeHoursPerDay * 7; // Total hours in week
            double timePerAssignment = totalStudyTime / (assignmentCount + quizCount + 1); // Add buffer for exams

            planOutputArea.appendText("📚 Weekly Study Plan\n");
            planOutputArea.appendText("-------------------------\n");
            planOutputArea.appendText("Total Free Time: " + totalStudyTime + " hours/week\n");

            for (int i = 1; i <= assignmentCount; i++) {
                planOutputArea.appendText("\nAssignment " + i + ": " + String.format("%.1f", timePerAssignment) + " hours\n");
                planOutputArea.appendText("Suggested Days: Monday - Wednesday");
            }

            for (int i = 1; i <= quizCount; i++) {
                planOutputArea.appendText("\n\nQuiz " + i + ": " + String.format("%.1f", timePerAssignment) + " hours\n");
                planOutputArea.appendText("Suggested Days: Thursday - Friday");
            }

            planOutputArea.appendText("\n\n⚠️ Keep some time for revision and breaks!");

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Please enter valid numbers only.");
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
            BorderPane dashboardRoot = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserName("User", currentUserEmail); // Replace with real user data if available

            Scene scene = new Scene(dashboardRoot, 800, 600);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}