package com.dortegau.vecminidb.infrastructure.adapters.out;

import com.dortegau.vecminidb.application.ports.out.VectorRepository;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based implementation of vector repository.
 * Stores vectors in a serialized file format with automatic persistence.
 */
public class FileVectorRepository implements VectorRepository {
    
    private final Map<VectorId, Vector> vectors;
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
        this.dataFile = Paths.get(filePath);
        loadFromFile();
    }
    
    @Override
    public void save(Vector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        
        vectors.put(vector.id(), vector);
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
        
        boolean existed = vectors.remove(vectorId) != null;
        if (existed) {
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
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: Could not load existing vectors from file: " + e.getMessage());
        }
    }
}