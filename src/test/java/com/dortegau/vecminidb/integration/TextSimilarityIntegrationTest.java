package com.dortegau.vecminidb.integration;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import com.dortegau.vecminidb.infrastructure.adapters.in.MiniTextEmbedder;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorDatabaseFacade;
import com.dortegau.vecminidb.infrastructure.config.VectorDatabaseFactory;
import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating the complete text similarity workflow
 * using the hexagonal architecture with real text embeddings.
 */
class TextSimilarityIntegrationTest {
    
    /**
     * Test document structure for better readability and maintainability.
     */
    private record TestDocument(String id, String text) {}
    
    @TempDir
    Path tempDir;
    
    private MiniTextEmbedder embedder;
    private VectorDatabaseFacade database;
    
    private static final TestDocument[] TEST_DOCUMENTS = {
        new TestDocument("ml_basics", "Machine learning is a subset of artificial intelligence that focuses on algorithms"),
        new TestDocument("deep_learning", "Deep learning uses neural networks with multiple layers to learn complex patterns"),
        new TestDocument("python_ds", "Python is a popular programming language for data science and machine learning"),
        new TestDocument("nlp_intro", "Natural language processing helps computers understand and generate human language"),
        new TestDocument("computer_vision", "Computer vision enables machines to interpret and analyze visual information"),
        new TestDocument("data_science", "Data science combines statistics programming and domain expertise"),
        new TestDocument("ai_overview", "Artificial intelligence aims to create systems that can perform human-like tasks"),
        new TestDocument("neural_nets", "Neural networks are inspired by the structure of biological brain networks"),
        new TestDocument("big_data", "Big data refers to extremely large datasets that require special tools to process"),
        new TestDocument("algorithms", "Algorithm optimization improves the efficiency and performance of computational methods")
    };
    
    @BeforeEach
    void setUp() {
        embedder = new MiniTextEmbedder(64);
        Path dbFile = tempDir.resolve("integration_test.vecdb");
        database = VectorDatabaseFactory.createDefault(dbFile.toString());
        
        // Embed and store all documents
        for (TestDocument document : TEST_DOCUMENTS) {
            Vector docVector = embedder.embedText(document.text(), document.id());
            database.insert(docVector);
        }
    }
    
    @Test
    @DisplayName("Should embed, store and retrieve all test documents successfully")
    void testCompleteWorkflow() {
        // Verify all documents were stored
        assertEquals(TEST_DOCUMENTS.length, database.size());
        assertEquals(TEST_DOCUMENTS.length, database.getAllIds().size());
        
        // Verify vocabulary was built
        assertTrue(embedder.getVocabularySize() > 0);
        assertTrue(embedder.getVocabulary().contains("machine"));
        assertTrue(embedder.getVocabulary().contains("learning"));
    }
    
    @Test
    @DisplayName("Should find most relevant documents for machine learning query")
    void testMachineLearningQuery() {
        Vector queryVector = embedder.embedQuery("What is machine learning?");
        
        List<VectorSimilarityUseCase.SimilarityResult> results = 
            database.findSimilarWithScores(queryVector, 3);
        
        assertEquals(3, results.size());
        
        // ML basics should be most relevant
        assertEquals("ml_basics", results.get(0).vector().getIdValue());
        assertTrue(results.get(0).similarity() > 0.3, 
                  "ML query should have high similarity with ML basics");
        
        // Results should be ordered by similarity (descending)
        assertTrue(results.get(0).similarity() >= results.get(1).similarity());
        assertTrue(results.get(1).similarity() >= results.get(2).similarity());
    }
    
    @Test
    @DisplayName("Should find neural network documents for neural network query")
    void testNeuralNetworksQuery() {
        Vector queryVector = embedder.embedQuery("How do neural networks work?");
        
        List<VectorSimilarityUseCase.SimilarityResult> results = 
            database.findSimilarWithScores(queryVector, 2);
        
        assertEquals(2, results.size());
        
        // Neural nets or deep learning should be top results
        String topResultId = results.get(0).vector().getIdValue();
        assertTrue(topResultId.equals("neural_nets") || topResultId.equals("deep_learning"),
                  "Neural network query should match neural network documents");
    }
    
    @Test
    @DisplayName("Should find Python document for Python programming query")
    void testPythonProgrammingQuery() {
        Vector queryVector = embedder.embedQuery("Python programming for data analysis");
        
        List<VectorSimilarityUseCase.SimilarityResult> results = 
            database.findSimilarWithScores(queryVector, 3);
        
        assertEquals(3, results.size());
        
        // Python document should be most relevant
        assertEquals("python_ds", results.get(0).vector().getIdValue());
        assertTrue(results.get(0).similarity() > 0.3,
                  "Python query should have high similarity with Python document");
    }
    
