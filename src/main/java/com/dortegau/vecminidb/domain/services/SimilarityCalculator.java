package com.dortegau.vecminidb.domain.services;

import com.dortegau.vecminidb.domain.entities.Vector;

/**
 * Domain service for calculating similarity between vectors.
 * This is a functional interface allowing different similarity algorithms.
 */
@FunctionalInterface
public interface SimilarityCalculator {
    
    /**
     * Calculates similarity between two vectors.
     * 
     * @param vector1 the first vector
     * @param vector2 the second vector
     * @return similarity score (higher values indicate more similarity)
     * @throws IllegalArgumentException if vectors are null or have different dimensions
     */
    double calculate(Vector vector1, Vector vector2);
    
    /**
     * Gets the name of this similarity calculator.
     * 
     * @return the calculator name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}