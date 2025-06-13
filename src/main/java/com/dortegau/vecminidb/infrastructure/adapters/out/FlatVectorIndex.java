package com.dortegau.vecminidb.infrastructure.adapters.out;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.VectorIndex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple flat vector index implementation using a ConcurrentHashMap.
 * Provides O(1) lookup by ID and efficient iteration over all vectors.
 * Thread-safe for concurrent read operations.
 */
public class FlatVectorIndex implements VectorIndex {
    
    private final Map<String, Vector> vectors;
    
    public FlatVectorIndex() {
        this.vectors = new ConcurrentHashMap<>();
    }
    
    @Override
    public void add(Vector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        
        vectors.put(vector.getIdValue(), vector);
    }
    
    @Override
    public Vector get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        return vectors.get(id);
    }
    
    @Override
    public List<Vector> all() {
        return new ArrayList<>(vectors.values());
    }
    
    @Override
    public int size() {
        return vectors.size();
    }
    
    @Override
    public boolean contains(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        return vectors.containsKey(id);
    }
    
    @Override
    public Vector remove(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        return vectors.remove(id);
    }
    
    @Override
    public void clear() {
        vectors.clear();
    }
    
    /**
     * Returns all vector IDs in the index.
     * 
     * @return set of all vector IDs
     */
    public Set<String> getAllIds() {
        return new HashSet<>(vectors.keySet());
    }
    
    /**
     * Adds all vectors from another index to this index.
     * Existing vectors with the same ID will be replaced.
     * 
     * @param otherIndex the index to copy vectors from
     * @throws IllegalArgumentException if otherIndex is null
     */
    public void addAll(VectorIndex otherIndex) {
        if (otherIndex == null) {
            throw new IllegalArgumentException("Other index cannot be null");
        }
        
        for (Vector vector : otherIndex.all()) {
            add(vector);
        }
    }
    
    /**
     * Creates a copy of this index.
     * 
     * @return a new FlatVectorIndex containing copies of all vectors
     */
    public FlatVectorIndex copy() {
        FlatVectorIndex copy = new FlatVectorIndex();
        copy.vectors.putAll(this.vectors);
        return copy;
    }
    
    @Override
    public String toString() {
        return "FlatVectorIndex{size=" + size() + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FlatVectorIndex that = (FlatVectorIndex) obj;
        return Objects.equals(vectors, that.vectors);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(vectors);
    }
}