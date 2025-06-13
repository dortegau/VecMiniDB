package com.dortegau.vecminidb.domain.valueobjects;

import java.io.Serial;
import java.io.Serializable;

public record VectorId(String value) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public VectorId {
        if (value == null) {
            throw new IllegalArgumentException("Vector ID cannot be null");
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Vector ID cannot be empty");
        }
    }
    
    public static VectorId of(String value) {
        return new VectorId(value);
    }
}