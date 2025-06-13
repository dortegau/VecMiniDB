package com.dortegau.vecminidb.domain.valueobjects;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public record VectorData(double[] values) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public VectorData {
        if (values == null) {
            throw new IllegalArgumentException("Vector values cannot be null");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException("Vector values cannot be empty");
        }
        
        // Defensive copy
        values = Arrays.copyOf(values, values.length);
    }
    
    public double[] values() {
        // Defensive copy on access
        return Arrays.copyOf(values, values.length);
    }
    
    public int dimension() {
        return values.length;
    }
    
    public static VectorData of(double[] values) {
        return new VectorData(values);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VectorData that = (VectorData) obj;
        return Arrays.equals(values, that.values);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
    
    @Override
    public String toString() {
        return "VectorData{values=" + Arrays.toString(values) + "}";
    }
}