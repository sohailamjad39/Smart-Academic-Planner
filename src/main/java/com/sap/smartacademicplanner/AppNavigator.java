package com.sap.smartacademicplanner;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class AppNavigator {
    private static Stage mainStage;
    private static BorderPane mainLayout;
    private static final Stack<String> backStack = new Stack<>();
    private static String currentPage = "/fxml/dashboard-view.fxml";

    public static void setStage(Stage stage) {
        mainStage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/fxml/signup-view.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 500, 600);
            scene.getStylesheets().add(AppNavigator.class.getResource("/styles.css").toExternalForm());

            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner");
            mainStage.setResizable(false);
            mainStage.centerOnScreen(); // ⬅️ This ensures centering
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateTo(String fxmlFile, String title, double width, double height) {
        currentPage = "/fxml/" + fxmlFile;

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(currentPage));
            BorderPane view = loader.load(); // Loads new instance of FXML
            Scene scene = new Scene(view, width, height);
            scene.getStylesheets().add(AppNavigator.class.getResource("/styles.css").toExternalForm());

            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | " + title);
            mainStage.setResizable(false);
            mainStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void goBack() {
        if (!backStack.isEmpty()) {
            currentPage = backStack.pop();
            try {
                FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(currentPage));
                BorderPane view = loader.load();
                mainLayout.setCenter(view);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void navigateToChangePassword(String userEmail, String userName) {
        currentPage = "/fxml/change-password-view.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(currentPage));
            BorderPane view = loader.load();

            Scene scene = new Scene(view, 500, 600);
            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | Change Password");
            mainStage.centerOnScreen();

            // Pass user data to controller
            ChangePasswordController controller = loader.getController();
            if (controller != null) {
                controller.setUserData(userName, userEmail);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToPlanWeek(String userEmail, String userName) {
        currentPage = "/fxml/plan-week-view.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(currentPage));
            BorderPane view = loader.load();

            Scene scene = new Scene(view, 500, 600);
            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | Weekly Planner");
            mainStage.centerOnScreen();

            PlanWeekController controller = loader.getController();
            if (controller != null) {
                controller.setUserData(userName, userEmail);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToAddTask(String userEmail, String userName) {
        currentPage = "/fxml/add-task-view.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(currentPage));
            BorderPane view = loader.load();

            Scene scene = new Scene(view, 500, 600);
            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | Add New Task");
            mainStage.centerOnScreen();

            AddTaskController controller = loader.getController();
            if (controller != null) {
//                controller.setUserData(userName, userEmail);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateDashboardName(String name, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/fxml/dashboard-view.fxml"));
            BorderPane dashboard = loader.load();
            DashboardController controller = loader.getController();
            controller.setUserName(name, email);
            mainLayout.setCenter(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}