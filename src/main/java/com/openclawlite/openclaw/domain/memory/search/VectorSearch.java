package com.openclawlite.openclaw.domain.memory.search;

import com.openclawlite.openclaw.domain.memory.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Vector similarity search
 * Computes cosine similarity between embeddings
 */
@Component
public class VectorSearch {
    
    private static final Logger log = LoggerFactory.getLogger(VectorSearch.class);
    
    /**
     * Search result
     */
    public record SearchResult(
        Memory memory,
        float similarity
    ) {}
    
    /**
     * Search by vector similarity
     * 
     * @param queryEmbedding Query embedding vector
     * @param memories List of memories to search
     * @param topK Number of top results to return
     * @return List of search results sorted by similarity
     */
    public List<SearchResult> search(float[] queryEmbedding, List<Memory> memories, int topK) {
        if (queryEmbedding == null || memories == null || memories.isEmpty()) {
            return List.of();
        }
        
        // Use priority queue to get top K results
        PriorityQueue<SearchResult> queue = new PriorityQueue<>(
            topK,
            Comparator.comparing(SearchResult::similarity)
        );
        
        for (Memory memory : memories) {
            if (memory.embedding() == null || memory.embedding().length != queryEmbedding.length) {
                continue;
            }
            
            float similarity = cosineSimilarity(queryEmbedding, memory.embedding());
            
            SearchResult result = new SearchResult(memory, similarity);
            
            if (queue.size() < topK) {
                queue.offer(result);
            } else if (similarity > queue.peek().similarity()) {
                queue.poll();
                queue.offer(result);
            }
        }
        
        // Convert to list and sort descending
        List<SearchResult> results = new ArrayList<>(queue);
        results.sort((a, b) -> Float.compare(b.similarity(), a.similarity()));
        
        return results;
    }
    
    /**
     * Compute cosine similarity between two vectors
     */
    public float cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        return (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }
    
    /**
     * Compute Euclidean distance between two vectors
     */
    public float euclideanDistance(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        float sum = 0;
        for (int i = 0; i < vec1.length; i++) {
            float diff = vec1[i] - vec2[i];
            sum += diff * diff;
        }
        
        return (float) Math.sqrt(sum);
    }
    
    /**
     * Normalize a vector to unit length
     */
    public float[] normalize(float[] vector) {
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        
        if (norm == 0) {
            return vector;
        }
        
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }
        
        return normalized;
    }
}