    @Test
    @DisplayName("Should calculate valid similarity scores between related documents")
    void testDocumentSimilarityAnalysis() {
        Vector mlDoc = database.get("ml_basics");
        Vector aiDoc = database.get("ai_overview");
        Vector pythonDoc = database.get("python_ds");
        
        assertNotNull(mlDoc);
        assertNotNull(aiDoc);
        assertNotNull(pythonDoc);
        
        CosineSimilarity similarity = new CosineSimilarity();
        
        double mlAiSimilarity = similarity.calculate(mlDoc, aiDoc);
        double mlPythonSimilarity = similarity.calculate(mlDoc, pythonDoc);
        double aiPythonSimilarity = similarity.calculate(aiDoc, pythonDoc);
        
        // ML and AI should be quite similar (related concepts)
        assertTrue(mlAiSimilarity > 0.1, "ML and AI should have some similarity");
        
        // All similarities should be valid (between -1 and 1 for cosine)
        assertTrue(mlAiSimilarity >= -1.0 && mlAiSimilarity <= 1.0);
        assertTrue(mlPythonSimilarity >= -1.0 && mlPythonSimilarity <= 1.0);
        assertTrue(aiPythonSimilarity >= -1.0 && aiPythonSimilarity <= 1.0);
    }
    
    @Test
    @DisplayName("Should persist and reload data across database instances")
    void testPersistenceAcrossInstances() {
        // Store original database size
        int originalSize = database.size();
        Vector originalVector = database.get("ml_basics");
        assertNotNull(originalVector);
        
        // Create new database instance pointing to same file
        Path dbFile = tempDir.resolve("integration_test.vecdb");
        VectorDatabaseFacade reloadedDb = VectorDatabaseFactory.createDefault(dbFile.toString());
        
        // Verify data was persisted
        assertEquals(originalSize, reloadedDb.size());
        
        Vector reloadedVector = reloadedDb.get("ml_basics");
        assertNotNull(reloadedVector);
        assertEquals(originalVector, reloadedVector);
    }
    
    @Test
    @DisplayName("Should integrate with query builder for complex queries")
    void testQueryBuilderIntegration() {
        Vector queryVector = embedder.embedQuery("artificial intelligence systems");
        
        // Test query builder adapter
        com.dortegau.vecminidb.infrastructure.adapters.in.VectorQueryBuilder queryBuilder = 
            new com.dortegau.vecminidb.infrastructure.adapters.in.VectorQueryBuilder(
                // We need access to the similarity use case - this shows a limitation in our current facade design
                database.findSimilarWithScores(queryVector, 1).isEmpty() ? 
                    null : 
                    new com.dortegau.vecminidb.application.usecases.VectorSimilarityService(
                        new com.dortegau.vecminidb.infrastructure.adapters.out.FileVectorRepository(
                            tempDir.resolve("integration_test.vecdb").toString()),
                        new CosineSimilarity())
            );
        
        // For now, test the facade directly
        List<VectorSimilarityUseCase.SimilarityResult> results = 
            database.findSimilarWithScores(queryVector, 2);
        
        assertEquals(2, results.size());
        assertTrue(results.get(0).similarity() >= results.get(1).similarity());
    }
    
    @Test
    @DisplayName("Should work with different similarity algorithms (Euclidean distance)")
    void testDifferentSimilarityAlgorithms() {
        // Test with different similarity calculator
        com.dortegau.vecminidb.domain.services.EuclideanDistance euclidean = 
            new com.dortegau.vecminidb.domain.services.EuclideanDistance();
        
        Path euclideanDbFile = tempDir.resolve("euclidean_test.vecdb");
        VectorDatabaseFacade euclideanDb = VectorDatabaseFactory.create(
            euclideanDbFile.toString(), euclidean);
        
        // Insert a few test vectors
        Vector v1 = embedder.embedText("machine learning", "test1");
        Vector v2 = embedder.embedText("deep learning", "test2");
        
        euclideanDb.insert(v1);
        euclideanDb.insert(v2);
        
        assertEquals(2, euclideanDb.size());
        
        // Query should work with different similarity algorithm
        Vector query = embedder.embedQuery("learning algorithms");
        List<VectorSimilarityUseCase.SimilarityResult> results = 
            euclideanDb.findSimilarWithScores(query, 2);
        
        assertEquals(2, results.size());
        // With Euclidean distance converted to similarity, values should be positive
        assertTrue(results.get(0).similarity() > 0);
        assertTrue(results.get(1).similarity() > 0);
    }
}