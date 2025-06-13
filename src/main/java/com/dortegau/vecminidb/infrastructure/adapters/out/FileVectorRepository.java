package com.dortegau.vecminidb.infrastructure.adapters.out;

import com.dortegau.vecminidb.application.ports.out.VectorRepository;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.VectorIndex;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based implementation of vector repository with WAL support.
 * Stores vectors in a serialized file format with write-ahead logging for durability.
 * Uses an in-memory index for fast access.
 */
public class FileVectorRepository implements VectorRepository {
    
    private final Map<VectorId, Vector> vectors;
    private final VectorIndex memoryIndex;
    private final WriteAheadLog writeAheadLog;
    private final Path dataFile;
    
    /**
     * Creates a new file-based vector repository.
     * 
     * @param filePath the path to the data file
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public FileVectorRepository(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        if (filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        
        this.vectors = new ConcurrentHashMap<>();
        this.memoryIndex = new FlatVectorIndex();
        this.dataFile = Paths.get(filePath);
        this.writeAheadLog = new WriteAheadLog(dataFile);
        
        loadFromFile();
        recoverFromWAL();
    }
    
    @Override
    public void save(Vector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        
        // Log to WAL first for durability
        writeAheadLog.logInsert(vector);
        
        // Update in-memory structures
        vectors.put(vector.id(), vector);
        memoryIndex.add(vector);
        
        // Persist to disk
        saveToFile();
    }
    
    @Override
    public Optional<Vector> findById(VectorId vectorId) {
        if (vectorId == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        return Optional.ofNullable(vectors.get(vectorId));
    }
    
    @Override
    public boolean deleteById(VectorId vectorId) {
        if (vectorId == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        Vector removed = vectors.remove(vectorId);
        boolean existed = removed != null;
        if (existed) {
            memoryIndex.remove(removed.getIdValue());
            saveToFile();
        }
        return existed;
    }
    
    @Override
    public Set<Vector> findAll() {
        return new HashSet<>(vectors.values());
    }
    
    @Override
    public Set<VectorId> findAllIds() {
        return new HashSet<>(vectors.keySet());
    }
    
    @Override
    public int count() {
        return vectors.size();
    }
    
    private void saveToFile() {
        try {
            if (dataFile.getParent() != null) {
                Files.createDirectories(dataFile.getParent());
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dataFile))) {
                oos.writeObject(new HashMap<>(vectors));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save vectors to file: " + dataFile, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(dataFile)) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(dataFile))) {
            Map<VectorId, Vector> loaded = (Map<VectorId, Vector>) ois.readObject();
            vectors.putAll(loaded);
            
            // Populate memory index
            for (Vector vector : loaded.values()) {
                memoryIndex.add(vector);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: Could not load existing vectors from file: " + e.getMessage());
        }
    }
    
    /**
     * Recovers vectors from the Write-Ahead Log and adds them to the repository.
     * This is called during startup to ensure no data is lost.
     */
    private void recoverFromWAL() {
        try {
            List<Vector> recoveredVectors = writeAheadLog.recover();
            
            for (Vector vector : recoveredVectors) {
                // Only add if not already in memory (avoid duplicates)
                if (!memoryIndex.contains(vector.getIdValue())) {
                    vectors.put(vector.id(), vector);
                    memoryIndex.add(vector);
                }
            }
            
            // If we recovered any vectors, persist them to the main file
            if (!recoveredVectors.isEmpty()) {
                saveToFile();
                // Clear WAL after successful recovery
                writeAheadLog.clear();
            }
            
        } catch (Exception e) {
            System.err.println("Warning: Failed to recover from WAL: " + e.getMessage());
        }
    }
    
    /**
     * Gets the in-memory vector index for fast query operations.
     * 
     * @return the vector index
     */
    public VectorIndex getMemoryIndex() {
        return memoryIndex;
    }
    
    /**
     * Gets all vectors from the in-memory index.
     * This is much faster than reading from disk.
     * 
     * @return list of all vectors in memory
     */
    public List<Vector> getAllVectorsFromMemory() {
        return memoryIndex.all();
    }
    
    /**
     * Forces a write of all in-memory vectors to disk and clears the WAL.
     * Useful for explicit checkpointing.
     */
    public void checkpoint() {
        saveToFile();
        writeAheadLog.clear();
    }
}