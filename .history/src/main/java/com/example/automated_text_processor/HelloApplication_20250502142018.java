package com.example.automated_text_processor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("text-processor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        
        // Set minimum window size
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Set initial window size to 80% of screen size
        Screen screen = Screen.getPrimary();
        stage.setWidth(screen.getVisualBounds().getWidth() * 0.8);
        stage.setHeight(screen.getVisualBounds().getHeight() * 0.8);
        
        stage.setTitle("Text Processor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}