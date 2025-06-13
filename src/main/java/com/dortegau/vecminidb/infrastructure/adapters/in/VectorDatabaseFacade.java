package com.dortegau.vecminidb.infrastructure.adapters.in;

import com.dortegau.vecminidb.application.ports.in.VectorDatabaseUseCase;
import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Facade providing a unified interface for vector database operations.
 * Combines database and similarity use cases into a single API.
 */
public class VectorDatabaseFacade {
    
    private final VectorDatabaseUseCase vectorDatabaseUseCase;
    private final VectorSimilarityUseCase vectorSimilarityUseCase;
    
    /**
     * Creates a new vector database facade.
     * 
     * @param vectorDatabaseUseCase the database use case implementation
     * @param vectorSimilarityUseCase the similarity use case implementation
     * @throws IllegalArgumentException if any parameter is null
     */
    public VectorDatabaseFacade(VectorDatabaseUseCase vectorDatabaseUseCase, 
                               VectorSimilarityUseCase vectorSimilarityUseCase) {
        if (vectorDatabaseUseCase == null) {
            throw new IllegalArgumentException("Vector database use case cannot be null");
        }
        if (vectorSimilarityUseCase == null) {
            throw new IllegalArgumentException("Vector similarity use case cannot be null");
        }
        
        this.vectorDatabaseUseCase = vectorDatabaseUseCase;
        this.vectorSimilarityUseCase = vectorSimilarityUseCase;
    }
    
    // Database operations
    
    /**
     * Stores a vector in the database.
     * 
     * @param vector the vector to store
     * @throws IllegalArgumentException if vector is null
     */
    public void insert(Vector vector) {
        vectorDatabaseUseCase.storeVector(vector);
    }
    
    /**
     * Retrieves a vector by its ID string.
     * 
     * @param id the vector identifier string
     * @return the vector if found, null otherwise
     * @throws IllegalArgumentException if id is null or empty
     */
    public Vector get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Vector ID cannot be empty");
        }
        
        return vectorDatabaseUseCase.getVector(VectorId.of(id)).orElse(null);
    }
    
    /**
     * Deletes a vector by its ID string.
     * 
     * @param id the vector identifier string
     * @return true if vector was deleted, false if not found
     * @throws IllegalArgumentException if id is null or empty
     */
    public boolean delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Vector ID cannot be empty");
        }
        
        return vectorDatabaseUseCase.deleteVector(VectorId.of(id));
    }
    
    /**
     * Gets all vector ID strings in the database.
     * 
     * @return set of all vector ID strings
     */
    public Set<String> getAllIds() {
        return vectorDatabaseUseCase.getAllVectorIds()
                .stream()
                .map(VectorId::value)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Gets the total number of vectors in the database.
     * 
     * @return number of vectors stored
     */
    public int size() {
        return vectorDatabaseUseCase.getVectorCount();
    }
    
    // Similarity operations
    
    /**
     * Finds vectors similar to the query vector.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results
     * @return list of similar vectors ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null or limit is negative
     */
    public List<Vector> findSimilar(Vector queryVector, int limit) {
        return vectorSimilarityUseCase.findSimilarVectors(queryVector, limit);
    }
    
    /**
     * Finds vectors similar to the query vector with similarity scores.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results
     * @return list of similarity results ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null or limit is negative
     */
    public List<VectorSimilarityUseCase.SimilarityResult> findSimilarWithScores(Vector queryVector, int limit) {
        return vectorSimilarityUseCase.findSimilarVectorsWithScores(queryVector, limit);
    }
}