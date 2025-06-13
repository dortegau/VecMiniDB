package com.dortegau.vecminidb.infrastructure.adapters.out;

import com.dortegau.vecminidb.domain.entities.Vector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Write-Ahead Log implementation for ensuring durability of vector operations.
 * Records all insert operations before they are committed to the main database file.
 */
public class WriteAheadLog {
    
    private final Path walFile;
    
    public WriteAheadLog(Path databaseFile) {
        if (databaseFile == null) {
            throw new IllegalArgumentException("Database file path cannot be null");
        }
        
        // Create WAL file path: if db is "vecs.vecdb", WAL is "vecs.wal"
        String dbFileName = databaseFile.getFileName().toString();
        String walFileName = dbFileName.replaceAll("\\.[^.]*$", ".wal");
        this.walFile = databaseFile.getParent() != null 
            ? databaseFile.getParent().resolve(walFileName)
            : Path.of(walFileName);
    }
    
    /**
     * Logs a vector insert operation to the WAL.
     * Format: {"id":"vectorId","values":[1.0,2.0,3.0]}
     * 
     * @param vector the vector to log
     * @throws IllegalArgumentException if vector is null
     */
    public void logInsert(Vector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(walFile, 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND)) {
            
            String jsonLine = vectorToJson(vector);
            writer.write(jsonLine);
            writer.newLine();
            writer.flush(); // Ensure durability
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to WAL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recovers all vectors from the WAL.
     * 
     * @return list of vectors found in the WAL
     */
    public List<Vector> recover() {
        List<Vector> recoveredVectors = new ArrayList<>();
        
        if (!Files.exists(walFile)) {
            return recoveredVectors; // No WAL file, nothing to recover
        }
        
        try (BufferedReader reader = Files.newBufferedReader(walFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        Vector vector = jsonToVector(line);
                        recoveredVectors.add(vector);
                    } catch (Exception e) {
                        // Log malformed entry but continue recovery
                        System.err.println("Warning: Skipping malformed WAL entry: " + line);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from WAL: " + e.getMessage(), e);
        }
        
        return recoveredVectors;
    }
    
    /**
     * Clears the WAL file after successful recovery.
     */
    public void clear() {
        try {
            if (Files.exists(walFile)) {
                Files.delete(walFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear WAL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if WAL file exists and has content.
     * 
     * @return true if WAL exists and is not empty
     */
    public boolean hasEntries() {
        try {
            return Files.exists(walFile) && Files.size(walFile) > 0;
        } catch (IOException e) {
            return false;
        }
    }
    
    // JSON serialization without external dependencies
    
    private String vectorToJson(Vector vector) {
        StringBuilder json = new StringBuilder();
        json.append("{\"id\":\"").append(escapeJson(vector.getIdValue())).append("\",\"values\":[");
        
        double[] values = vector.getValues();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) json.append(",");
            json.append(values[i]);
        }
        
        json.append("]}");
        return json.toString();
    }
    
    private Vector jsonToVector(String json) {
        // Simple JSON parser for our specific format
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        
        // Extract id
        String idPattern = "\"id\":\"";
        int idStart = json.indexOf(idPattern);
        if (idStart == -1) {
            throw new IllegalArgumentException("Missing id field");
        }
        idStart += idPattern.length();
        
        int idEnd = json.indexOf("\"", idStart);
        if (idEnd == -1) {
            throw new IllegalArgumentException("Malformed id field");
        }
        
        String id = unescapeJson(json.substring(idStart, idEnd));
        
        // Extract values array
        String valuesPattern = "\"values\":[";
        int valuesStart = json.indexOf(valuesPattern);
        if (valuesStart == -1) {
            throw new IllegalArgumentException("Missing values field");
        }
        valuesStart += valuesPattern.length();
        
        int valuesEnd = json.indexOf("]", valuesStart);
        if (valuesEnd == -1) {
            throw new IllegalArgumentException("Malformed values field");
        }
        
        String valuesStr = json.substring(valuesStart, valuesEnd);
        
        // Parse values array
        List<Double> valuesList = new ArrayList<>();
        if (!valuesStr.trim().isEmpty()) {
            String[] parts = valuesStr.split(",");
            for (String part : parts) {
                try {
                    valuesList.add(Double.parseDouble(part.trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number in values: " + part);
                }
            }
        }
        
        double[] values = valuesList.stream().mapToDouble(Double::doubleValue).toArray();
        return Vector.of(id, values);
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private String unescapeJson(String str) {
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
}