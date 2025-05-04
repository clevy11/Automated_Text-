package com.example.automated_text_processor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static LogManager instance;
    private final Path logFile;

    private LogManager() {
        try {
            // Create logs directory if it doesn't exist
            Files.createDirectories(Paths.get(LOG_DIR));
            logFile = Paths.get(LOG_DIR, "text_processor_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logging system", e);
        }
    }

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public void logInfo(String message) {
        writeLog("INFO", message);
    }

    public void logError(String message, Throwable error) {
        writeLog("ERROR", message + "\n" + getStackTrace(error));
    }

    public void logPerformance(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        writeLog("PERFORMANCE", operation + " completed in " + duration + "ms");
    }

    private void writeLog(String level, String message) {
        String logEntry = String.format("[%s] [%s] %s%n",
                LocalDateTime.now().format(DATE_FORMAT),
                level,
                message);

        try {
            Files.write(logFile,
                    logEntry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    private String getStackTrace(Throwable error) {
        StringBuilder sb = new StringBuilder();
        sb.append(error.toString()).append("\n");
        for (StackTraceElement element : error.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
} 