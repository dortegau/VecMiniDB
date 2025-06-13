package com.dortegau.vecminidb.application.ports.in;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Input port for vector database operations.
 * Defines the use cases available for vector database management.
 */
public interface VectorDatabaseUseCase {
    
    /**
     * Stores a vector in the database.
     * 
     * @param vector the vector to store
     * @throws IllegalArgumentException if vector is null
     */
    void storeVector(Vector vector);
    
    /**
     * Retrieves a vector by its ID.
     * 
     * @param vectorId the vector identifier
     * @return optional containing the vector if found, empty otherwise
     * @throws IllegalArgumentException if vectorId is null
     */
    Optional<Vector> getVector(VectorId vectorId);
    
    /**
     * Deletes a vector from the database.
     * 
     * @param vectorId the vector identifier
     * @return true if vector was deleted, false if not found
     * @throws IllegalArgumentException if vectorId is null
     */
    boolean deleteVector(VectorId vectorId);
    
    /**
     * Gets all vector IDs in the database.
     * 
     * @return set of all vector IDs
     */
    Set<VectorId> getAllVectorIds();
    
    /**
     * Gets the total number of vectors in the database.
     * 
     * @return number of vectors stored
     */
    int getVectorCount();
}