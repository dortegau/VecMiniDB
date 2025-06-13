package com.dortegau.vecminidb.domain.services;

import com.dortegau.vecminidb.domain.entities.Vector;

/**
 * Manhattan distance similarity calculator implementation.
 * Converts distance to similarity using 1/(1+distance) formula.
 */
public class ManhattanDistance implements SimilarityCalculator {
    
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
        
        double sumAbsoluteDifferences = 0.0;
        
        for (int i = 0; i < values1.length; i++) {
            sumAbsoluteDifferences += Math.abs(values1[i] - values2[i]);
        }
        
        // Return similarity (1 / (1 + distance)) instead of distance
        // This makes it consistent with other similarity measures (higher = more similar)
        return 1.0 / (1.0 + sumAbsoluteDifferences);
    }
}