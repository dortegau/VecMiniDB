package com.dortegau.vecminidb.domain.services;

import com.dortegau.vecminidb.domain.entities.Vector;

import java.util.List;

/**
 * Interface for in-memory vector indexing operations.
 * Provides fast access to vectors by ID and efficient iteration over all vectors.
 */
public interface VectorIndex {
    
    /**
     * Adds a vector to the index.
     * If a vector with the same ID already exists, it will be replaced.
     * 
     * @param vector the vector to add
     * @throws IllegalArgumentException if vector is null
     */
    void add(Vector vector);
    
    /**
     * Retrieves a vector by its ID.
     * 
     * @param id the vector ID to look up
     * @return the vector if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    Vector get(String id);
    
    /**
     * Returns all vectors in the index.
     * The returned list is a snapshot and modifications will not affect the index.
     * 
     * @return list of all vectors in the index
     */
    List<Vector> all();
    
    /**
     * Returns the number of vectors in the index.
     * 
     * @return the size of the index
     */
    int size();
    
    /**
     * Checks if the index contains a vector with the given ID.
     * 
     * @param id the vector ID to check
     * @return true if a vector with this ID exists
     * @throws IllegalArgumentException if id is null
     */
    boolean contains(String id);
    
    /**
     * Removes a vector from the index.
     * 
     * @param id the ID of the vector to remove
     * @return the removed vector, or null if no vector with this ID existed
     * @throws IllegalArgumentException if id is null
     */
    Vector remove(String id);
    
    /**
     * Clears all vectors from the index.
     */
    void clear();
}