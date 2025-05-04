package com.example.automated_text_processor.controller;

import com.example.automated_text_processor.model.TextProcessor;
import com.example.automated_text_processor.model.BatchProcessor;
import com.example.automated_text_processor.util.LogManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;

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
    private Button batchProcessButton;
    @FXML
    private ListView<String> wordFrequencyList;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;

    private TextProcessor textProcessor;
    private BatchProcessor batchProcessor;
    private LogManager logManager;
    private Stage stage;

    @FXML
    public void initialize() {
        textProcessor = new TextProcessor();
        batchProcessor = new BatchProcessor();
        logManager = LogManager.getInstance();
        setupEventHandlers();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> performSearch());
        replaceButton.setOnAction(e -> performReplace());
        loadFileButton.setOnAction(e -> loadFile());
        saveFileButton.setOnAction(e -> saveFile());
        batchProcessButton.setOnAction(e -> processBatch());
        
        // Add text change listener to update word frequency in real-time
        inputTextArea.textProperty().addListener((obs, oldText, newText) -> {
            textProcessor.setText(newText);
            updateWordFrequency();
        });
    }

    @FXML
    private void processBatch() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Process");
        File directory = directoryChooser.showDialog(stage);

        if (directory != null) {
            String pattern = regexPatternField.getText();
            String replacement = replacementField.getText();

            if (pattern != null && !pattern.isEmpty()) {
                try {
                    progressBar.setProgress(0);
                    statusLabel.setText("Processing files...");
                    
                    Map<String, String> results = batchProcessor.processDirectory(
                            directory.getPath(),
                            pattern,
                            replacement
                    );

                    // Show results in output area
                    StringBuilder output = new StringBuilder();
                    for (Map.Entry<String, String> entry : results.entrySet()) {
                        output.append("File: ").append(entry.getKey()).append("\n");
                        output.append(entry.getValue()).append("\n\n");
                    }
                    outputTextArea.setText(output.toString());

                    // Ask for output directory
                    DirectoryChooser outputChooser = new DirectoryChooser();
                    outputChooser.setTitle("Select Output Directory");
                    File outputDir = outputChooser.showDialog(stage);

                    if (outputDir != null) {
                        batchProcessor.saveProcessedFiles(outputDir.getPath());
                        statusLabel.setText("Batch processing completed successfully");
                    }

                    progressBar.setProgress(1.0);
                    logManager.logInfo("Batch processing completed for directory: " + directory.getPath());

                } catch (Exception e) {
                    logManager.logError("Batch processing failed", e);
                    showError("Batch Processing Error", e.getMessage());
                    statusLabel.setText("Batch processing failed");
                }
            } else {
                showError("Invalid Pattern", "Please enter a valid regex pattern");
            }
        }
    }

    @FXML
    private void newFile() {
        inputTextArea.clear();
        outputTextArea.clear();
        regexPatternField.clear();
        replacementField.clear();
        wordFrequencyList.getItems().clear();
        statusLabel.setText("New file created");
        logManager.logInfo("New file created");
    }

    @FXML
    private void exit() {
        if (stage != null) {
            batchProcessor.shutdown();
            stage.close();
        }
    }

    @FXML
    private void toggleFullScreen() {
        if (stage != null) {
            stage.setFullScreen(!stage.isFullScreen());
            logManager.logInfo("Full screen mode toggled");
        }
    }

    @FXML
    private void showFindDialog() {
        regexPatternField.requestFocus();
    }

    @FXML
    private void showReplaceDialog() {
        replacementField.requestFocus();
    }

    @FXML
    private void performSearch() {
        String pattern = regexPatternField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            try {
                long startTime = System.currentTimeMillis();
                List<String> matches = textProcessor.searchWithRegex(pattern);
                outputTextArea.setText(String.join("\n", matches));
                updateWordFrequency();
                logManager.logPerformance("Search operation", startTime);
                statusLabel.setText("Search completed successfully");
            } catch (Exception e) {
                logManager.logError("Search operation failed", e);
                showError("Regex Error", "Invalid regular expression pattern: " + e.getMessage());
                statusLabel.setText("Search failed");
            }
        }
    }

    @FXML
    private void performReplace() {
        String pattern = regexPatternField.getText();
        String replacement = replacementField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            try {
                long startTime = System.currentTimeMillis();
                String result = textProcessor.replaceWithRegex(pattern, replacement);
                outputTextArea.setText(result);
                updateWordFrequency();
                logManager.logPerformance("Replace operation", startTime);
                statusLabel.setText("Replace completed successfully");
            } catch (Exception e) {
                logManager.logError("Replace operation failed", e);
                showError("Regex Error", "Invalid regular expression pattern: " + e.getMessage());
                statusLabel.setText("Replace failed");
            }
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
            try {
                long startTime = System.currentTimeMillis();
                String content = Files.readString(file.toPath());
                inputTextArea.setText(content);
                textProcessor.setText(content);
                updateWordFrequency();
                logManager.logPerformance("File load operation", startTime);
                statusLabel.setText("File loaded successfully");
            } catch (IOException e) {
                logManager.logError("File load failed", e);
                showError("Error loading file", e.getMessage());
                statusLabel.setText("File load failed");
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
            try {
                long startTime = System.currentTimeMillis();
                Files.writeString(file.toPath(), outputTextArea.getText());
                logManager.logPerformance("File save operation", startTime);
                statusLabel.setText("File saved successfully");
            } catch (IOException e) {
                logManager.logError("File save failed", e);
                showError("Error saving file", e.getMessage());
                statusLabel.setText("File save failed");
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