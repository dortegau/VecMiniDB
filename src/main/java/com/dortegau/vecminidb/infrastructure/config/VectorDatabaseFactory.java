package com.dortegau.vecminidb.infrastructure.config;

import com.dortegau.vecminidb.application.ports.in.VectorDatabaseUseCase;
import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.application.ports.out.VectorRepository;
import com.dortegau.vecminidb.application.usecases.VectorDatabaseService;
import com.dortegau.vecminidb.application.usecases.VectorSimilarityService;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import com.dortegau.vecminidb.domain.services.SimilarityCalculator;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorDatabaseFacade;
import com.dortegau.vecminidb.infrastructure.adapters.out.FileVectorRepository;

/**
 * Factory for creating pre-configured vector database instances.
 * Handles dependency injection and wiring of hexagonal architecture components.
 */
public class VectorDatabaseFactory {
    
    /**
     * Creates a vector database facade with default configuration.
     * Uses file-based persistence and cosine similarity.
     * 
     * @param filePath the path to the data file
     * @return configured vector database facade
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static VectorDatabaseFacade createDefault(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        if (filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        
        return create(filePath, new CosineSimilarity());
    }
    
    /**
     * Creates a vector database facade with custom similarity calculator.
     * Uses file-based persistence.
     * 
     * @param filePath the path to the data file
     * @param similarityCalculator the similarity calculator to use
     * @return configured vector database facade
     * @throws IllegalArgumentException if any parameter is null or filePath is empty
     */
    public static VectorDatabaseFacade create(String filePath, SimilarityCalculator similarityCalculator) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        if (filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        if (similarityCalculator == null) {
            throw new IllegalArgumentException("Similarity calculator cannot be null");
        }
        
        // Infrastructure layer
        VectorRepository vectorRepository = new FileVectorRepository(filePath);
        
        // Application layer
        VectorDatabaseUseCase databaseUseCase = new VectorDatabaseService(vectorRepository);
        VectorSimilarityUseCase similarityUseCase = new VectorSimilarityService(vectorRepository, similarityCalculator);
        
        // Presentation layer
        return new VectorDatabaseFacade(databaseUseCase, similarityUseCase);
    }
    
    /**
     * Creates a vector database facade with custom repository and similarity calculator.
     * 
     * @param vectorRepository the repository implementation to use
     * @param similarityCalculator the similarity calculator to use
     * @return configured vector database facade
     * @throws IllegalArgumentException if any parameter is null
     */
    public static VectorDatabaseFacade create(VectorRepository vectorRepository, SimilarityCalculator similarityCalculator) {
        if (vectorRepository == null) {
            throw new IllegalArgumentException("Vector repository cannot be null");
        }
        if (similarityCalculator == null) {
            throw new IllegalArgumentException("Similarity calculator cannot be null");
        }
        
        // Application layer
        VectorDatabaseUseCase databaseUseCase = new VectorDatabaseService(vectorRepository);
        VectorSimilarityUseCase similarityUseCase = new VectorSimilarityService(vectorRepository, similarityCalculator);
        
        // Presentation layer
        return new VectorDatabaseFacade(databaseUseCase, similarityUseCase);
    }
}