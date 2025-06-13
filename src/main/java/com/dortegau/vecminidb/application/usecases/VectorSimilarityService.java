package com.dortegau.vecminidb.application.usecases;

import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.application.ports.out.VectorRepository;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.SimilarityCalculator;

import java.util.List;
import java.util.Set;

/**
 * Implementation of vector similarity use cases.
 * Orchestrates similarity calculations and repository operations.
 */
public class VectorSimilarityService implements VectorSimilarityUseCase {
    
    private final VectorRepository vectorRepository;
    private final SimilarityCalculator similarityCalculator;
    
    public VectorSimilarityService(VectorRepository vectorRepository, SimilarityCalculator similarityCalculator) {
        if (vectorRepository == null) {
            throw new IllegalArgumentException("Vector repository cannot be null");
        }
        if (similarityCalculator == null) {
            throw new IllegalArgumentException("Similarity calculator cannot be null");
        }
        
        this.vectorRepository = vectorRepository;
        this.similarityCalculator = similarityCalculator;
    }
    
    @Override
    public List<Vector> findSimilarVectors(Vector queryVector, int limit) {
        if (queryVector == null) {
            throw new IllegalArgumentException("Query vector cannot be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        
        return findSimilarVectorsWithScores(queryVector, limit)
                .stream()
                .map(SimilarityResult::vector)
                .toList();
    }
    
    @Override
    public List<SimilarityResult> findSimilarVectorsWithScores(Vector queryVector, int limit) {
        if (queryVector == null) {
            throw new IllegalArgumentException("Query vector cannot be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        
        Set<Vector> allVectors = vectorRepository.findAll();
        
        return allVectors.stream()
                .map(vector -> new SimilarityResult(vector, similarityCalculator.calculate(queryVector, vector)))
                .sorted((a, b) -> Double.compare(b.similarity(), a.similarity()))
                .limit(limit)
                .toList();
    }
    
    @Override
    public List<SimilarityResult> findSimilarVectorsWithThreshold(Vector queryVector, int limit, double minSimilarity) {
        if (queryVector == null) {
            throw new IllegalArgumentException("Query vector cannot be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        if (minSimilarity < 0.0 || minSimilarity > 1.0) {
            throw new IllegalArgumentException("Minimum similarity must be between 0.0 and 1.0");
        }
        
        return findSimilarVectorsWithScores(queryVector, Integer.MAX_VALUE)
                .stream()
                .filter(result -> result.similarity() >= minSimilarity)
                .limit(limit)
                .toList();
    }
}