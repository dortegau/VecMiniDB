package com.dortegau.vecminidb.infrastructure.adapters;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import com.dortegau.vecminidb.domain.services.EuclideanDistance;
import com.dortegau.vecminidb.infrastructure.adapters.in.InMemoryVectorQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryVectorQuery.
 */
@DisplayName("InMemoryVectorQuery Tests")
class InMemoryVectorQueryTest {
    
    private List<Vector> testVectors;
    private InMemoryVectorQuery query;
    
    @BeforeEach
    void setUp() {
        testVectors = new ArrayList<>();
        testVectors.add(Vector.of("v1", new double[]{1.0, 0.0, 0.0}));  // Unit vector in x direction
        testVectors.add(Vector.of("v2", new double[]{0.0, 1.0, 0.0}));  // Unit vector in y direction
        testVectors.add(Vector.of("v3", new double[]{0.0, 0.0, 1.0}));  // Unit vector in z direction
        testVectors.add(Vector.of("v4", new double[]{1.0, 1.0, 0.0}));  // 45 degrees from x in xy plane
        testVectors.add(Vector.of("v5", new double[]{2.0, 0.0, 0.0}));  // Parallel to v1 but different magnitude
        
        query = new InMemoryVectorQuery(testVectors, new CosineSimilarity());
    }
    
    @Test
    @DisplayName("Should create query with default cosine similarity")
    void testCreateWithDefaultSimilarity() {
        InMemoryVectorQuery defaultQuery = new InMemoryVectorQuery(testVectors);
        assertNotNull(defaultQuery);
        
        InMemoryVectorQuery.QueryStats stats = defaultQuery.getStats();
        assertEquals(5, stats.indexSize());
        assertEquals("CosineSimilarity", stats.similarityAlgorithm());
    }
    
