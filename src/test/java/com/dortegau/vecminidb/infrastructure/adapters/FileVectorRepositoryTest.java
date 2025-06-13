package com.dortegau.vecminidb.infrastructure.adapters;

import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;
import com.dortegau.vecminidb.infrastructure.adapters.out.FileVectorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FileVectorRepository.
 */
class FileVectorRepositoryTest {
    
    @TempDir
    Path tempDir;
    
    private FileVectorRepository repository;
    private Path dbFile;
    
    @BeforeEach
    void setUp() {
        dbFile = tempDir.resolve("test.vecdb");
        repository = new FileVectorRepository(dbFile.toString());
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(dbFile)) {
            Files.delete(dbFile);
        }
    }
    
    @Test
    void testConstructorWithNullPath() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FileVectorRepository(null)
        );
        assertEquals("File path cannot be null", exception.getMessage());
    }
    
    @Test
    void testConstructorWithEmptyPath() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FileVectorRepository("")
        );
        assertEquals("File path cannot be empty", exception.getMessage());
    }
    
    @Test
    void testSaveAndFindById() {
        Vector vector = Vector.of("test1", new double[]{1.0, 2.0, 3.0});
        
        repository.save(vector);
        Optional<Vector> retrieved = repository.findById(vector.id());
        
        assertTrue(retrieved.isPresent());
        assertEquals(vector, retrieved.get());
        assertEquals(1, repository.count());
    }
    
    @Test
    void testSaveWithNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.save(null)
        );
        assertEquals("Vector cannot be null", exception.getMessage());
    }
    
    @Test
    void testFindByIdNotExists() {
        VectorId vectorId = VectorId.of("nonexistent");
        
        Optional<Vector> result = repository.findById(vectorId);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdWithNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.findById(null)
        );
        assertEquals("Vector ID cannot be null", exception.getMessage());
    }
    
    @Test
    void testDeleteById() {
        Vector vector = Vector.of("test1", new double[]{1.0, 2.0, 3.0});
        repository.save(vector);
        
        boolean deleted = repository.deleteById(vector.id());
        
        assertTrue(deleted);
        assertFalse(repository.findById(vector.id()).isPresent());
        assertEquals(0, repository.count());
    }
    
    @Test
    void testDeleteByIdNotExists() {
        VectorId vectorId = VectorId.of("nonexistent");
        
        boolean deleted = repository.deleteById(vectorId);
        
        assertFalse(deleted);
    }
    
    @Test
    void testDeleteByIdWithNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.deleteById(null)
        );
        assertEquals("Vector ID cannot be null", exception.getMessage());
    }
    
    @Test
    void testFindAll() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 0.0});
        Vector v2 = Vector.of("v2", new double[]{0.0, 1.0});
        
        repository.save(v1);
        repository.save(v2);
        
        Set<Vector> all = repository.findAll();
        
        assertEquals(2, all.size());
        assertTrue(all.contains(v1));
        assertTrue(all.contains(v2));
    }
    
    @Test
    void testFindAllIds() {
        Vector v1 = Vector.of("v1", new double[]{1.0, 0.0});
        Vector v2 = Vector.of("v2", new double[]{0.0, 1.0});
        
        repository.save(v1);
        repository.save(v2);
        
        Set<VectorId> allIds = repository.findAllIds();
        
        assertEquals(2, allIds.size());
        assertTrue(allIds.contains(v1.id()));
        assertTrue(allIds.contains(v2.id()));
    }
    
    @Test
    void testPersistence() {
        Vector vector = Vector.of("persistent", new double[]{1.0, 2.0, 3.0});
        repository.save(vector);
        
        // Create new repository instance pointing to same file
        FileVectorRepository newRepository = new FileVectorRepository(dbFile.toString());
        Optional<Vector> retrieved = newRepository.findById(vector.id());
        
        assertTrue(retrieved.isPresent());
        assertEquals(vector, retrieved.get());
        assertEquals(1, newRepository.count());
    }
    
    @Test
    void testOverwriteVector() {
        VectorId id = VectorId.of("same_id");
        Vector v1 = Vector.of("same_id", new double[]{1.0, 0.0});
        Vector v2 = Vector.of("same_id", new double[]{0.0, 1.0});
        
        repository.save(v1);
        assertEquals(1, repository.count());
        
        repository.save(v2);
        assertEquals(1, repository.count());
        
        Optional<Vector> retrieved = repository.findById(id);
        assertTrue(retrieved.isPresent());
        assertEquals(v2, retrieved.get());
    }
}