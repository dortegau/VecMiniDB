package com.dortegau.vecminidb.infrastructure.adapters.in;

import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.domain.entities.Vector;

import java.util.List;

/**
 * Query builder adapter providing a fluent interface for vector similarity searches.
 * This is an input adapter that provides a convenient API over the use cases.
 */
public class VectorQueryBuilder {
    
    private final VectorSimilarityUseCase vectorSimilarityUseCase;
    
    /**
     * Creates a new vector query builder.
     * 
     * @param vectorSimilarityUseCase the similarity use case implementation
     * @throws IllegalArgumentException if vectorSimilarityUseCase is null
     */
    public VectorQueryBuilder(VectorSimilarityUseCase vectorSimilarityUseCase) {
        if (vectorSimilarityUseCase == null) {
            throw new IllegalArgumentException("Vector similarity use case cannot be null");
        }
        this.vectorSimilarityUseCase = vectorSimilarityUseCase;
    }
    
    /**
     * Starts a new query builder.
     * 
     * @return a new query builder instance
     */
    public QueryBuilder select() {
        return new QueryBuilder(vectorSimilarityUseCase);
    }
    
    /**
     * Fluent query builder for vector similarity searches.
     */
    public static class QueryBuilder {
        private final VectorSimilarityUseCase vectorSimilarityUseCase;
        private Vector queryVector;
        private int limit = 10;
        private double minSimilarity = 0.0;
        
        public QueryBuilder(VectorSimilarityUseCase vectorSimilarityUseCase) {
            this.vectorSimilarityUseCase = vectorSimilarityUseCase;
        }
        
        /**
         * Sets the query vector for similarity search.
         * 
         * @param vector the vector to find similarities for
         * @return this builder for method chaining
         * @throws IllegalArgumentException if vector is null
         */
        public QueryBuilder similarTo(Vector vector) {
            if (vector == null) {
                throw new IllegalArgumentException("Query vector cannot be null");
            }
            this.queryVector = vector;
            return this;
        }
        
        /**
         * Sets the maximum number of results to return.
         * 
         * @param limit the maximum number of results
         * @return this builder for method chaining
         * @throws IllegalArgumentException if limit is negative
         */
        public QueryBuilder limit(int limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("Limit cannot be negative");
            }
            this.limit = limit;
            return this;
        }
        
        /**
         * Sets the minimum similarity threshold.
         * 
         * @param threshold the minimum similarity threshold
         * @return this builder for method chaining
         * @throws IllegalArgumentException if threshold is invalid
         */
        public QueryBuilder minSimilarity(double threshold) {
            if (threshold < 0.0 || threshold > 1.0) {
                throw new IllegalArgumentException("Minimum similarity must be between 0.0 and 1.0");
            }
            this.minSimilarity = threshold;
            return this;
        }
        
        /**
         * Executes the query and returns similarity results.
         * 
         * @return list of similarity results
         * @throws IllegalStateException if query vector is not set
         */
        public List<VectorSimilarityUseCase.SimilarityResult> execute() {
            if (queryVector == null) {
                throw new IllegalStateException("Query vector must be specified using similarTo()");
            }
            
            return vectorSimilarityUseCase.findSimilarVectorsWithThreshold(queryVector, limit, minSimilarity);
        }
        
        /**
         * Executes the query and returns only the vectors (without similarity scores).
         * 
         * @return list of similar vectors
         * @throws IllegalStateException if query vector is not set
         */
        public List<Vector> executeVectors() {
            if (queryVector == null) {
                throw new IllegalStateException("Query vector must be specified using similarTo()");
            }
            
            return execute().stream()
                    .map(VectorSimilarityUseCase.SimilarityResult::vector)
                    .toList();
        }
    }
}