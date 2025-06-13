package com.dortegau.vecminidb.infrastructure.adapters;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import com.dortegau.vecminidb.infrastructure.adapters.in.MiniTextEmbedder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MiniTextEmbedder adapter.
 */
class MiniTextEmbedderTest {
    
    private MiniTextEmbedder embedder;
    
    @BeforeEach
    void setUp() {
        embedder = new MiniTextEmbedder(64);
    }
    
    @Test
    void testConstructorWithInvalidDimension() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new MiniTextEmbedder(0)
        );
        assertEquals("Vector dimension must be positive", exception.getMessage());
    }
    
    @Test
    void testEmbedText() {
        Vector vector = embedder.embedText("machine learning algorithm", "test");
        
        assertEquals("test", vector.getIdValue());
        assertEquals(64, vector.getDimension());
        
        // Vector should be normalized (L2 norm ≈ 1)
        double norm = 0.0;
        for (double value : vector.getValues()) {
            norm += value * value;
        }
        assertEquals(1.0, Math.sqrt(norm), 1e-10);
    }
    
    @Test
    void testEmbedTextWithNullText() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> embedder.embedText(null, "test")
        );
        assertEquals("Text cannot be null", exception.getMessage());
    }
    
    @Test
    void testEmbedTextWithNullId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> embedder.embedText("test", null)
        );
        assertEquals("ID cannot be null", exception.getMessage());
    }
    
    @Test
    void testConsistentEmbeddings() {
        Vector v1 = embedder.embedText("machine learning", "doc1");
        Vector v2 = embedder.embedText("machine learning", "doc2");
        
        // Same text should produce same embeddings (ignoring ID)
        assertArrayEquals(v1.getValues(), v2.getValues(), 1e-10);
    }
    
    @Test
    void testSimilarTexts() {
        Vector v1 = embedder.embedText("machine learning algorithm", "doc1");
        Vector v2 = embedder.embedText("learning machine algorithm", "doc2");
        Vector v3 = embedder.embedText("completely different text", "doc3");
        
        // Similar texts should have higher similarity
        CosineSimilarity similarity = new CosineSimilarity();
        double sim12 = similarity.calculate(v1, v2);
        double sim13 = similarity.calculate(v1, v3);
        
        assertTrue(sim12 > sim13, "Similar texts should have higher similarity");
    }
    
    @Test
    void testEmptyText() {
        Vector vector = embedder.embedText("", "empty");
        
        // Empty text should produce zero vector
        for (double value : vector.getValues()) {
            assertEquals(0.0, value, 1e-10);
        }
    }
    
    @Test
    void testVocabularyGrowth() {
        assertEquals(0, embedder.getVocabularySize());
        
        embedder.embedText("machine learning", "doc1");
        assertEquals(2, embedder.getVocabularySize());
        
        embedder.embedText("deep learning", "doc2");
        assertEquals(3, embedder.getVocabularySize()); // "deep" is new
        
        assertTrue(embedder.getVocabulary().contains("machine"));
        assertTrue(embedder.getVocabulary().contains("learning"));
        assertTrue(embedder.getVocabulary().contains("deep"));
    }
    
    @Test
    void testTextPreprocessing() {
        Vector v1 = embedder.embedText("Machine Learning!", "doc1");
        Vector v2 = embedder.embedText("machine learning", "doc2");
        
        // Should handle case and punctuation
        assertArrayEquals(v1.getValues(), v2.getValues(), 1e-10);
    }
    
    @Test
    void testWordFrequencyWeighting() {
        Vector v1 = embedder.embedText("machine learning", "doc1");
        Vector v2 = embedder.embedText("machine machine learning", "doc2");
        
        // Different word frequencies should produce different embeddings
        assertFalse(java.util.Arrays.equals(v1.getValues(), v2.getValues()));
    }
    
    @Test
    void testEmbedQuery() {
        Vector queryVector = embedder.embedQuery("what is machine learning");
        
        assertEquals("query", queryVector.getIdValue());
        assertEquals(64, queryVector.getDimension());
    }
}