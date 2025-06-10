package com.sap.smartacademicplanner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserSession {
    private static String currentUserEmail;
    private static String currentUserName;
    private static int currentUserID = -1; // New field

    public static void login(String name, String email) {
        currentUserName = name;
        currentUserEmail = email;
        currentUserID = fetchUserID(email); // Set ID on login
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

    public static int getCurrentUserID() {
        return currentUserID;
    }

    public static void logout() {
        currentUserEmail = null;
        currentUserName = null;
        currentUserID = -1;
    }

    // Helper method to get UserID from Email
    private static int fetchUserID(String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT UserID FROM Users WHERE Email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UserID");
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to fetch user ID");
            e.printStackTrace();
        }
        return -1;
    }
}