    @Test
    @DisplayName("Should find most similar vectors")
    void testFindSimilar() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0}); // Same as v1
        
        List<InMemoryVectorQuery.SimilarityResult> results = query.findSimilar(queryVector, 3);
        
        assertEquals(3, results.size());
        
        // v1 and v5 should be most similar (parallel vectors)
        assertEquals("v1", results.get(0).vector().getIdValue());
        assertEquals(1.0, results.get(0).similarity(), 1e-10); // Perfect match
        
        assertEquals("v5", results.get(1).vector().getIdValue());
        assertEquals(1.0, results.get(1).similarity(), 1e-10); // Parallel vectors have cosine = 1
        
        // Results should be ordered by similarity (descending)
        assertTrue(results.get(0).similarity() >= results.get(1).similarity());
        assertTrue(results.get(1).similarity() >= results.get(2).similarity());
    }
    
    @Test
    @DisplayName("Should find similar vectors with threshold")
    void testFindSimilarWithThreshold() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        List<InMemoryVectorQuery.SimilarityResult> results = 
            query.findSimilarWithThreshold(queryVector, 10, 0.5);
        
        // Should only return vectors with similarity >= 0.5
        assertTrue(results.size() <= 5);
        for (InMemoryVectorQuery.SimilarityResult result : results) {
            assertTrue(result.similarity() >= 0.5, 
                      "Similarity " + result.similarity() + " should be >= 0.5");
        }
    }
    
    @Test
    @DisplayName("Should return only vectors without similarity scores")
    void testFindSimilarVectors() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        List<Vector> results = query.findSimilarVectors(queryVector, 2);
        
        assertEquals(2, results.size());
        assertEquals("v1", results.get(0).getIdValue());
        assertEquals("v5", results.get(1).getIdValue());
    }
    
    @Test
    @DisplayName("Should handle empty vector list")
    void testEmptyVectorList() {
        InMemoryVectorQuery emptyQuery = new InMemoryVectorQuery(new ArrayList<>());
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0});
        
        List<InMemoryVectorQuery.SimilarityResult> results = emptyQuery.findSimilar(queryVector, 5);
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle limit larger than available vectors")
    void testLimitLargerThanAvailable() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        List<InMemoryVectorQuery.SimilarityResult> results = query.findSimilar(queryVector, 100);
        
        assertEquals(5, results.size()); // Should return all available vectors
    }
    
    @Test
    @DisplayName("Should handle zero limit")
    void testZeroLimit() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        List<InMemoryVectorQuery.SimilarityResult> results = query.findSimilar(queryVector, 0);
        
        assertEquals(5, results.size()); // Zero limit should return all results
    }
    
    @Test
    @DisplayName("Should work with different similarity algorithms")
    void testDifferentSimilarityAlgorithms() {
        InMemoryVectorQuery euclideanQuery = new InMemoryVectorQuery(testVectors, new EuclideanDistance());
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        List<InMemoryVectorQuery.SimilarityResult> results = euclideanQuery.findSimilar(queryVector, 3);
        
        assertEquals(3, results.size());
        
        // With Euclidean distance, closer vectors should have higher similarity
        // (EuclideanDistance converts distance to similarity)
        assertTrue(results.get(0).similarity() >= results.get(1).similarity());
    }
    
    @Test
    @DisplayName("Should handle dimension mismatch gracefully")
    void testDimensionMismatch() {
        // Add a vector with different dimensions
        List<Vector> mixedVectors = new ArrayList<>(testVectors);
        mixedVectors.add(Vector.of("different_dim", new double[]{1.0, 2.0})); // 2D instead of 3D
        
        InMemoryVectorQuery mixedQuery = new InMemoryVectorQuery(mixedVectors);
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0}); // 3D
        
        List<InMemoryVectorQuery.SimilarityResult> results = mixedQuery.findSimilar(queryVector, 10);
        
        // Should skip the incompatible vector and return only the 3D ones
        assertEquals(5, results.size());
        for (InMemoryVectorQuery.SimilarityResult result : results) {
            assertEquals(3, result.vector().getValues().length);
        }
    }
    
    @Test
    @DisplayName("Should use builder pattern correctly")
    void testBuilderPattern() {
        InMemoryVectorQuery builtQuery = new InMemoryVectorQuery.Builder()
            .withVectors(testVectors)
            .withSimilarityCalculator(new EuclideanDistance())
            .build();
        
        assertNotNull(builtQuery);
        
        InMemoryVectorQuery.QueryStats stats = builtQuery.getStats();
        assertEquals(5, stats.indexSize());
        assertEquals("EuclideanDistance", stats.similarityAlgorithm());
    }
    
    @Test
    @DisplayName("Should get correct query statistics")
    void testQueryStats() {
        InMemoryVectorQuery.QueryStats stats = query.getStats();
        
        assertEquals(5, stats.indexSize());
        assertEquals("CosineSimilarity", stats.similarityAlgorithm());
        
        String statsString = stats.toString();
        assertTrue(statsString.contains("indexSize=5"));
        assertTrue(statsString.contains("algorithm='CosineSimilarity'"));
    }
    
    @Test
    @DisplayName("Should format similarity results correctly")
    void testSimilarityResultFormatting() {
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        
        List<InMemoryVectorQuery.SimilarityResult> results = query.findSimilar(queryVector, 1);
        
        InMemoryVectorQuery.SimilarityResult result = results.get(0);
        String resultString = result.toString();
        
        assertTrue(resultString.contains("vector=v1"));
        assertTrue(resultString.contains("similarity="));
    }
    
    @Test
    @DisplayName("Should reject null parameters")
    void testNullParameterValidation() {
        // Null vector list
        assertThrows(IllegalArgumentException.class, 
            () -> new InMemoryVectorQuery(null));
        
        // Null similarity calculator
        assertThrows(IllegalArgumentException.class, 
            () -> new InMemoryVectorQuery(testVectors, null));
        
        // Null query vector
        assertThrows(IllegalArgumentException.class, 
            () -> query.findSimilar(null, 5));
        
        // Negative limit
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.0, 0.0});
        assertThrows(IllegalArgumentException.class, 
            () -> query.findSimilar(queryVector, -1));
    }
    
    @Test
    @DisplayName("Should reject null vector in similarity result")
    void testSimilarityResultValidation() {
        assertThrows(IllegalArgumentException.class, 
            () -> new InMemoryVectorQuery.SimilarityResult(null, 0.5));
    }
    
    @Test
    @DisplayName("Should require vectors in builder")
    void testBuilderValidation() {
        InMemoryVectorQuery.Builder builder = new InMemoryVectorQuery.Builder()
            .withSimilarityCalculator(new CosineSimilarity());
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            builder::build
        );
        assertEquals("Vector index must be set", exception.getMessage());
    }
}