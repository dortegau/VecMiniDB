package com.dortegau.vecminidb.examples;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import com.dortegau.vecminidb.domain.services.SimilarityCalculator;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorDatabaseFacade;
import com.dortegau.vecminidb.infrastructure.config.VectorDatabaseFactory;
import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;

import java.util.List;

/**
 * Example comparing different similarity calculation algorithms.
 * Demonstrates the modular design of the similarity calculation system.
 */
public class SimilarityComparison {
    
    public static void main(String[] args) {
        System.out.println("=== Similarity Algorithm Comparison ===\n");
        
        // Create test vectors
        Vector queryVector = Vector.of("query", new double[]{1.0, 0.5, 0.0, 0.8});
        Vector doc1 = Vector.of("doc1", new double[]{0.9, 0.6, 0.1, 0.7});  // Very similar
        Vector doc2 = Vector.of("doc2", new double[]{0.1, 0.9, 0.8, 0.2});  // Different
        Vector doc3 = Vector.of("doc3", new double[]{2.0, 1.0, 0.0, 1.6});  // Proportional
        
        // Test with different similarity algorithms
        testWithCosineSimilarity(queryVector, doc1, doc2, doc3);
        
        // Show how to use different calculators in database
        demonstrateDatabaseWithDifferentSimilarity();
        
        System.out.println("=== Comparison completed ===");
    }
    
    private static void testWithCosineSimilarity(Vector query, Vector doc1, Vector doc2, Vector doc3) {
        System.out.println("=== Direct Similarity Calculations ===\n");
        
        SimilarityCalculator cosine = new CosineSimilarity();
        
        System.out.println("Query vector: " + formatVector(query.getValues()));
        System.out.println();
        
        System.out.println("Using Cosine Similarity:");
        System.out.printf("  Query ↔ Doc1: %.4f%n", cosine.calculate(query, doc1));
        System.out.printf("  Query ↔ Doc2: %.4f%n", cosine.calculate(query, doc2));
        System.out.printf("  Query ↔ Doc3: %.4f%n", cosine.calculate(query, doc3));
        System.out.println();
        
        // Explain the results
        System.out.println("Analysis:");
        System.out.println("  - Doc1 should have highest similarity (similar values)");
        System.out.println("  - Doc3 should have perfect similarity (proportional vectors)");
        System.out.println("  - Doc2 should have lowest similarity (different pattern)");
        System.out.println();
    }
    
    private static void demonstrateDatabaseWithDifferentSimilarity() {
        System.out.println("=== Database with Different Similarity Algorithms ===\n");
        
        // Create database with custom similarity calculator
        SimilarityCalculator customSimilarity = new CosineSimilarity();
        VectorDatabaseFacade database = VectorDatabaseFactory.create("data/similarity_test_minidb.vecdb", customSimilarity);
        
        // Insert test vectors
        Vector query = Vector.of("query", new double[]{1.0, 0.5, 0.0});
        Vector similar = Vector.of("similar", new double[]{0.8, 0.4, 0.1});
        Vector different = Vector.of("different", new double[]{0.1, 0.9, 0.8});
        Vector proportional = Vector.of("proportional", new double[]{2.0, 1.0, 0.0});
        
        database.insert(similar);
        database.insert(different);
        database.insert(proportional);
        
        System.out.println("Inserted 3 test vectors into database");
        System.out.println("Query vector: " + formatVector(query.getValues()));
        System.out.println();
        
        // Perform similarity search
        List<VectorSimilarityUseCase.SimilarityResult> results = 
            database.findSimilarWithScores(query, 3);
        
        System.out.println("Similarity search results (using " + customSimilarity.getName() + "):");
        for (int i = 0; i < results.size(); i++) {
            VectorSimilarityUseCase.SimilarityResult result = results.get(i);
            System.out.printf("  %d. %-12s: %.4f%n", 
                            i + 1, result.vector().getIdValue(), result.similarity());
        }
        System.out.println();
        
        // Demonstrate extensibility
        System.out.println("The architecture allows easy addition of new similarity algorithms:");
        System.out.println("  - Implement SimilarityCalculator interface");
        System.out.println("  - Pass to VectorDatabaseFactory.create()");
        System.out.println("  - No changes needed in other layers");
        System.out.println();
    }
    
    private static String formatVector(double[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.1f", values[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}