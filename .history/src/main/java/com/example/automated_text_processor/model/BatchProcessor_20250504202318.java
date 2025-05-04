package com.example.automated_text_processor.model;

import com.example.automated_text_processor.util.LogManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BatchProcessor {
    private final TextProcessor textProcessor;
    private final LogManager logManager;
    private final ExecutorService executorService;
    private final Map<String, String> processedFiles;

    public BatchProcessor() {
        this.textProcessor = new TextProcessor();
        this.logManager = LogManager.getInstance();
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
        this.processedFiles = new ConcurrentHashMap<>();
    }

    public Map<String, String> processDirectory(String directoryPath, String regexPattern, String replacement) {
        long startTime = System.currentTimeMillis();
        logManager.logInfo("Starting batch processing of directory: " + directoryPath);

        try {
            List<Path> files = Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            List<Future<Void>> futures = new ArrayList<>();

            for (Path file : files) {
                futures.add(executorService.submit(() -> {
                    processFile(file, regexPattern, replacement);
                    return null;
                }));
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                future.get();
            }

            logManager.logPerformance("Batch processing", startTime);
            return new HashMap<>(processedFiles);

        } catch (Exception e) {
            logManager.logError("Error during batch processing", e);
            throw new RuntimeException("Batch processing failed", e);
        }
    }

    private void processFile(Path file, String regexPattern, String replacement) {
        long startTime = System.currentTimeMillis();
        String fileName = file.getFileName().toString();

        try {
            String content = Files.readString(file);
            textProcessor.setText(content);

            String processedContent;
            if (replacement != null && !replacement.isEmpty()) {
                processedContent = textProcessor.replaceWithRegex(regexPattern, replacement);
            } else {
                List<String> matches = textProcessor.searchWithRegex(regexPattern);
                processedContent = String.join("\n", matches);
            }

            processedFiles.put(fileName, processedContent);
            logManager.logPerformance("Processing file: " + fileName, startTime);

        } catch (IOException e) {
            logManager.logError("Error processing file: " + fileName, e);
            processedFiles.put(fileName, "Error: " + e.getMessage());
        }
    }

    public void saveProcessedFiles(String outputDirectory) {
        long startTime = System.currentTimeMillis();
        logManager.logInfo("Saving processed files to: " + outputDirectory);

        try {
            Files.createDirectories(Paths.get(outputDirectory));

            for (Map.Entry<String, String> entry : processedFiles.entrySet()) {
                Path outputFile = Paths.get(outputDirectory, "processed_" + entry.getKey());
                Files.writeString(outputFile, entry.getValue());
            }

            logManager.logPerformance("Saving processed files", startTime);

        } catch (IOException e) {
            logManager.logError("Error saving processed files", e);
            throw new RuntimeException("Failed to save processed files", e);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 