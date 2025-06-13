package com.dortegau.vecminidb.domain;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorData;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Vector domain entity.
 */
class VectorTest {
    
    @Test
    void testVectorCreation() {
        double[] values = {1.0, 2.0, 3.0};
        Vector vector = Vector.of("test", values);
        
        assertEquals("test", vector.getIdValue());
        assertArrayEquals(values, vector.getValues());
        assertEquals(3, vector.getDimension());
    }
    
    @Test
    void testVectorCreationWithNullId() {
        double[] values = {1.0, 2.0, 3.0};
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Vector.of(null, values)
        );
        assertEquals("Vector ID cannot be null", exception.getMessage());
    }
    
    @Test
    void testVectorCreationWithNullValues() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Vector.of("test", null)
        );
        assertEquals("Vector values cannot be null", exception.getMessage());
    }
    
    @Test
    void testVectorCreationWithEmptyId() {
        double[] values = {1.0, 2.0, 3.0};
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Vector.of("", values)
        );
        assertEquals("Vector ID cannot be empty", exception.getMessage());
    }
    
    @Test
    void testVectorCreationWithEmptyValues() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Vector.of("test", new double[0])
        );
        assertEquals("Vector values cannot be empty", exception.getMessage());
    }
    
    @Test
    void testVectorImmutability() {
        double[] original = {1.0, 2.0, 3.0};
        Vector vector = Vector.of("test", original);
        
        // Modify original array
        original[0] = 999.0;
        assertEquals(1.0, vector.getValues()[0]);
        
        // Modify returned array
        double[] retrieved = vector.getValues();
        retrieved[0] = 888.0;
        assertEquals(1.0, vector.getValues()[0]);
    }
    
    @Test
    void testVectorEquality() {
        Vector v1 = Vector.of("test", new double[]{1.0, 2.0, 3.0});
        Vector v2 = Vector.of("test", new double[]{1.0, 2.0, 3.0});
        Vector v3 = Vector.of("different", new double[]{1.0, 2.0, 3.0});
        Vector v4 = Vector.of("test", new double[]{1.0, 2.0, 4.0});
        
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
        assertNotEquals(v1, v4);
        assertNotEquals(v1, null);
        assertNotEquals(v1, "not a vector");
    }
    
    @Test
    void testVectorHashCode() {
        Vector v1 = Vector.of("test", new double[]{1.0, 2.0, 3.0});
        Vector v2 = Vector.of("test", new double[]{1.0, 2.0, 3.0});
        
        assertEquals(v1.hashCode(), v2.hashCode());
    }
}