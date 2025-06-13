package com.dortegau.vecminidb.infrastructure.adapters.in;

import com.dortegau.vecminidb.domain.entities.Vector;

import java.util.*;

/**
 * Simple text-to-vector embedding adapter for demonstration purposes.
 * Converts text to vectors using bag-of-words with TF weighting.
 * This is an input adapter that transforms text into domain entities.
 */
public class MiniTextEmbedder {
    private final int vectorDimension;
    private final Map<String, Integer> vocabulary;
    
    /**
     * Creates a new text embedder with specified vector dimension.
     * 
     * @param vectorDimension the dimension of output vectors
     * @throws IllegalArgumentException if vectorDimension is not positive
     */
    public MiniTextEmbedder(int vectorDimension) {
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        
        this.vectorDimension = vectorDimension;
        this.vocabulary = new HashMap<>();
    }
    
    /**
     * Embeds text into a vector with the given ID.
     * 
     * @param text the text to embed
     * @param id the vector identifier
     * @return vector representation of the text
     * @throws IllegalArgumentException if text or id is null
     */
    public Vector embedText(String text, String id) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        String[] words = preprocessText(text);
        double[] embedding = new double[vectorDimension];
        
        // Simple bag-of-words with TF weighting
        Map<String, Integer> wordCounts = countWords(words);
        
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            
            // Get or create word vector
            double[] wordVector = getWordVector(word);
            
            // Add weighted word vector to document embedding
            double weight = (double) count / words.length; // TF weighting
            for (int i = 0; i < vectorDimension; i++) {
                embedding[i] += weight * wordVector[i];
            }
        }
        
        // L2 normalization
        double norm = 0.0;
        for (double value : embedding) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        
        if (norm > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= norm;
            }
        }
        
        return Vector.of(id, embedding);
    }
    
    /**
     * Embeds query text into a vector with "query" as ID.
     * 
     * @param queryText the query text to embed
     * @return vector representation of the query
     * @throws IllegalArgumentException if queryText is null
     */
    public Vector embedQuery(String queryText) {
        return embedText(queryText, "query");
    }
    
    /**
     * Gets the current vocabulary size.
     * 
     * @return number of unique words in vocabulary
     */
    public int getVocabularySize() {
        return vocabulary.size();
    }
    
    /**
     * Gets a copy of the vocabulary.
     * 
     * @return set of words in vocabulary
     */
    public Set<String> getVocabulary() {
        return new HashSet<>(vocabulary.keySet());
    }
    
    private String[] preprocessText(String text) {
        return text.toLowerCase()
                  .replaceAll("[^a-zA-Z\\s]", "")
                  .split("\\s+");
    }
    
    private Map<String, Integer> countWords(String[] words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            if (!word.isEmpty()) {
                counts.put(word, counts.getOrDefault(word, 0) + 1);
            }
        }
        return counts;
    }
    
    private double[] getWordVector(String word) {
        // Get word index (creates new if doesn't exist)
        int wordIndex = vocabulary.computeIfAbsent(word, k -> vocabulary.size());
        
        // Generate consistent pseudo-random vector for this word
        Random wordRandom = new Random(wordIndex);
        double[] vector = new double[vectorDimension];
        
        for (int i = 0; i < vectorDimension; i++) {
            vector[i] = wordRandom.nextGaussian();
        }
        
        return vector;
    }
}