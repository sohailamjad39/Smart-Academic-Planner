module com.sap.smartacademicplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.sap.smartacademicplanner to javafx.fxml;
    exports com.sap.smartacademicplanner;
}
