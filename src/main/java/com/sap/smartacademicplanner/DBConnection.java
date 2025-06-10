package com.sap.smartacademicplanner;

import java.io.File;
import java.sql.*;

public class DBConnection {

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String dbPath = "SmartPlannerDB.db";
                File dbFile = new File(dbPath);

                // Create empty DB file if not exists
                if (!dbFile.exists()) {
                    dbFile.createNewFile();
                }

                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);

                // Enable foreign keys
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }

                // Create tables if they don't exist
                createTablesIfNotExists();

            }

            return connection;

        } catch (Exception e) {
            System.out.println("❌ Failed to connect to SQLite");
            e.printStackTrace();
            return null;
        }
    }

    private static void createTablesIfNotExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    UserID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Name TEXT NOT NULL,
                    Email TEXT NOT NULL UNIQUE,
                    Password TEXT NOT NULL,
                    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Subjects Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Subjects (
                    SubjectID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    Name TEXT NOT NULL,
                    FOREIGN KEY(UserID) REFERENCES Users(UserID) ON DELETE CASCADE
                )
            """);

            // Tasks Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Tasks (
                    TaskID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    SubjectName TEXT NOT NULL,
                    TaskType TEXT NOT NULL,
                    EstHours INTEGER NOT NULL,
                    DueDate DATE NOT NULL,
                    Priority TEXT DEFAULT 'Medium',
                    FOREIGN KEY(UserID) REFERENCES Users(UserID) ON DELETE CASCADE
                )
            """);

            // TaskStatus Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS TaskStatus (
                    TaskID INTEGER PRIMARY KEY,
                    IsCompleted INTEGER DEFAULT 0 CHECK (IsCompleted IN (0, 1)),
                    FOREIGN KEY(TaskID) REFERENCES Tasks(TaskID) ON DELETE CASCADE
                )
            """);

            // WeeklyPlans Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS WeeklyPlans (
                    PlanID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    FreeHours INTEGER NOT NULL,
                    WeekStartDate DATE NOT NULL,
                    FOREIGN KEY(UserID) REFERENCES Users(UserID) ON DELETE CASCADE
                )
            """);

            // ScheduledPlans Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ScheduledPlans (
                    ScheduleID INTEGER PRIMARY KEY AUTOINCREMENT,
                    TaskID INTEGER NOT NULL,
                    StartTime DATETIME NOT NULL,
                    EndTime DATETIME NOT NULL,
                    FOREIGN KEY(TaskID) REFERENCES Tasks(TaskID) ON DELETE CASCADE
                )
            """);

            System.out.println("✅ All tables created successfully or already exist.");
        }
    }
}