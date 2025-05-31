package com.sap.smartacademicplanner;

public class UserSession {
    private static String currentUserEmail;
    private static String currentUserName;

    public static void login(String name, String email) {
        currentUserName = name;
        currentUserEmail = email;
    }

    public static boolean isLoggedIn() {
        return currentUserEmail != null && currentUserName != null;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static String getCurrentUserName() {
        return currentUserName;
    }

    public static void logout() {
        currentUserEmail = null;
        currentUserName = null;
    }
}