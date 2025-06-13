package com.dortegau.vecminidb.application.ports.out;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.util.Optional;
import java.util.Set;

/**
 * Output port for vector persistence operations.
 * Defines the repository contract for vector storage.
 */
public interface VectorRepository {
    
    /**
     * Saves a vector to the repository.
     * 
     * @param vector the vector to save
     * @throws IllegalArgumentException if vector is null
     */
    void save(Vector vector);
    
    /**
     * Finds a vector by its ID.
     * 
     * @param vectorId the vector identifier
     * @return optional containing the vector if found, empty otherwise
     * @throws IllegalArgumentException if vectorId is null
     */
    Optional<Vector> findById(VectorId vectorId);
    
    /**
     * Deletes a vector by its ID.
     * 
     * @param vectorId the vector identifier
     * @return true if vector was deleted, false if not found
     * @throws IllegalArgumentException if vectorId is null
     */
    boolean deleteById(VectorId vectorId);
    
    /**
     * Finds all vectors in the repository.
     * 
     * @return set of all vectors
     */
    Set<Vector> findAll();
    
    /**
     * Gets all vector IDs in the repository.
     * 
     * @return set of all vector IDs
     */
    Set<VectorId> findAllIds();
    
    /**
     * Gets the total number of vectors in the repository.
     * 
     * @return number of vectors stored
     */
    int count();
}