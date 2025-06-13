package com.dortegau.vecminidb.application.ports.in;

import com.dortegau.vecminidb.domain.entities.Vector;

import java.util.List;

/**
 * Input port for vector similarity operations.
 * Defines use cases for finding similar vectors.
 */
public interface VectorSimilarityUseCase {
    
    /**
     * Result of a similarity search containing vector and similarity score.
     */
    record SimilarityResult(Vector vector, double similarity) {
        public SimilarityResult {
            if (vector == null) {
                throw new IllegalArgumentException("Vector cannot be null");
            }
            // Note: Similarity can be negative (e.g., cosine similarity ranges from -1 to 1)
        }
        
        @Override
        public String toString() {
            return "SimilarityResult{vector=" + vector.getIdValue() + 
                   ", similarity=" + String.format("%.4f", similarity) + "}";
        }
    }
    
    /**
     * Finds vectors similar to the query vector.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results
     * @return list of similar vectors ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null or limit is negative
     */
    List<Vector> findSimilarVectors(Vector queryVector, int limit);
    
    /**
     * Finds vectors similar to the query vector with similarity scores.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results
     * @return list of similarity results ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null or limit is negative
     */
    List<SimilarityResult> findSimilarVectorsWithScores(Vector queryVector, int limit);
    
    /**
     * Finds vectors similar to the query vector with minimum similarity threshold.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results
     * @param minSimilarity minimum similarity threshold
     * @return list of similarity results ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null, limit is negative, or minSimilarity is invalid
     */
    List<SimilarityResult> findSimilarVectorsWithThreshold(Vector queryVector, int limit, double minSimilarity);
}