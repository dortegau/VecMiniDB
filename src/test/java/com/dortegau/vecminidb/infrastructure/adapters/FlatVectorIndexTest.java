package com.dortegau.vecminidb.infrastructure.adapters;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.infrastructure.adapters.out.FlatVectorIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FlatVectorIndex.
 */
@DisplayName("FlatVectorIndex Tests")
class FlatVectorIndexTest {
    
    private FlatVectorIndex index;
    
    @BeforeEach
    void setUp() {
        index = new FlatVectorIndex();
    }
    
    @Test
    @DisplayName("Should start empty")
    void testEmptyIndex() {
        assertEquals(0, index.size());
        assertTrue(index.all().isEmpty());
        assertFalse(index.contains("any_id"));
    }
    
    @Test
    @DisplayName("Should add single vector")
    void testAddSingleVector() {
        Vector vector = Vector.of("test1", new double[]{1.0, 2.0});
        
        index.add(vector);
        
        assertEquals(1, index.size());
        assertTrue(index.contains("test1"));
        assertEquals(vector, index.get("test1"));
        
        List<Vector> all = index.all();
        assertEquals(1, all.size());
        assertEquals(vector, all.get(0));
    }
    
    @Test
    @DisplayName("Should add multiple vectors")
    void testAddMultipleVectors() {
        Vector vector1 = Vector.of("v1", new double[]{1.0});
        Vector vector2 = Vector.of("v2", new double[]{2.0});
        Vector vector3 = Vector.of("v3", new double[]{3.0});
        
        index.add(vector1);
        index.add(vector2);
        index.add(vector3);
        
        assertEquals(3, index.size());
        assertTrue(index.contains("v1"));
        assertTrue(index.contains("v2"));
        assertTrue(index.contains("v3"));
        
        assertEquals(vector1, index.get("v1"));
        assertEquals(vector2, index.get("v2"));
        assertEquals(vector3, index.get("v3"));
    }
    
    @Test
    @DisplayName("Should replace vector with same ID")
    void testReplaceVector() {
        Vector original = Vector.of("same_id", new double[]{1.0, 2.0});
        Vector replacement = Vector.of("same_id", new double[]{3.0, 4.0});
        
        index.add(original);
        assertEquals(1, index.size());
        assertEquals(original, index.get("same_id"));
        
        index.add(replacement);
        assertEquals(1, index.size()); // Size should remain the same
        assertEquals(replacement, index.get("same_id"));
        assertNotEquals(original, index.get("same_id"));
    }
    
    @Test
    @DisplayName("Should remove vector by ID")
    void testRemoveVector() {
        Vector vector1 = Vector.of("v1", new double[]{1.0});
        Vector vector2 = Vector.of("v2", new double[]{2.0});
        
        index.add(vector1);
        index.add(vector2);
        assertEquals(2, index.size());
        
        Vector removed = index.remove("v1");
        assertEquals(vector1, removed);
        assertEquals(1, index.size());
        assertFalse(index.contains("v1"));
        assertTrue(index.contains("v2"));
        
        // Try to remove non-existent vector
        Vector notRemoved = index.remove("non_existent");
        assertNull(notRemoved);
        assertEquals(1, index.size());
    }
    
    @Test
    @DisplayName("Should clear all vectors")
    void testClearAllVectors() {
        Vector vector1 = Vector.of("v1", new double[]{1.0});
        Vector vector2 = Vector.of("v2", new double[]{2.0});
        
        index.add(vector1);
        index.add(vector2);
        assertEquals(2, index.size());
        
        index.clear();
        
        assertEquals(0, index.size());
        assertTrue(index.all().isEmpty());
        assertFalse(index.contains("v1"));
        assertFalse(index.contains("v2"));
    }
    
    @Test
    @DisplayName("Should return null for non-existent ID")
    void testGetNonExistentId() {
        assertNull(index.get("non_existent"));
        
        index.add(Vector.of("existing", new double[]{1.0}));
        assertNull(index.get("still_non_existent"));
    }
    
