module com.example.automated_text_processor {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.automated_text_processor to javafx.fxml;
    opens com.example.automated_text_processor.controller to javafx.fxml;
    exports com.example.automated_text_processor;
    exports com.example.automated_text_processor.controller;
}