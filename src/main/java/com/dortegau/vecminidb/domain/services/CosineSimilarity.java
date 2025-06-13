package com.dortegau.vecminidb.domain.services;

import com.dortegau.vecminidb.domain.entities.Vector;

/**
 * Cosine similarity calculator implementation.
 * Calculates the cosine of the angle between two vectors.
 */
public class CosineSimilarity implements SimilarityCalculator {
    
    @Override
    public double calculate(Vector vector1, Vector vector2) {
        if (vector1 == null) {
            throw new IllegalArgumentException("First vector cannot be null");
        }
        if (vector2 == null) {
            throw new IllegalArgumentException("Second vector cannot be null");
        }
        
        double[] values1 = vector1.getValues();
        double[] values2 = vector2.getValues();
        
        if (values1.length != values2.length) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < values1.length; i++) {
            dotProduct += values1[i] * values2[i];
            normA += values1[i] * values1[i];
            normB += values2[i] * values2[i];
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}