package com.dortegau.vecminidb.benchmark;

import com.dortegau.vecminidb.application.ports.in.VectorSimilarityUseCase;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;
import com.dortegau.vecminidb.domain.services.EuclideanDistance;
import com.dortegau.vecminidb.domain.services.ManhattanDistance;
import com.dortegau.vecminidb.infrastructure.adapters.in.MiniTextEmbedder;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorDatabaseFacade;
import com.dortegau.vecminidb.infrastructure.config.VectorDatabaseFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance benchmark tests for VecMiniDB.
 * Tests scalability, algorithm performance, and memory usage.
 */
@DisplayName("VecMiniDB Performance Benchmarks")
class PerformanceBenchmarkTest {
    
    @TempDir
    Path tempDir;
    
    private final Random random = new Random(42); // Fixed seed for reproducible results
    private VectorDatabaseFacade database;
    
    @BeforeEach
    void setUp() {
        // Clean database for each test
        Path dbFile = tempDir.resolve("benchmark.vecdb");
        database = VectorDatabaseFactory.createDefault(dbFile.toString());
    }
    
    @AfterEach
    void tearDown() {
        // Force GC to clean up between tests
        System.gc();
        try {
            Thread.sleep(100); // Give GC time to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @ParameterizedTest
    @ValueSource(ints = {100, 500, 1000, 2500, 5000})
    @DisplayName("Should scale linearly with dataset size")
    void benchmarkScalability(int datasetSize) {
        System.out.printf("%n=== Scalability Test: %,d vectors ===%n", datasetSize);
        
        // Setup: Populate database
        populateDatabase(datasetSize, 64);
        Vector queryVector = generateRandomVector("query", 64);
        
        // Warm up JVM
        warmUp(queryVector, 5);
        
        // Benchmark similarity search
        long[] times = new long[10];
        for (int i = 0; i < times.length; i++) {
            long startTime = System.nanoTime();
            List<Vector> results = database.findSimilar(queryVector, 10);
            long endTime = System.nanoTime();
            
            times[i] = endTime - startTime;
            assertEquals(Math.min(10, datasetSize), results.size());
        }
        
        // Calculate statistics
        long avgTimeNs = calculateAverage(times);
        long minTimeNs = calculateMin(times);
        long maxTimeNs = calculateMax(times);
        
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        double minTimeMs = minTimeNs / 1_000_000.0;
        double maxTimeMs = maxTimeNs / 1_000_000.0;
        
        System.out.printf("Search time: %.2f ms (avg), %.2f ms (min), %.2f ms (max)%n", 
                         avgTimeMs, minTimeMs, maxTimeMs);
        System.out.printf("Throughput: %.0f vectors/ms%n", datasetSize / avgTimeMs);
        
        // Performance assertions (should scale reasonably)
        if (datasetSize <= 1000) {
            assertTrue(avgTimeMs < 50, 
                      String.format("Search of %d vectors should complete in under 50ms, took %.2fms", 
                                  datasetSize, avgTimeMs));
        } else if (datasetSize <= 5000) {
            assertTrue(avgTimeMs < 200, 
                      String.format("Search of %d vectors should complete in under 200ms, took %.2fms", 
                                  datasetSize, avgTimeMs));
        }
    }
    
    @Test
    @DisplayName("Should compare similarity algorithm performance")
    void benchmarkAlgorithmComparison() {
        System.out.printf("%n=== Algorithm Performance Comparison ===%n");
        
        final int datasetSize = 2000;
        final int dimensions = 64;
        
        // Test data
        List<Vector> testVectors = generateTestVectors(datasetSize, dimensions);
        Vector queryVector = generateRandomVector("query", dimensions);
        
        // Test each algorithm
        testAlgorithmPerformance("Cosine Similarity", new CosineSimilarity(), testVectors, queryVector);
        testAlgorithmPerformance("Euclidean Distance", new EuclideanDistance(), testVectors, queryVector);
        testAlgorithmPerformance("Manhattan Distance", new ManhattanDistance(), testVectors, queryVector);
    }
    
    @Test
    @DisplayName("Should perform insertion benchmarks")
    void benchmarkInsertion() {
        System.out.printf("%n=== Insertion Performance ===%n");
        
        final int batchSize = 1000;
        final int dimensions = 64;
        
        // Benchmark batch insertion
        List<Vector> vectors = generateTestVectors(batchSize, dimensions);
        
        long startTime = System.nanoTime();
        for (Vector vector : vectors) {
            database.insert(vector);
        }
        long endTime = System.nanoTime();
        
        long totalTimeNs = endTime - startTime;
        double totalTimeMs = totalTimeNs / 1_000_000.0;
        double avgTimePerInsert = totalTimeMs / batchSize;
        
        System.out.printf("Inserted %,d vectors in %.2f ms%n", batchSize, totalTimeMs);
        System.out.printf("Average time per insert: %.3f ms%n", avgTimePerInsert);
        System.out.printf("Insertion rate: %.0f vectors/second%n", (batchSize * 1000.0) / totalTimeMs);
        
        // Verify all vectors were inserted
        assertEquals(batchSize, database.size());
        
        // Performance assertion
        assertTrue(avgTimePerInsert < 5.0, 
                  String.format("Average insertion time should be under 5ms, was %.3fms", avgTimePerInsert));
    }
    
    @Test
    @DisplayName("Should measure memory usage patterns")
    void benchmarkMemoryUsage() {
        System.out.printf("%n=== Memory Usage Analysis ===%n");
        
        Runtime runtime = Runtime.getRuntime();
        
        // Baseline memory
        System.gc();
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.printf("Baseline memory usage: %.2f MB%n", baselineMemory / 1_048_576.0);
        
        // Load data and measure memory growth
        final int datasetSize = 5000;
        populateDatabase(datasetSize, 64);
        
        System.gc();
        long loadedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = loadedMemory - baselineMemory;
        
        System.out.printf("Memory after loading %,d vectors: %.2f MB%n", datasetSize, loadedMemory / 1_048_576.0);
        System.out.printf("Memory increase: %.2f MB%n", memoryIncrease / 1_048_576.0);
        System.out.printf("Memory per vector: %.0f bytes%n", (double) memoryIncrease / datasetSize);
        
        // Memory efficiency assertion (should be reasonable)
        double memoryPerVectorKB = memoryIncrease / (datasetSize * 1024.0);
        assertTrue(memoryPerVectorKB < 10.0, 
                  String.format("Memory per vector should be under 10KB, was %.2fKB", memoryPerVectorKB));
    }
    
    @Test
    @DisplayName("Should test concurrent query performance")
    void benchmarkConcurrentQueries() {
        System.out.printf("%n=== Concurrent Query Performance ===%n");
        
        // Setup database with test data
        final int datasetSize = 2000;
        populateDatabase(datasetSize, 64);
        
        Vector queryVector = generateRandomVector("query", 64);
        
        // Warm up
        warmUp(queryVector, 3);
        
        // Sequential queries
        long sequentialTime = measureSequentialQueries(queryVector, 50);
        
        // Note: VecMiniDB uses file-based storage, so true concurrency would require
        // thread-safe implementation. This test measures sequential performance
        // as a baseline for future concurrent implementations.
        
        double avgTimeMs = sequentialTime / (50 * 1_000_000.0);
        System.out.printf("Sequential queries: %.2f ms average%n", avgTimeMs);
        System.out.printf("Query rate: %.0f queries/second%n", 50_000.0 / sequentialTime * 1_000_000_000);
        
        assertTrue(avgTimeMs < 100, "Average query time should be under 100ms");
    }
    
    // Helper methods
    
    private void populateDatabase(int size, int dimensions) {
        for (int i = 0; i < size; i++) {
            Vector vector = generateRandomVector("vec_" + i, dimensions);
            database.insert(vector);
        }
    }
    
    private Vector generateRandomVector(String id, int dimensions) {
        double[] values = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            values[i] = random.nextGaussian();
        }
        return Vector.of(id, values);
    }
    
    private List<Vector> generateTestVectors(int count, int dimensions) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> generateRandomVector("test_" + i, dimensions))
                .toList();
    }
    
    private void warmUp(Vector queryVector, int iterations) {
        for (int i = 0; i < iterations; i++) {
            database.findSimilar(queryVector, 5);
        }
    }
    
    private void testAlgorithmPerformance(String algorithmName, 
                                        com.dortegau.vecminidb.domain.services.SimilarityCalculator algorithm,
                                        List<Vector> testVectors, Vector queryVector) {
        
        // Create database with specific algorithm
        Path dbFile = tempDir.resolve("algo_" + algorithmName.replaceAll(" ", "_") + ".vecdb");
        VectorDatabaseFacade algoDb = VectorDatabaseFactory.create(dbFile.toString(), algorithm);
        
        // Insert test vectors
        for (Vector vector : testVectors) {
            algoDb.insert(vector);
        }
        
        // Warm up
        for (int i = 0; i < 3; i++) {
            algoDb.findSimilar(queryVector, 5);
        }
        
        // Benchmark
        long[] times = new long[20];
        for (int i = 0; i < times.length; i++) {
            long startTime = System.nanoTime();
            List<Vector> results = algoDb.findSimilar(queryVector, 10);
            long endTime = System.nanoTime();
            
            times[i] = endTime - startTime;
            assertEquals(10, results.size());
        }
        
        long avgTimeNs = calculateAverage(times);
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        System.out.printf("%-20s: %.2f ms average%n", algorithmName, avgTimeMs);
    }
    
    private long measureSequentialQueries(Vector queryVector, int queryCount) {
        long startTime = System.nanoTime();
        for (int i = 0; i < queryCount; i++) {
            database.findSimilar(queryVector, 10);
        }
        return System.nanoTime() - startTime;
    }
    
    private long calculateAverage(long[] times) {
        long sum = 0;
        for (long time : times) {
            sum += time;
        }
        return sum / times.length;
    }
    
    private long calculateMin(long[] times) {
        long min = times[0];
        for (long time : times) {
            if (time < min) min = time;
        }
        return min;
    }
    
    private long calculateMax(long[] times) {
        long max = times[0];
        for (long time : times) {
            if (time > max) max = time;
        }
        return max;
    }
}