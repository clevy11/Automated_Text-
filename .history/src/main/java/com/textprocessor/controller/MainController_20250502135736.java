package com.textprocessor.controller;

import com.textprocessor.model.TextProcessor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MainController {
    @FXML private TextArea inputTextArea;
    @FXML private TextArea outputTextArea;
    @FXML private TextField regexPatternField;
    @FXML private TextField replacementField;
    @FXML private ListView<String> processedFilesList;
    @FXML private TableView<Map.Entry<String, Integer>> wordFrequencyTable;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> wordColumn;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> frequencyColumn;

    private TextProcessor textProcessor;
    private FileChooser fileChooser;

    @FXML
    public void initialize() {
        textProcessor = new TextProcessor();
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        // Initialize word frequency table
        wordColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getKey()));
        frequencyColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getValue()).asObject());
    }

    @FXML
    private void handleOpenFile() {
        File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                textProcessor.processFile(file.getAbsolutePath());
                inputTextArea.setText(textProcessor.getCurrentText());
                updateProcessedFilesList();
                updateWordFrequencyTable();
            } catch (IOException e) {
                showError("Error opening file", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveFile() {
        File file = fileChooser.showSaveDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                textProcessor.saveToFile(file.getAbsolutePath(), outputTextArea.getText());
            } catch (IOException e) {
                showError("Error saving file", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        String pattern = regexPatternField.getText();
        String text = inputTextArea.getText();
        
        try {
            String matches = textProcessor.searchWithRegex(pattern, text);
            outputTextArea.setText(matches);
        } catch (IllegalArgumentException e) {
            showError("Regex Error", e.getMessage());
        }
    }

    @FXML
    private void handleReplace() {
        String pattern = regexPatternField.getText();
        String replacement = replacementField.getText();
        String text = inputTextArea.getText();
        
        try {
            String result = textProcessor.replaceWithRegex(pattern, replacement, text);
            outputTextArea.setText(result);
        } catch (IllegalArgumentException e) {
            showError("Regex Error", e.getMessage());
        }
    }

    private void updateProcessedFilesList() {
        processedFilesList.getItems().setAll(textProcessor.getProcessedFiles());
    }

    private void updateWordFrequencyTable() {
        wordFrequencyTable.getItems().setAll(
            textProcessor.getWordFrequency().entrySet()
        );
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 