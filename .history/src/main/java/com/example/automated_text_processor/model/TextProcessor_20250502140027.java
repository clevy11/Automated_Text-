package com.example.automated_text_processor.model;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class TextProcessor {
    private String text;
    private List<String> processedLines;
    private Map<String, Integer> wordFrequency;

    public TextProcessor() {
        this.processedLines = new ArrayList<>();
        this.wordFrequency = new HashMap<>();
    }

    public void setText(String text) {
        this.text = text;
        processText();
    }

    private void processText() {
        if (text != null) {
            processedLines = Arrays.asList(text.split("\n"));
            calculateWordFrequency();
        }
    }

    public List<String> searchWithRegex(String pattern) {
        List<String> matches = new ArrayList<>();
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(text);
        
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    public String replaceWithRegex(String pattern, String replacement) {
        return text.replaceAll(pattern, replacement);
    }

    private void calculateWordFrequency() {
        wordFrequency.clear();
        if (text != null) {
            Arrays.stream(text.toLowerCase().split("\\W+"))
                  .filter(word -> !word.isEmpty())
                  .forEach(word -> wordFrequency.merge(word, 1, Integer::sum));
        }
    }

    public Map<String, Integer> getWordFrequency() {
        return new HashMap<>(wordFrequency);
    }

    public List<String> getProcessedLines() {
        return new ArrayList<>(processedLines);
    }

    public String getText() {
        return text;
    }
} 