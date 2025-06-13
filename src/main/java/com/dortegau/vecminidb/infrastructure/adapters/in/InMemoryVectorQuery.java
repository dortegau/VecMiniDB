package com.dortegau.vecminidb.infrastructure.adapters.in;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.SimilarityCalculator;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * In-memory vector query engine.
 * Operates exclusively on vectors already loaded in memory, never reads from disk.
 * Provides filtering, similarity calculation, and result ranking.
 */
public class InMemoryVectorQuery {
    
    private final List<Vector> vectorIndex;
    private final SimilarityCalculator similarityCalculator;
    
    /**
     * Creates a new in-memory query engine.
     * 
     * @param vectorIndex the in-memory vector collection to query
     * @param similarityCalculator the similarity algorithm to use
     * @throws IllegalArgumentException if vectorIndex or similarityCalculator is null
     */
    public InMemoryVectorQuery(List<Vector> vectorIndex, SimilarityCalculator similarityCalculator) {
        if (vectorIndex == null) {
            throw new IllegalArgumentException("Vector index cannot be null");
        }
        if (similarityCalculator == null) {
            throw new IllegalArgumentException("Similarity calculator cannot be null");
        }
        
        this.vectorIndex = new ArrayList<>(vectorIndex); // Defensive copy
        this.similarityCalculator = similarityCalculator;
    }
    
    /**
     * Creates a new in-memory query engine with default cosine similarity.
     * 
     * @param vectorIndex the in-memory vector collection to query
     * @throws IllegalArgumentException if vectorIndex is null
     */
    public InMemoryVectorQuery(List<Vector> vectorIndex) {
        this(vectorIndex, new CosineSimilarity());
    }
    
    /**
     * Finds the most similar vectors to the query vector.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results to return
     * @return list of similarity results ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null or limit is negative
     */
    public List<SimilarityResult> findSimilar(Vector queryVector, int limit) {
        if (queryVector == null) {
            throw new IllegalArgumentException("Query vector cannot be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        
        List<SimilarityResult> results = new ArrayList<>();
        
        // Calculate similarity for each vector in the index
        for (Vector candidate : vectorIndex) {
            try {
                double similarity = similarityCalculator.calculate(queryVector, candidate);
                results.add(new SimilarityResult(candidate, similarity));
            } catch (Exception e) {
                // Skip vectors that can't be compared (e.g., dimension mismatch)
                continue;
            }
        }
        
        // Sort by similarity (descending) and apply limit
        results.sort(Comparator.comparingDouble(SimilarityResult::similarity).reversed());
        
        return limit == 0 ? results : results.subList(0, Math.min(limit, results.size()));
    }
    
    /**
     * Finds similar vectors with a minimum similarity threshold.
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results to return
     * @param minSimilarity minimum similarity threshold (inclusive)
     * @return list of similarity results ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null, limit is negative, or minSimilarity is invalid
     */
    public List<SimilarityResult> findSimilarWithThreshold(Vector queryVector, int limit, double minSimilarity) {
        if (queryVector == null) {
            throw new IllegalArgumentException("Query vector cannot be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        
        List<SimilarityResult> allResults = findSimilar(queryVector, 0); // Get all results
        
        // Filter by minimum similarity
        List<SimilarityResult> filteredResults = allResults.stream()
                .filter(result -> result.similarity() >= minSimilarity)
                .toList();
        
        return limit == 0 ? filteredResults : filteredResults.subList(0, Math.min(limit, filteredResults.size()));
    }
    
    /**
     * Finds similar vectors and returns only the vectors (without similarity scores).
     * 
     * @param queryVector the vector to find similarities for
     * @param limit maximum number of results to return
     * @return list of vectors ordered by similarity (highest first)
     * @throws IllegalArgumentException if queryVector is null or limit is negative
     */
    public List<Vector> findSimilarVectors(Vector queryVector, int limit) {
        return findSimilar(queryVector, limit).stream()
                .map(SimilarityResult::vector)
                .toList();
    }
    
    /**
     * Gets statistics about the query performance.
     * 
     * @return query statistics
     */
    public QueryStats getStats() {
        return new QueryStats(vectorIndex.size(), similarityCalculator.getName());
    }
    
    /**
     * Result of a similarity search containing the vector and its similarity score.
     */
    public record SimilarityResult(Vector vector, double similarity) {
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
     * Statistics about the query engine.
     */
    public record QueryStats(int indexSize, String similarityAlgorithm) {
        @Override
        public String toString() {
            return "QueryStats{indexSize=" + indexSize + 
                   ", algorithm='" + similarityAlgorithm + "'}";
        }
    }
    
    /**
     * Builder for creating InMemoryVectorQuery with fluent API.
     */
    public static class Builder {
        private List<Vector> vectorIndex;
        private SimilarityCalculator similarityCalculator = new CosineSimilarity();
        
        public Builder withVectors(List<Vector> vectors) {
            this.vectorIndex = vectors;
            return this;
        }
        
        public Builder withSimilarityCalculator(SimilarityCalculator calculator) {
            this.similarityCalculator = calculator;
            return this;
        }
        
        public InMemoryVectorQuery build() {
            if (vectorIndex == null) {
                throw new IllegalStateException("Vector index must be set");
            }
            return new InMemoryVectorQuery(vectorIndex, similarityCalculator);
        }
    }
}