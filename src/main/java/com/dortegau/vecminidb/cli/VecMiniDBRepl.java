package com.dortegau.vecminidb.cli;

import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.infrastructure.adapters.in.MiniTextEmbedder;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorDatabaseFacade;
import com.dortegau.vecminidb.infrastructure.config.VectorDatabaseFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * VecMiniDB REPL (Read-Eval-Print Loop) CLI application.
 * Provides an interactive command-line interface for vector database operations.
 */
public class VecMiniDBRepl {
    
    private static final String VERSION = "1.0.0";
    private static final String DEFAULT_DB_FILE = "vecminidb_repl.vecdb";
    private static final int DEFAULT_EMBEDDING_DIMENSIONS = 64;
    private static final int DEFAULT_QUERY_LIMIT = 5;
    
    private final VectorDatabaseFacade database;
    private final MiniTextEmbedder embedder;
    private final BufferedReader reader;
    private boolean running;
    
    public VecMiniDBRepl() {
        this(DEFAULT_DB_FILE);
    }
    
    public VecMiniDBRepl(String databaseFile) {
        this.database = VectorDatabaseFactory.createDefault(databaseFile);
        this.embedder = new MiniTextEmbedder(DEFAULT_EMBEDDING_DIMENSIONS);
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.running = true;
        
        // Load existing data to build vocabulary
        loadExistingVocabulary();
    }
    
    public static void main(String[] args) {
        String dbFile = args.length > 0 ? args[0] : DEFAULT_DB_FILE;
        
        VecMiniDBRepl repl = new VecMiniDBRepl(dbFile);
        repl.start();
    }
    
    public void start() {
        printWelcome();
        
        while (running) {
            try {
                System.out.print("vecminidb> ");
                String input = reader.readLine();
                
                if (input == null) {
                    break; // EOF
                }
                
                input = input.trim();
                if (input.isEmpty()) {
                    continue;
                }
                
                executeCommand(input);
                
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        printGoodbye();
    }
    
    private void executeCommand(String input) {
        String[] parts = parseCommand(input);
        if (parts.length == 0) {
            return;
        }
        
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help", "h", "?" -> showHelp();
            case "insert", "ins" -> handleInsert(parts);
            case "query", "q" -> handleQuery(parts);
            case "vector", "vec" -> handleVector(parts);
            case "meta", "metadata" -> handleMetadata(parts);
            case "list", "ls" -> handleList();
            case "size", "count" -> handleSize();
            case "clear" -> handleClear();
            case "vocab" -> handleVocab();
            case "stats" -> handleStats();
            case "exit", "quit", "q!" -> handleExit();
            default -> System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
        }
    }
    
    private String[] parseCommand(String input) {
        // Simple parsing that handles quoted strings
        if (!input.contains("\"")) {
            return input.split("\\s+");
        }
        
        // Handle quoted text (simple implementation)
        return parseQuotedCommand(input);
    }
    
    private String[] parseQuotedCommand(String input) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    parts.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        
        return parts.toArray(new String[0]);
    }
    
    private void handleInsert(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: insert <id> <text>");
            System.out.println("Example: insert doc1 \"This is a sample document\"");
            return;
        }
        
        String id = parts[1];
        String text = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
        
