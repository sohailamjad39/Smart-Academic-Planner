package com.sap.smartacademicplanner;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.*;

public class PlanWeekController {

    // Input Fields
    @FXML private TextField weekdayHoursField;
    @FXML private TextField weekendHoursField;
    @FXML private TextField assignCountField;
    @FXML private TextField quizCountField;
    @FXML private TextField projectCountField;
    @FXML private TextField examCountField;

    // Output Table
    @FXML private TableView<ScheduledTask> scheduleTable;
    @FXML private TableColumn<ScheduledTask, String> dayCol;
    @FXML private TableColumn<ScheduledTask, String> taskCol;
    @FXML private TableColumn<ScheduledTask, String> typeCol;
    @FXML private TableColumn<ScheduledTask, String> timeCol;
    @FXML private TableColumn<ScheduledTask, String> priorityCol;

    @FXML private Label statusLabel;

    public void initialize() {
        setupTableColumns();

        // Optional: Load default values or last plan
    }

    private void setupTableColumns() {
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));
        taskCol.setCellValueFactory(new PropertyValueFactory<>("task"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("suggestedTime"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
    }

    @FXML
    private void generatePlan() {
        try {
            // Get input values
            double weekdayFree = Double.parseDouble(weekdayHoursField.getText());
            double weekendFree = Double.parseDouble(weekendHoursField.getText());
            int assignments = Integer.parseInt(assignCountField.getText());
            int quizzes = Integer.parseInt(quizCountField.getText());
            int projects = Integer.parseInt(projectCountField.getText());
            int exams = Integer.parseInt(examCountField.getText());

            // Validate inputs
            if (weekdayFree < 0 || weekendFree < 0 || assignments < 0 || quizzes < 0 || projects < 0 || exams < 0) {
                statusLabel.setText("❌ All inputs must be positive numbers.");
                return;
            }

            // Create task list with priorities
            List<TaskType> tasks = new ArrayList<>();
            for (int i = 0; i < assignments; i++) {
                tasks.add(new TaskType("Assignment " + (i+1), "Assignment", 1));
            }
            for (int i = 0; i < quizzes; i++) {
                tasks.add(new TaskType("Quiz " + (i+1), "Quiz", 2));
            }
            for (int i = 0; i < projects; i++) {
                tasks.add(new TaskType("Project " + (i+1), "Project", 3));
            }
            for (int i = 0; i < exams; i++) {
                tasks.add(new TaskType("Exam " + (i+1), "Exam", 4));
            }

            if (tasks.isEmpty()) {
                statusLabel.setText("✅ No tasks to plan!");
                scheduleTable.getItems().clear();
                return;
            }

            // Sort by priority (highest first)
            tasks.sort((t1, t2) -> Integer.compare(t2.priorityLevel, t1.priorityLevel));

            // Create day structure with available hours
            List<DaySlot> weekSchedule = new ArrayList<>();
            weekSchedule.add(new DaySlot("Monday", weekdayFree));
            weekSchedule.add(new DaySlot("Tuesday", weekdayFree));
            weekSchedule.add(new DaySlot("Wednesday", weekdayFree));
            weekSchedule.add(new DaySlot("Thursday", weekdayFree));
            weekSchedule.add(new DaySlot("Friday", weekdayFree));
            weekSchedule.add(new DaySlot("Saturday", weekendFree));
            weekSchedule.add(new DaySlot("Sunday", weekendFree));

            // Schedule tasks
            List<ScheduledTask> scheduledTasks = new ArrayList<>();
            int dayIndex = 0;

            for (TaskType task : tasks) {
                double timeRequired = calculateTimeRequired(task);
                boolean scheduled = false;

                // Try to schedule in available slots across the week
                for (int attempt = 0; attempt < weekSchedule.size() * 2; attempt++) {
                    DaySlot currentDay = weekSchedule.get(dayIndex);

                    if (currentDay.remainingHours >= timeRequired) {
                        scheduledTasks.add(new ScheduledTask(
                                currentDay.dayName,
                                task.name,
                                task.type,
                                String.format("%.1f hours", timeRequired),
                                getPriorityName(task.priorityLevel)
                        ));
                        currentDay.remainingHours -= timeRequired;
                        scheduled = true;
                        break;
                    }

                    // Move to next day (round-robin)
                    dayIndex = (dayIndex + 1) % weekSchedule.size();
                }

                // Handle unscheduled tasks
                if (!scheduled) {
                    scheduledTasks.add(new ScheduledTask(
                            "⚠ Not Scheduled",
                            task.name,
                            task.type,
                            "Not enough time!",
                            getPriorityName(task.priorityLevel)
                    ));
                }

                // Move to next day for next task
                dayIndex = (dayIndex + 1) % weekSchedule.size();
            }

            scheduleTable.setItems(FXCollections.observableArrayList(scheduledTasks));
            statusLabel.setText("✅ Weekly plan generated!");

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Please enter valid numbers only.");
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateTimeRequired(TaskType task) {
        // Base time + priority adjustment
        return switch (task.type) {
            case "Assignment" -> 1.5;
            case "Quiz" -> 1.0;
            case "Project" -> 2.5;
            case "Exam" -> 3.0;
            default -> 1.0;
        } * (0.8 + (task.priorityLevel * 0.1));
    }

    private String getPriorityName(int priorityLevel) {
        return switch (priorityLevel) {
            case 4 -> "Highest";
            case 3 -> "High";
            case 2 -> "Medium";
            case 1 -> "Low";
            default -> "Lowest";
        };
    }

    // Helper class for day slots
    private static class DaySlot {
        String dayName;
        double remainingHours;

        DaySlot(String dayName, double hours) {
            this.dayName = dayName;
            this.remainingHours = hours;
        }
    }

    private double getTimeForTask(TaskType task, double availableHours) {
        double baseTime = availableHours / 3.0; // Base time per day
        switch (task.type) {
            case "Assignment": return baseTime * 0.8;
            case "Quiz": return baseTime * 1.2;
            case "Project": return baseTime * 1.5;
            case "Exam": return baseTime * 2.0;
            default: return baseTime * 0.5;
        }
    }

    private String getBestDay(TaskType task, Map<String, Double> remainingHours) {
        // High priority tasks go earlier in the week
        List<String> highPriorityDays = Arrays.asList("Monday", "Tuesday", "Wednesday");
        List<String> lowPriorityDays = Arrays.asList("Thursday", "Friday", "Saturday", "Sunday");

        boolean isHighPriority = task.isHighPriority();

        for (String day : isHighPriority ? highPriorityDays : lowPriorityDays) {
            if (remainingHours.get(day) > 0) {
                return day;
            }
        }

        // Fallback
        for (Map.Entry<String, Double> entry : remainingHours.entrySet()) {
            if (entry.getValue() > 0) return entry.getKey();
        }

        return "No Free Day";
    }

    @FXML
    private void saveSchedule() {
        statusLabel.setText("💾 Schedule saved locally.");
        // You can later add DB saving logic here
    }

    @FXML
    private void resetFields() {
        weekdayHoursField.clear();
        weekendHoursField.clear();
        assignCountField.clear();
        quizCountField.clear();
        projectCountField.clear();
        examCountField.clear();
        scheduleTable.getItems().clear();
        statusLabel.setText("");
    }

    @FXML
    private void goToDashboard() {
        AppNavigator.navigateToDashboard();
    }

    // Helper class for task types and priorities
    private static class TaskType {
        String name;
        String type;
        int priorityLevel;

        TaskType(String name, String type, double totalFreeHours) {
            this.name = name;
            this.type = type;
            this.priorityLevel = priorityLevel();
        }

        int priorityLevel() {
            return switch (type) {
                case "Exam" -> 4;
                case "Project" -> 3;
                case "Quiz" -> 2;
                case "Assignment" -> 1;
                default -> 0;
            };
        }

        String priorityName() {
            return switch (type) {
                case "Exam" -> "Higher";
                case "Project" -> "Medium";
                case "Quiz" -> "High";
                case "Assignment" -> "Low";
                default -> "Lowest";
            };
        }

        boolean isHighPriority() {
            return priorityLevel() >= 2;
        }
    }

    // Data model for scheduled tasks
    public static class ScheduledTask {
        private final StringProperty day;
        private final StringProperty task;
        private final StringProperty type;
        private final StringProperty suggestedTime;
        private final StringProperty priority;

        public ScheduledTask(String day, String task, String type, String time, String priority) {
            this.day = new SimpleStringProperty(this, "day", day);
            this.task = new SimpleStringProperty(this, "task", task);
            this.type = new SimpleStringProperty(this, "type", type);
            this.suggestedTime = new SimpleStringProperty(this, "suggestedTime", time);
            this.priority = new SimpleStringProperty(this, "priority", priority);
        }

        public String getDay() { return day.get(); }
        public String getTask() { return task.get(); }
        public String getType() { return type.get(); }
        public String getSuggestedTime() { return suggestedTime.get(); }
        public String getPriority() { return priority.get(); }

        public StringProperty dayProperty() { return day; }
        public StringProperty taskProperty() { return task; }
        public StringProperty typeProperty() { return type; }
        public StringProperty suggestedTimeProperty() { return suggestedTime; }
        public StringProperty priorityProperty() { return priority; }
    }
}