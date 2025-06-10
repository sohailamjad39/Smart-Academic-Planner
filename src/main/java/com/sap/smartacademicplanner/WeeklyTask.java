package com.sap.smartacademicplanner;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WeeklyTask {
    private final StringProperty day;
    private final StringProperty task;
    private final StringProperty type;
    private final StringProperty suggestedTime;
    private final StringProperty priority;

    public WeeklyTask(String day, String task, String type, String suggestedTime, String priority) {
        this.day = new SimpleStringProperty(day);
        this.task = new SimpleStringProperty(task);
        this.type = new SimpleStringProperty(type);
        this.suggestedTime = new SimpleStringProperty(suggestedTime);
        this.priority = new SimpleStringProperty(priority);
    }

    public StringProperty dayProperty() { return day; }
    public StringProperty taskProperty() { return task; }
    public StringProperty typeProperty() { return type; }
    public StringProperty suggestedTimeProperty() { return suggestedTime; }
    public StringProperty priorityProperty() { return priority; }
}
