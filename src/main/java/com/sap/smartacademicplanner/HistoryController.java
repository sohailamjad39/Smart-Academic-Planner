package com.sap.smartacademicplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class HistoryController {

    @FXML private TableView<Task> historyTable;
    @FXML private TableColumn<Task, String> subjectCol;
    @FXML private TableColumn<Task, String> taskTypeCol;
    @FXML private TableColumn<Task, LocalDate> dueDateCol;
    @FXML private TableColumn<Task, Integer> estHoursCol;

    public void initialize() {
        setupTableColumns();
        loadHistoryTasks();
    }

    private void setupTableColumns() {
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        taskTypeCol.setCellValueFactory(new PropertyValueFactory<>("taskType"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        estHoursCol.setCellValueFactory(new PropertyValueFactory<>("estHours"));
    }

    private void loadHistoryTasks() {
        ObservableList<Task> historyData = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT t.* 
                FROM Tasks t
                JOIN TaskStatus ts ON t.TaskID = ts.TaskID
                WHERE ts.IsCompleted = 1""";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = getSubjectName(rs.getInt("SubjectID"), conn);

                historyData.add(new Task(
                        rs.getInt("TaskID"),
                        subjectName,
                        rs.getString("TaskType"),
                        rs.getDate("DueDate").toLocalDate(),
                        rs.getInt("EstHours")
                ));
            }

            historyTable.setItems(historyData);

        } catch (Exception e) {
            System.out.println("❌ Failed to load history");
            e.printStackTrace();
        }
    }

    private String getSubjectName(int subjectID, Connection conn) {
        try {
            String sql = "SELECT Name FROM Subjects WHERE SubjectID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, subjectID);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("Name") : "Unknown";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }
}