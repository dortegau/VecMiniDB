package com.dortegau.vecminidb.application;

import com.dortegau.vecminidb.application.ports.out.VectorRepository;
import com.dortegau.vecminidb.application.usecases.VectorDatabaseService;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VectorDatabaseService.
 */
class VectorDatabaseServiceTest {
    
    private VectorRepository mockRepository;
    private VectorDatabaseService service;
    
    @BeforeEach
    void setUp() {
        mockRepository = mock(VectorRepository.class);
        service = new VectorDatabaseService(mockRepository);
    }
    
    @Test
    void testConstructorWithNullRepository() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new VectorDatabaseService(null)
        );
        assertEquals("Vector repository cannot be null", exception.getMessage());
    }
    
    @Test
    void testStoreVector() {
        Vector vector = Vector.of("test", new double[]{1.0, 2.0, 3.0});
        
        service.storeVector(vector);
        
        verify(mockRepository).save(vector);
    }
    
    @Test
    void testStoreVectorWithNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.storeVector(null)
        );
        assertEquals("Vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testGetVectorExists() {
        VectorId vectorId = VectorId.of("test");
        Vector vector = Vector.of("test", new double[]{1.0, 2.0, 3.0});
        when(mockRepository.findById(vectorId)).thenReturn(Optional.of(vector));
        
        Optional<Vector> result = service.getVector(vectorId);
        
        assertTrue(result.isPresent());
        assertEquals(vector, result.get());
        verify(mockRepository).findById(vectorId);
    }
    
    @Test
    void testGetVectorNotExists() {
        VectorId vectorId = VectorId.of("test");
        when(mockRepository.findById(vectorId)).thenReturn(Optional.empty());
        
        Optional<Vector> result = service.getVector(vectorId);
        
        assertFalse(result.isPresent());
        verify(mockRepository).findById(vectorId);
    }
    
    @Test
    void testGetVectorWithNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getVector(null)
        );
        assertEquals("Vector ID cannot be null", exception.getMessage());
    }
    
    @Test
    void testDeleteVectorExists() {
        VectorId vectorId = VectorId.of("test");
        when(mockRepository.deleteById(vectorId)).thenReturn(true);
        
        boolean result = service.deleteVector(vectorId);
        
        assertTrue(result);
        verify(mockRepository).deleteById(vectorId);
    }
    
    @Test
    void testDeleteVectorNotExists() {
        VectorId vectorId = VectorId.of("test");
        when(mockRepository.deleteById(vectorId)).thenReturn(false);
        
        boolean result = service.deleteVector(vectorId);
        
        assertFalse(result);
        verify(mockRepository).deleteById(vectorId);
    }
    
    @Test
    void testDeleteVectorWithNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.deleteVector(null)
        );
        assertEquals("Vector ID cannot be null", exception.getMessage());
    }
    
    @Test
    void testGetAllVectorIds() {
        Set<VectorId> vectorIds = Set.of(VectorId.of("v1"), VectorId.of("v2"));
        when(mockRepository.findAllIds()).thenReturn(vectorIds);
        
        Set<VectorId> result = service.getAllVectorIds();
        
        assertEquals(vectorIds, result);
        verify(mockRepository).findAllIds();
    }
    
    @Test
    void testGetVectorCount() {
        when(mockRepository.count()).thenReturn(5);
        
        int result = service.getVectorCount();
        
        assertEquals(5, result);
        verify(mockRepository).count();
    }
}