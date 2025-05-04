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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.application.Platform;

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
    @FXML
    private ComboBox<String> predefinedPatternCombo;
    @FXML
    private TextFlow inputTextFlow;

    private TextProcessor textProcessor;
    private BatchProcessor batchProcessor;
    private LogManager logManager;
    private Stage stage;

    private static final Map<String, String> PREDEFINED_PATTERNS = Map.of(
        "Email", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        "Phone", "\\b\\d{10,15}\\b",
        "URL", "https?://[\\w.-]+(?:\\.[\\w.-]+)+[/#?]?.*",
        "Number", "\\b\\d+\\b",
        "Word", "\\b\\w+\\b",
        "Date (YYYY-MM-DD)", "\\b\\d{4}-\\d{2}-\\d{2}\\b"
    );

    @FXML
    public void initialize() {
        textProcessor = new TextProcessor();
        batchProcessor = new BatchProcessor();
        logManager = LogManager.getInstance();
        setupEventHandlers();
        setupPredefinedPatterns();
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

    private void setupPredefinedPatterns() {
        predefinedPatternCombo.getItems().addAll(PREDEFINED_PATTERNS.keySet());
        predefinedPatternCombo.setOnAction(e -> {
            String selected = predefinedPatternCombo.getValue();
            if (selected != null) {
                regexPatternField.setText(PREDEFINED_PATTERNS.get(selected));
            }
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
                new Thread(() -> {
                    try {
                        Platform.runLater(() -> {
                            progressBar.setProgress(0);
                            statusLabel.setText("Processing files...");
                        });
                        Map<String, String> results = batchProcessor.processDirectory(
                                directory.getPath(),
                                pattern,
                                replacement
                        );
                        Platform.runLater(() -> {
                            StringBuilder output = new StringBuilder();
                            for (Map.Entry<String, String> entry : results.entrySet()) {
                                output.append("File: ").append(entry.getKey()).append("\n");
                                output.append(entry.getValue()).append("\n\n");
                            }
                            outputTextArea.setText(output.toString());
                        });
                        DirectoryChooser outputChooser = new DirectoryChooser();
                        outputChooser.setTitle("Select Output Directory");
                        File outputDir = outputChooser.showDialog(stage);
                        if (outputDir != null) {
                            batchProcessor.saveProcessedFiles(outputDir.getPath());
                            Platform.runLater(() -> statusLabel.setText("Batch processing completed successfully"));
                        }
                        Platform.runLater(() -> progressBar.setProgress(1.0));
                        logManager.logInfo("Batch processing completed for directory: " + directory.getPath());
                    } catch (Exception e) {
                        logManager.logError("Batch processing failed", e);
                        Platform.runLater(() -> {
                            showError("Batch Processing Error", e.getMessage());
                            statusLabel.setText("Batch processing failed");
                        });
                    }
                }).start();
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
                highlightMatches(pattern);
                logManager.logPerformance("Search operation", startTime);
                statusLabel.setText("Search completed successfully");
            } catch (Exception e) {
                logManager.logError("Search operation failed", e);
                showError("Regex Error", "Invalid regular expression pattern: " + e.getMessage());
                statusLabel.setText("Search failed");
            }
        }
    }

    private void highlightMatches(String pattern) {
        // Use TextFlow to highlight matches in the input text
        String text = inputTextArea.getText();
        inputTextFlow.getChildren().clear();
        if (pattern == null || pattern.isEmpty() || text == null) {
            inputTextFlow.getChildren().add(new Text(text));
            return;
        }
        try {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(text);
            int lastEnd = 0;
            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    inputTextFlow.getChildren().add(new Text(text.substring(lastEnd, matcher.start())));
                }
                Text highlight = new Text(text.substring(matcher.start(), matcher.end()));
                highlight.setStyle("-fx-fill: #000; -fx-background-color: #FFEB3B; -fx-font-weight: bold; -fx-underline: true;");
                inputTextFlow.getChildren().add(highlight);
                lastEnd = matcher.end();
            }
            if (lastEnd < text.length()) {
                inputTextFlow.getChildren().add(new Text(text.substring(lastEnd)));
            }
        } catch (Exception e) {
            inputTextFlow.getChildren().add(new Text(text));
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