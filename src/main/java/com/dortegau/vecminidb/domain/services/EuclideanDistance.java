package com.dortegau.vecminidb.domain.services;

import com.dortegau.vecminidb.domain.entities.Vector;

/**
 * Euclidean distance similarity calculator implementation.
 * Converts distance to similarity using 1/(1+distance) formula.
 */
public class EuclideanDistance implements SimilarityCalculator {
    
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
        
        double sumSquaredDifferences = 0.0;
        
        for (int i = 0; i < values1.length; i++) {
            double diff = values1[i] - values2[i];
            sumSquaredDifferences += diff * diff;
        }
        
        // Return similarity (1 / (1 + distance)) instead of distance
        // This makes it consistent with other similarity measures (higher = more similar)
        double distance = Math.sqrt(sumSquaredDifferences);
        return 1.0 / (1.0 + distance);
    }
}