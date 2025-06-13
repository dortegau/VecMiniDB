package com.dortegau.vecminidb.domain;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CosineSimilarity domain service.
 */
class CosineSimilarityTest {
    
    private CosineSimilarity similarity;
    
    @BeforeEach
    void setUp() {
        similarity = new CosineSimilarity();
    }
    
    @Test
    void testCalculateWithOrthogonalVectors() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 0.0, 0.0});
        Vector v2 = Vector.of("v2", new double[]{0.0, 1.0, 0.0});
        
        assertEquals(0.0, similarity.calculate(v1, v2), 1e-10);
    }
    
    @Test
    void testCalculateWithIdenticalVectors() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 0.0, 0.0});
        Vector v2 = Vector.of("v2", new double[]{1.0, 0.0, 0.0});
        
        assertEquals(1.0, similarity.calculate(v1, v2), 1e-10);
    }
    
    @Test
    void testCalculateWithSameVector() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0, 3.0});
        
        assertEquals(1.0, similarity.calculate(v1, v1), 1e-10);
    }
    
    @Test
    void testCalculateWithProportionalVectors() {
        Vector v1 = Vector.of("v1", new double[]{3.0, 4.0});
        Vector v2 = Vector.of("v2", new double[]{6.0, 8.0});
        
        assertEquals(1.0, similarity.calculate(v1, v2), 1e-10);
    }
    
    @Test
    void testCalculateWithZeroVector() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0, 3.0});
        Vector zeroVector = Vector.of("zero", new double[]{0.0, 0.0, 0.0});
        
        assertEquals(0.0, similarity.calculate(v1, zeroVector), 1e-10);
        assertEquals(0.0, similarity.calculate(zeroVector, v1), 1e-10);
    }
    
    @Test
    void testCalculateWithNullFirstVector() {
        Vector v2 = Vector.of("v2", new double[]{1.0, 2.0, 3.0});
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> similarity.calculate(null, v2)
        );
        assertEquals("First vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testCalculateWithNullSecondVector() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0, 3.0});
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> similarity.calculate(v1, null)
        );
        assertEquals("Second vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testCalculateWithDifferentDimensions() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0});
        Vector v2 = Vector.of("v2", new double[]{1.0, 2.0, 3.0});
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> similarity.calculate(v1, v2)
        );
        assertEquals("Vectors must have same dimension", exception.getMessage());
    }
    
    @Test
    void testGetName() {
        assertEquals("CosineSimilarity", similarity.getName());
    }
}