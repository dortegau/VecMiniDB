package com.dortegau.vecminidb.domain.entities;

import com.dortegau.vecminidb.domain.valueobjects.VectorData;
import com.dortegau.vecminidb.domain.valueobjects.VectorId;

import java.io.Serial;
import java.io.Serializable;

/**
 * Vector entity representing a vector in the vector database.
 * Immutable entity containing an ID and vector data.
 */
public record Vector(VectorId id, VectorData data) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public Vector {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Vector data cannot be null");
        }
    }
    
    /**
     * Creates a new Vector with the given ID and values.
     * 
     * @param id the vector identifier
     * @param values the vector values
     * @return a new Vector instance
     * @throws IllegalArgumentException if id is null/empty or values is null/empty
     */
    public static Vector of(String id, double[] values) {
        if (id == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Vector values cannot be null");
        }
        
        return new Vector(VectorId.of(id), VectorData.of(values));
    }
    
    /**
     * Gets the vector dimension.
     * 
     * @return the number of dimensions in this vector
     */
    public int getDimension() {
        return data.dimension();
    }
    
    /**
     * Gets the vector values as a defensive copy.
     * 
     * @return a copy of the vector values array
     */
    public double[] getValues() {
        return data.values();
    }
    
    /**
     * Gets the vector ID as a string.
     * 
     * @return the vector identifier
     */
    public String getIdValue() {
        return id.value();
    }
}