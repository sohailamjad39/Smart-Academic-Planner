package com.sap.smartacademicplanner;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Task {

    // Properties
    private final IntegerProperty taskID = new SimpleIntegerProperty(this, "taskID");
    private final StringProperty subject = new SimpleStringProperty(this, "subject");
    private final StringProperty taskType = new SimpleStringProperty(this, "taskType");
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>(this, "dueDate");
    private final IntegerProperty estHours = new SimpleIntegerProperty(this, "estHours");
    private final StringProperty priority = new SimpleStringProperty(this, "priority", "Medium");
    private final StringProperty status = new SimpleStringProperty(this, "status", "Pending");

    private final ObjectProperty<LocalDate> completionDate = new SimpleObjectProperty<>(this, "completionDate");

    // Constructor for tasks with subject, type, due date, est hours
    public Task(int taskId, String subject, String taskType, LocalDate dueDate, int estHours) {
        this.taskID.set(taskId);
        this.subject.set(subject);
        this.taskType.set(taskType);
        this.dueDate.set(dueDate);
        this.estHours.set(estHours);
    }

    // Full constructor for completed/pending tasks
    public Task(int taskId, String subject, String taskType, LocalDate dueDate, int estHours, String status) {
        this.taskID.set(taskId);
        this.subject.set(subject);
        this.taskType.set(taskType);
        this.dueDate.set(dueDate);
        this.estHours.set(estHours);
        this.priority.set("Medium");
        this.status.set(status);
    }

    // For completed tasks with a completion date
    public Task(int taskId, String subject, String taskType, LocalDate dueDate, int estHours, LocalDate completionDate) {
        this(taskId, subject, taskType, dueDate, estHours, "Completed");
        this.completionDate.set(completionDate);
    }

    // ✅ NEW CONSTRUCTOR ADDED HERE:
    public Task(int taskId, String subject, String taskType, LocalDate dueDate, int estHours, String priority, String status) {
        this.taskID.set(taskId);
        this.subject.set(subject);
        this.taskType.set(taskType);
        this.dueDate.set(dueDate);
        this.estHours.set(estHours);
        this.priority.set(priority);
        this.status.set(status);
    }

    // Getters
    public int getTaskID() { return taskID.get(); }
    public String getSubject() { return subject.get(); }
    public String getTaskType() { return taskType.get(); }
    public LocalDate getDueDate() { return dueDate.get(); }
    public int getEstHours() { return estHours.get(); }
    public String getPriority() { return priority.get(); }
    public String getStatus() { return status.get(); }
    public LocalDate getCompletionDate() { return completionDate.get(); }

    // Property accessors
    public IntegerProperty taskIDProperty() { return taskID; }
    public StringProperty subjectProperty() { return subject; }
    public StringProperty taskTypeProperty() { return taskType; }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public IntegerProperty estHoursProperty() { return estHours; }
    public StringProperty priorityProperty() { return priority; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<LocalDate> completionDateProperty() { return completionDate; }
}