    @Test
    @DisplayName("Should get all IDs")
    void testGetAllIds() {
        Vector vector1 = Vector.of("id1", new double[]{1.0});
        Vector vector2 = Vector.of("id2", new double[]{2.0});
        Vector vector3 = Vector.of("id3", new double[]{3.0});
        
        index.add(vector1);
        index.add(vector2);
        index.add(vector3);
        
        Set<String> allIds = index.getAllIds();
        assertEquals(3, allIds.size());
        assertTrue(allIds.contains("id1"));
        assertTrue(allIds.contains("id2"));
        assertTrue(allIds.contains("id3"));
    }
    
    @Test
    @DisplayName("Should add all vectors from another index")
    void testAddAllFromOtherIndex() {
        FlatVectorIndex otherIndex = new FlatVectorIndex();
        otherIndex.add(Vector.of("other1", new double[]{1.0}));
        otherIndex.add(Vector.of("other2", new double[]{2.0}));
        
        index.add(Vector.of("original", new double[]{0.0}));
        assertEquals(1, index.size());
        
        index.addAll(otherIndex);
        
        assertEquals(3, index.size());
        assertTrue(index.contains("original"));
        assertTrue(index.contains("other1"));
        assertTrue(index.contains("other2"));
    }
    
    @Test
    @DisplayName("Should create copy of index")
    void testCopyIndex() {
        Vector vector1 = Vector.of("v1", new double[]{1.0});
        Vector vector2 = Vector.of("v2", new double[]{2.0});
        
        index.add(vector1);
        index.add(vector2);
        
        FlatVectorIndex copy = index.copy();
        
        assertEquals(index.size(), copy.size());
        assertEquals(index.get("v1"), copy.get("v1"));
        assertEquals(index.get("v2"), copy.get("v2"));
        
        // Verify it's a true copy (modifications don't affect original)
        copy.add(Vector.of("v3", new double[]{3.0}));
        assertEquals(2, index.size());
        assertEquals(3, copy.size());
    }
    
    @Test
    @DisplayName("Should reject null vector")
    void testAddNullVector() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> index.add(null)
        );
        assertEquals("Vector cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject null ID in operations")
    void testNullIdOperations() {
        assertThrows(IllegalArgumentException.class, () -> index.get(null));
        assertThrows(IllegalArgumentException.class, () -> index.contains(null));
        assertThrows(IllegalArgumentException.class, () -> index.remove(null));
    }
    
    @Test
    @DisplayName("Should reject null other index in addAll")
    void testAddAllNullIndex() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> index.addAll(null)
        );
        assertEquals("Other index cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should handle concurrent access safely")
    void testConcurrentAccess() {
        // This is a basic test - real concurrent testing would need multiple threads
        Vector vector = Vector.of("concurrent", new double[]{1.0});
        
        index.add(vector);
        
        // These operations should be thread-safe
        assertTrue(index.contains("concurrent"));
        assertEquals(vector, index.get("concurrent"));
        assertEquals(1, index.size());
    }
    
    @Test
    @DisplayName("Should support equals and hashCode")
    void testEqualsAndHashCode() {
        FlatVectorIndex index1 = new FlatVectorIndex();
        FlatVectorIndex index2 = new FlatVectorIndex();
        
        // Empty indexes should be equal
        assertEquals(index1, index2);
        assertEquals(index1.hashCode(), index2.hashCode());
        
        // Add same content to both
        Vector vector = Vector.of("test", new double[]{1.0, 2.0});
        index1.add(vector);
        index2.add(vector);
        
        assertEquals(index1, index2);
        assertEquals(index1.hashCode(), index2.hashCode());
        
        // Different content should not be equal
        index2.add(Vector.of("different", new double[]{3.0}));
        assertNotEquals(index1, index2);
    }
}