        try {
            Vector vector = embedder.embedText(text, id);
            database.insert(vector);
            System.out.println("✓ Inserted vector '" + id + "' (" + vector.getValues().length + " dimensions)");
        } catch (Exception e) {
            System.err.println("Failed to insert vector: " + e.getMessage());
        }
    }
    
    private void handleQuery(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: query <text> [limit]");
            System.out.println("Example: query \"machine learning\" 3");
            return;
        }
        
        int limit = DEFAULT_QUERY_LIMIT;
        String text;
        
        if (parts.length > 2) {
            try {
                limit = Integer.parseInt(parts[parts.length - 1]);
                text = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1));
            } catch (NumberFormatException e) {
                text = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
            }
        } else {
            text = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        }
        
        try {
            Vector queryVector = embedder.embedQuery(text);
            List<VectorSimilarityUseCase.SimilarityResult> results = 
                database.findSimilarWithScores(queryVector, limit);
            
            if (results.isEmpty()) {
                System.out.println("No similar vectors found.");
                return;
            }
            
            System.out.println("Found " + results.size() + " similar vectors:");
            System.out.println("┌─────────────────────────────────────────┬─────────────┐");
            System.out.println("│ Vector ID                               │ Similarity  │");
            System.out.println("├─────────────────────────────────────────┼─────────────┤");
            
            for (VectorSimilarityUseCase.SimilarityResult result : results) {
                String id = result.vector().getIdValue();
                String similarity = String.format("%.4f", result.similarity());
                System.out.printf("│ %-39s │ %11s │%n", 
                    truncate(id, 39), similarity);
            }
            
            System.out.println("└─────────────────────────────────────────┴─────────────┘");
            
        } catch (Exception e) {
            System.err.println("Query failed: " + e.getMessage());
        }
    }
    
    private void handleVector(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: vector <id>");
            System.out.println("Example: vector doc1");
            return;
        }
        
        String id = parts[1];
        Vector vector = database.get(id);
        
        if (vector == null) {
            System.out.println("Vector with ID '" + id + "' not found.");
            return;
        }
        
        System.out.println("Vector ID: " + vector.getIdValue());
        System.out.println("Dimensions: " + vector.getValues().length);
        System.out.println("Values: " + formatVector(vector.getValues()));
    }
    
    private void handleMetadata(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: meta <id>");
            System.out.println("Example: meta doc1");
            return;
        }
        
        String id = parts[1];
        Vector vector = database.get(id);
        
        if (vector == null) {
            System.out.println("Vector with ID '" + id + "' not found.");
            return;
        }
        
        double[] values = vector.getValues();
        
        // Calculate statistics
        double min = Arrays.stream(values).min().orElse(0.0);
        double max = Arrays.stream(values).max().orElse(0.0);
        double mean = Arrays.stream(values).average().orElse(0.0);
        double norm = Math.sqrt(Arrays.stream(values).map(x -> x * x).sum());
        
        System.out.println("Metadata for vector '" + id + "':");
        System.out.println("  ID: " + vector.getIdValue());
        System.out.println("  Dimensions: " + values.length);
        System.out.println("  Min value: " + String.format("%.6f", min));
        System.out.println("  Max value: " + String.format("%.6f", max));
        System.out.println("  Mean: " + String.format("%.6f", mean));
        System.out.println("  L2 norm: " + String.format("%.6f", norm));
    }
    
    private void handleList() {
        Set<String> ids = database.getAllIds();
        
        if (ids.isEmpty()) {
            System.out.println("No vectors in database.");
            return;
        }
        
        System.out.println("Vector IDs (" + ids.size() + " total):");
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│ Vector ID                               │");
        System.out.println("├─────────────────────────────────────────┤");
        
        for (String id : ids.stream().sorted().toList()) {
            System.out.printf("│ %-39s │%n", truncate(id, 39));
        }
        
        System.out.println("└─────────────────────────────────────────┘");
    }
    
    private void handleSize() {
        int size = database.size();
        System.out.println("Database contains " + size + " vector" + (size != 1 ? "s" : ""));
    }
    
    private void handleClear() {
        System.out.print("Are you sure you want to clear all vectors? (yes/no): ");
        try {
            String response = reader.readLine();
            if ("yes".equalsIgnoreCase(response.trim())) {
                // Note: This would require implementing a clear method
                System.out.println("Clear operation not implemented yet.");
            } else {
                System.out.println("Clear operation cancelled.");
            }
        } catch (IOException e) {
            System.err.println("Error reading confirmation: " + e.getMessage());
        }
    }
    
    private void handleVocab() {
        Set<String> vocabulary = embedder.getVocabulary();
        System.out.println("Text embedder vocabulary (" + vocabulary.size() + " terms):");
        
        if (vocabulary.isEmpty()) {
            System.out.println("  (empty - insert some text vectors to build vocabulary)");
            return;
        }
        
        vocabulary.stream()
            .sorted()
            .limit(20)
            .forEach(term -> System.out.println("  " + term));
        
        if (vocabulary.size() > 20) {
            System.out.println("  ... and " + (vocabulary.size() - 20) + " more terms");
        }
    }
    
    private void handleStats() {
        int vectorCount = database.size();
        int vocabularySize = embedder.getVocabularySize();
        
        System.out.println("VecMiniDB Statistics:");
        System.out.println("  Vectors: " + vectorCount);
        System.out.println("  Vocabulary size: " + vocabularySize);
        System.out.println("  Embedding dimensions: " + DEFAULT_EMBEDDING_DIMENSIONS);
        
        if (vectorCount > 0) {
            // Estimate memory usage
            long estimatedMemory = vectorCount * DEFAULT_EMBEDDING_DIMENSIONS * 8L; // 8 bytes per double
            System.out.println("  Estimated vector memory: " + formatBytes(estimatedMemory));
        }
    }
    
    private void handleExit() {
        running = false;
    }
    
    private void loadExistingVocabulary() {
        // This helps the embedder build vocabulary from existing vectors
        Set<String> existingIds = database.getAllIds();
        System.out.println("Loaded database with " + existingIds.size() + " existing vectors.");
    }
    
    private void printWelcome() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                    VecMiniDB REPL v" + VERSION + "                   ║");
        System.out.println("║           Interactive Vector Database CLI                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Type 'help' for available commands or 'exit' to quit.");
        System.out.println();
    }
    
    private void printGoodbye() {
        System.out.println();
        System.out.println("Thank you for using VecMiniDB REPL!");
        System.out.println("Your data has been saved automatically.");
    }
    
    private void showHelp() {
        System.out.println("VecMiniDB REPL Commands:");
        System.out.println();
        System.out.println("Data Operations:");
        System.out.println("  insert <id> <text>      Insert text as vector with given ID");
        System.out.println("  query <text> [limit]    Find similar vectors (default limit: 5)");
        System.out.println("  vector <id>             Show vector values for given ID");
        System.out.println("  meta <id>               Show metadata and statistics for vector");
        System.out.println();
        System.out.println("Database Operations:");
        System.out.println("  list                    List all vector IDs");
        System.out.println("  size                    Show number of vectors in database");
        System.out.println("  stats                   Show database statistics");
        System.out.println("  vocab                   Show text embedder vocabulary");
        System.out.println();
        System.out.println("Control Commands:");
        System.out.println("  help                    Show this help message");
        System.out.println("  exit                    Exit the REPL");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  insert doc1 \"Machine learning is fascinating\"");
        System.out.println("  query \"artificial intelligence\" 3");
        System.out.println("  vector doc1");
        System.out.println("  meta doc1");
        System.out.println();
        System.out.println("Tips:");
        System.out.println("  - Use quotes around text with spaces");
        System.out.println("  - Commands are case-insensitive");
        System.out.println("  - Data is automatically saved");
    }
    
    private String formatVector(double[] values) {
        if (values.length <= 5) {
            return Arrays.toString(values);
        }
        
        return String.format("[%.3f, %.3f, %.3f, ... %.3f, %.3f] (%d dimensions)",
            values[0], values[1], values[2], 
            values[values.length - 2], values[values.length - 1],
            values.length);
    }
    
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}