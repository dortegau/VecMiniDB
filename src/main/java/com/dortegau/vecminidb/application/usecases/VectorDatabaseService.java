package com.dortegau.vecminidb.application.usecases;

import com.dortegau.vecminidb.application.ports.in.VectorDatabaseUseCase;
import com.dortegau.vecminidb.application.ports.out.VectorRepository;
import com.dortegau.vecminidb.domain.entities.Vector;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.util.Optional;
import java.util.Set;

/**
 * Implementation of vector database use cases.
 * Orchestrates domain logic and repository operations.
 */
public class VectorDatabaseService implements VectorDatabaseUseCase {
    
    private final VectorRepository vectorRepository;
    
    public VectorDatabaseService(VectorRepository vectorRepository) {
        if (vectorRepository == null) {
            throw new IllegalArgumentException("Vector repository cannot be null");
        }
        this.vectorRepository = vectorRepository;
    }
    
    @Override
    public void storeVector(Vector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        
        vectorRepository.save(vector);
    }
    
    @Override
    public Optional<Vector> getVector(VectorId vectorId) {
        if (vectorId == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        return vectorRepository.findById(vectorId);
    }
    
    @Override
    public boolean deleteVector(VectorId vectorId) {
        if (vectorId == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        
        return vectorRepository.deleteById(vectorId);
    }
    
    @Override
    public Set<VectorId> getAllVectorIds() {
        return vectorRepository.findAllIds();
    }
    
    @Override
    public int getVectorCount() {
        return vectorRepository.count();
    }
}