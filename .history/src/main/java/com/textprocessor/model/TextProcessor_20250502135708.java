package com.textprocessor.model;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class TextProcessor {
    private String currentText;
    private List<String> processedFiles;
    private Map<String, Integer> wordFrequency;

    public TextProcessor() {
        this.processedFiles = new ArrayList<>();
        this.wordFrequency = new HashMap<>();
    }

    public String searchWithRegex(String pattern, String text) {
        try {
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(text);
            StringBuilder matches = new StringBuilder();
            
            while (matcher.find()) {
                matches.append(matcher.group()).append("\n");
            }
            
            return matches.toString();
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage());
        }
    }

    public String replaceWithRegex(String pattern, String replacement, String text) {
        try {
            return text.replaceAll(pattern, replacement);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage());
        }
    }

    public void processFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            currentText = reader.lines().collect(Collectors.joining("\n"));
            processedFiles.add(filePath);
            updateWordFrequency();
        }
    }

    public void saveToFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    private void updateWordFrequency() {
        wordFrequency.clear();
        if (currentText != null) {
            Arrays.stream(currentText.toLowerCase().split("\\W+"))
                  .filter(word -> !word.isEmpty())
                  .forEach(word -> wordFrequency.merge(word, 1, Integer::sum));
        }
    }

    public Map<String, Integer> getWordFrequency() {
        return Collections.unmodifiableMap(wordFrequency);
    }

    public String getCurrentText() {
        return currentText;
    }

    public void setCurrentText(String text) {
        this.currentText = text;
        updateWordFrequency();
    }

    public List<String> getProcessedFiles() {
        return Collections.unmodifiableList(processedFiles);
    }
} 