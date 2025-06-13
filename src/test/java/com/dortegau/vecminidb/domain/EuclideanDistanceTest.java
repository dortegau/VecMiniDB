package com.dortegau.vecminidb.domain;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.EuclideanDistance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EuclideanDistance domain service.
 */
class EuclideanDistanceTest {
    
    private EuclideanDistance euclidean;
    
    @BeforeEach
    void setUp() {
        euclidean = new EuclideanDistance();
    }
    
    @Test
    void testCalculateWithIdenticalVectors() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0, 3.0});
        Vector v2 = Vector.of("v2", new double[]{1.0, 2.0, 3.0});
        
        assertEquals(1.0, euclidean.calculate(v1, v2), 1e-10);
    }
    
    @Test
    void testCalculateWithDifferentVectors() {
        Vector v1 = Vector.of("v1", new double[]{0.0, 0.0});
        Vector v2 = Vector.of("v2", new double[]{3.0, 4.0}); // Distance = 5
        
        double expected = 1.0 / (1.0 + 5.0); // 1/6
        assertEquals(expected, euclidean.calculate(v1, v2), 1e-10);
    }
    
    @Test
    void testCalculateWithNullFirstVector() {
        Vector v2 = Vector.of("v2", new double[]{1.0, 2.0, 3.0});
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> euclidean.calculate(null, v2)
        );
        assertEquals("First vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testCalculateWithNullSecondVector() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0, 3.0});
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> euclidean.calculate(v1, null)
        );
        assertEquals("Second vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testCalculateWithDifferentDimensions() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 2.0});
        Vector v2 = Vector.of("v2", new double[]{1.0, 2.0, 3.0});
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> euclidean.calculate(v1, v2)
        );
        assertEquals("Vectors must have same dimension", exception.getMessage());
    }
    
    @Test
    void testGetName() {
        assertEquals("EuclideanDistance", euclidean.getName());
    }
}