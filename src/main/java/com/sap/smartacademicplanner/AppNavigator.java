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
    private static String currentPage;

    public static void setStage(Stage stage) {
        mainStage = stage;
        currentPage = "/fxml/login-view.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/fxml/login-view.fxml"));
            mainLayout = loader.load();

            Scene scene = new Scene(mainLayout, 500, 600);
            scene.getStylesheets().add(AppNavigator.class.getResource("/styles.css").toExternalForm());

            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner");
            mainStage.setResizable(false);
            mainStage.centerOnScreen();
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateTo(String fxmlFile, String title, double width, double height) {
        // Only allow login/signup if not logged in
        if (!UserSession.isLoggedIn() && !fxmlFile.equals("login-view.fxml") && !fxmlFile.equals("signup-view.fxml")) {
            System.out.println("❌ User not logged in. Redirecting to login.");
            navigateToLogin(); // Safe redirect
            return;
        }

        String path = "/fxml/" + fxmlFile;

        // Prevent infinite recursion/navigation loop
        if (currentPage != null && currentPage.equals(path)) {
            System.out.println("⚠️ Already on " + fxmlFile + ". Navigation stopped to prevent loop.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(path));
            BorderPane view = loader.load();

            Scene scene = new Scene(view, width, height);
            scene.getStylesheets().add(AppNavigator.class.getResource("/styles.css").toExternalForm());

            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | " + title);
            mainStage.centerOnScreen();

            // Save current page for goBack()
            if (!path.equals("/fxml/login-view.fxml") && !path.equals("/fxml/signup-view.fxml")) {
                backStack.push(currentPage); // Save old page
            }

            currentPage = path;

        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + path);
            e.printStackTrace();
        }
    }

    public static void navigateToLogin() {
        String path = "/fxml/login-view.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(path));
            BorderPane loginView = loader.load();

            Scene scene = new Scene(loginView, 500, 600);
            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | Login");
            mainStage.centerOnScreen();
            currentPage = path;
        } catch (IOException e) {
            System.err.println("Failed to load login view: " + path);
            e.printStackTrace();
        }
    }

    public static void navigateToDashboard() {
        navigateTo("dashboard-view.fxml", "Dashboard", 800, 600);
    }

    public static void navigateToAddTask() {
        navigateTo("add-task-view.fxml", "Add New Task", 500, 600);
    }

    public static void navigateToChangePassword() {
        navigateTo("change-password-view.fxml", "Change Password", 500, 600);
    }

    public static void navigateToPlanWeek() {
        navigateTo("plan-week-view.fxml", "Weekly Planner", 800, 700);
    }

    public static void navigateToMyGoals() {
        navigateTo("goals-view.fxml", "My Goals", 800, 600);
    }

    public static void navigateToAssignments() {
        navigateTo("assignments-view.fxml", "Assignments", 800, 600);
    }

    public static void navigateToSignUp() {
        navigateTo("signup-view.fxml", "Sign Up", 500, 600);
    }

    public static void navigateToOverview() {
        navigateTo("overview-view.fxml", "Overview", 800, 600);
    }

    public static void navigateToHistory() {
        navigateTo("history-view.fxml", "History", 800, 600);
    }

    public static void goBack() {
        if (backStack.isEmpty()) {
            System.out.println("BackStack is empty. Cannot go back.");
            return;
        }

        String previousPage = backStack.pop();
        currentPage = previousPage;

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(previousPage));
            BorderPane view = loader.load();

            Scene scene = new Scene(view, getSceneWidth(previousPage), getSceneHeight(previousPage));
            scene.getStylesheets().add(AppNavigator.class.getResource("/styles.css").toExternalForm());

            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | " + getTitleFromFXML(previousPage));
            mainStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Failed to load previous view: " + previousPage);
            e.printStackTrace();
        }
    }

    private static double getSceneWidth(String page) {
        if (page.contains("dashboard") || page.contains("goals")) return 800;
        return 500;
    }

    private static double getSceneHeight(String page) {
        return 600;
    }

    private static String getTitleFromFXML(String page) {
        switch (page) {
            case "/fxml/dashboard-view.fxml": return "Dashboard";
            case "/fxml/add-task-view.fxml": return "Add New Task";
            case "/fxml/change-password-view.fxml": return "Change Password";
            case "/fxml/plan-week-view.fxml": return "Weekly Planner";
            case "/fxml/login-view.fxml": return "Login";
            case "/fxml/signup-view.fxml": return "Sign Up";
            default: return "Home";
        }
    }

    public static void updateDashboardName(String name, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/fxml/dashboard-view.fxml"));
            BorderPane dashboardRoot = loader.load();
            DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUserName(name, email);
            }

            Scene scene = new Scene(dashboardRoot, 800, 600);
            mainStage.setScene(scene);
            mainStage.setTitle("EduPlanner | Dashboard");
            mainStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}