package com.dortegau.vecminidb.infrastructure.adapters;

import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VectorQueryBuilder adapter.
 */
class VectorQueryBuilderTest {
    
    private VectorSimilarityUseCase mockSimilarityUseCase;
    private VectorQueryBuilder queryBuilder;
    
    @BeforeEach
    void setUp() {
        mockSimilarityUseCase = mock(VectorSimilarityUseCase.class);
        queryBuilder = new VectorQueryBuilder(mockSimilarityUseCase);
    }
    
    @Test
    void testConstructorWithNullUseCase() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new VectorQueryBuilder(null)
        );
        assertEquals("Vector similarity use case cannot be null", exception.getMessage());
    }
    
    @Test
    void testBasicQuery() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        Vector result1 = Vector.of("v1", new double[]{0.9, 0.1, 0.0});
        Vector result2 = Vector.of("v2", new double[]{0.8, 0.2, 0.0});
        
        List<VectorSimilarityUseCase.SimilarityResult> mockResults = List.of(
            new VectorSimilarityUseCase.SimilarityResult(result1, 0.95),
            new VectorSimilarityUseCase.SimilarityResult(result2, 0.85)
        );
        
        when(mockSimilarityUseCase.findSimilarVectorsWithThreshold(queryVector, 10, 0.0))
            .thenReturn(mockResults);
        
        List<VectorSimilarityUseCase.SimilarityResult> results = queryBuilder
            .select()
            .similarTo(queryVector)
            .execute();
        
        assertEquals(2, results.size());
        assertEquals("v1", results.get(0).vector().getIdValue());
        assertEquals("v2", results.get(1).vector().getIdValue());
        
        verify(mockSimilarityUseCase).findSimilarVectorsWithThreshold(queryVector, 10, 0.0);
    }
    
    @Test
    void testQueryWithLimit() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        when(mockSimilarityUseCase.findSimilarVectorsWithThreshold(queryVector, 5, 0.0))
            .thenReturn(List.of());
        
        queryBuilder
            .select()
            .similarTo(queryVector)
            .limit(5)
            .execute();
        
        verify(mockSimilarityUseCase).findSimilarVectorsWithThreshold(queryVector, 5, 0.0);
    }
    
    @Test
    void testQueryWithMinSimilarity() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        when(mockSimilarityUseCase.findSimilarVectorsWithThreshold(queryVector, 10, 0.8))
            .thenReturn(List.of());
        
        queryBuilder
            .select()
            .similarTo(queryVector)
            .minSimilarity(0.8)
            .execute();
        
        verify(mockSimilarityUseCase).findSimilarVectorsWithThreshold(queryVector, 10, 0.8);
    }
    
    @Test
    void testQueryExecuteVectors() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        Vector result1 = Vector.of("v1", new double[]{0.9, 0.1, 0.0});
        
        List<VectorSimilarityUseCase.SimilarityResult> mockResults = List.of(
            new VectorSimilarityUseCase.SimilarityResult(result1, 0.95)
        );
        
        when(mockSimilarityUseCase.findSimilarVectorsWithThreshold(queryVector, 10, 0.0))
            .thenReturn(mockResults);
        
        List<Vector> results = queryBuilder
            .select()
            .similarTo(queryVector)
            .executeVectors();
        
        assertEquals(1, results.size());
        assertEquals("v1", results.get(0).getIdValue());
        
        verify(mockSimilarityUseCase).findSimilarVectorsWithThreshold(queryVector, 10, 0.0);
    }
    
    @Test
    void testQueryWithoutVector() {
        assertThrows(IllegalStateException.class, () -> 
            queryBuilder.select().execute()
        );
        
        assertThrows(IllegalStateException.class, () -> 
            queryBuilder.select().executeVectors()
        );
    }
    
    @Test
    void testQueryWithNullVector() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queryBuilder.select().similarTo(null)
        );
        assertEquals("Query vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testQueryWithNegativeLimit() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queryBuilder.select().limit(-1)
        );
        assertEquals("Limit cannot be negative", exception.getMessage());
    }
    
    @Test
    void testQueryWithInvalidMinSimilarity() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queryBuilder.select().minSimilarity(1.5)
        );
        assertEquals("Minimum similarity must be between 0.0 and 1.0", exception.getMessage());
    }
}