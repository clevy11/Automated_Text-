package com.example.automated_text_processor.controller;

import com.example.automated_text_processor.model.TextProcessor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.*;
import java.util.List;
import java.util.Map;

public class TextProcessorController {
    @FXML
    private TextArea inputTextArea;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private TextField regexPatternField;
    @FXML
    private TextField replacementField;
    @FXML
    private Button searchButton;
    @FXML
    private Button replaceButton;
    @FXML
    private Button loadFileButton;
    @FXML
    private Button saveFileButton;
    @FXML
    private ListView<String> wordFrequencyList;

    private TextProcessor textProcessor;

    @FXML
    public void initialize() {
        textProcessor = new TextProcessor();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> performSearch());
        replaceButton.setOnAction(e -> performReplace());
        loadFileButton.setOnAction(e -> loadFile());
        saveFileButton.setOnAction(e -> saveFile());
    }

    @FXML
    private void performSearch() {
        String pattern = regexPatternField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            List<String> matches = textProcessor.searchWithRegex(pattern);
            outputTextArea.setText(String.join("\n", matches));
            updateWordFrequency();
        }
    }

    @FXML
    private void performReplace() {
        String pattern = regexPatternField.getText();
        String replacement = replacementField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            String result = textProcessor.replaceWithRegex(pattern, replacement);
            outputTextArea.setText(result);
            updateWordFrequency();
        }
    }

    @FXML
    private void loadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                inputTextArea.setText(content.toString());
                textProcessor.setText(content.toString());
                updateWordFrequency();
            } catch (IOException e) {
                showError("Error loading file", e.getMessage());
            }
        }
    }

    @FXML
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showSaveDialog(inputTextArea.getScene().getWindow());
        
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(outputTextArea.getText());
            } catch (IOException e) {
                showError("Error saving file", e.getMessage());
            }
        }
    }

    private void updateWordFrequency() {
        wordFrequencyList.getItems().clear();
        Map<String, Integer> frequency = textProcessor.getWordFrequency();
        frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> wordFrequencyList.getItems().add(
                    String.format("%s: %d", entry.getKey(), entry.getValue())
                ));
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 