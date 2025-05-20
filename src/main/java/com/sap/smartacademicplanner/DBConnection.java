package com.sap.smartacademicplanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SmartPlannerDB;encrypt=false";
    private static final String USER = "javauser";  // Or your SQL login
    private static final String PASSWORD = "1234";  // Replace with actual password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Test method
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connected to SmartPlannerDB!");
        } catch (SQLException e) {
            System.out.println("❌ Failed to connect:");
            e.printStackTrace();
        }
    }
}
