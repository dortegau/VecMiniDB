package com.dortegau.vecminidb.infrastructure.adapters;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.infrastructure.adapters.out.WriteAheadLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WriteAheadLog.
 */
@DisplayName("WriteAheadLog Tests")
class WriteAheadLogTest {
    
    @TempDir
    Path tempDir;
    
    private WriteAheadLog wal;
    private Path dbFile;
    
    @BeforeEach
    void setUp() {
        dbFile = tempDir.resolve("test.vecdb");
        wal = new WriteAheadLog(dbFile);
    }
    
    @Test
    @DisplayName("Should create WAL with correct file path")
    void testWALCreation() {
        assertNotNull(wal);
        assertFalse(wal.hasEntries());
    }
    
    @Test
    @DisplayName("Should log single vector insert")
    void testLogSingleInsert() {
        Vector vector = Vector.of("test1", new double[]{1.0, 2.0, 3.0});
        
        wal.logInsert(vector);
        
        assertTrue(wal.hasEntries());
        
        List<Vector> recovered = wal.recover();
        assertEquals(1, recovered.size());
        assertEquals("test1", recovered.get(0).getIdValue());
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, recovered.get(0).getValues(), 1e-10);
    }
    
    @Test
    @DisplayName("Should log multiple vector inserts")
    void testLogMultipleInserts() {
        Vector vector1 = Vector.of("vec1", new double[]{1.0, 2.0});
        Vector vector2 = Vector.of("vec2", new double[]{3.0, 4.0});
        Vector vector3 = Vector.of("vec3", new double[]{5.0, 6.0});
        
        wal.logInsert(vector1);
        wal.logInsert(vector2);
        wal.logInsert(vector3);
        
        assertTrue(wal.hasEntries());
        
        List<Vector> recovered = wal.recover();
        assertEquals(3, recovered.size());
        
        // Verify order is preserved
        assertEquals("vec1", recovered.get(0).getIdValue());
        assertEquals("vec2", recovered.get(1).getIdValue());
        assertEquals("vec3", recovered.get(2).getIdValue());
    }
    
    @Test
    @DisplayName("Should clear WAL file")
    void testClearWAL() {
        Vector vector = Vector.of("test", new double[]{1.0, 2.0});
        wal.logInsert(vector);
        
        assertTrue(wal.hasEntries());
        
        wal.clear();
        
        assertFalse(wal.hasEntries());
        List<Vector> recovered = wal.recover();
        assertTrue(recovered.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle empty WAL recovery")
    void testEmptyWALRecovery() {
        List<Vector> recovered = wal.recover();
        assertTrue(recovered.isEmpty());
        assertFalse(wal.hasEntries());
    }
    
    @Test
    @DisplayName("Should handle vectors with special characters in ID")
    void testSpecialCharactersInId() {
        Vector vector = Vector.of("test_with_special_chars", new double[]{1.0});
        
        wal.logInsert(vector);
        
        List<Vector> recovered = wal.recover();
        assertEquals(1, recovered.size());
        assertEquals("test_with_special_chars", recovered.get(0).getIdValue());
    }
    
    @Test
    @DisplayName("Should handle large vectors")
    void testLargeVectors() {
        double[] largeArray = new double[1000];
        for (int i = 0; i < largeArray.length; i++) {
            largeArray[i] = Math.random();
        }
        
        Vector vector = Vector.of("large_vector", largeArray);
        wal.logInsert(vector);
        
        List<Vector> recovered = wal.recover();
        assertEquals(1, recovered.size());
        assertEquals("large_vector", recovered.get(0).getIdValue());
        assertArrayEquals(largeArray, recovered.get(0).getValues(), 1e-10);
    }
    
    @Test
    @DisplayName("Should handle vectors with negative and zero values")
    void testNegativeAndZeroValues() {
        Vector vector = Vector.of("mixed", new double[]{-1.5, 0.0, 1.5, -0.0});
        
        wal.logInsert(vector);
        
        List<Vector> recovered = wal.recover();
        assertEquals(1, recovered.size());
        assertArrayEquals(new double[]{-1.5, 0.0, 1.5, -0.0}, recovered.get(0).getValues(), 1e-10);
    }
    
    @Test
    @DisplayName("Should reject null vector")
    void testLogNullVector() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> wal.logInsert(null)
        );
        assertEquals("Vector cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject null database file path")
    void testNullDatabaseFile() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new WriteAheadLog(null)
        );
        assertEquals("Database file path cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should handle recovery after multiple clear operations")
    void testMultipleClearOperations() {
        Vector vector1 = Vector.of("v1", new double[]{1.0});
        Vector vector2 = Vector.of("v2", new double[]{2.0});
        
        // First batch
        wal.logInsert(vector1);
        wal.clear();
        
        // Second batch
        wal.logInsert(vector2);
        
        List<Vector> recovered = wal.recover();
        assertEquals(1, recovered.size());
        assertEquals("v2", recovered.get(0).getIdValue());
    }
}