module com.sap.smartacademicplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.sap.smartacademicplanner to javafx.fxml;
    exports com.sap.smartacademicplanner;
}
