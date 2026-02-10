package com.openclawlite.openclaw.domain.memory.search;

import com.openclawlite.openclaw.domain.memory.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hybrid search combining vector similarity and keyword matching (BM25-like)
 */
@Component
public class HybridSearch {
    
    private static final Logger log = LoggerFactory.getLogger(HybridSearch.class);
    
    private final VectorSearch vectorSearch;
    
    public HybridSearch(VectorSearch vectorSearch) {
        this.vectorSearch = vectorSearch;
    }
    
    /**
     * Hybrid search result
     */
    public record HybridResult(
        Memory memory,
        float vectorScore,
        float keywordScore,
        float combinedScore
    ) {}
    
    /**
     * Search with hybrid scoring
     * 
     * @param queryEmbedding Query embedding vector
     * @param queryText Query text for keyword matching
     * @param memories List of memories to search
     * @param topK Number of top results
     * @param alpha Weight for vector score (0-1), keyword score = 1-alpha
     * @return List of hybrid search results
     */
    public List<HybridResult> search(
            float[] queryEmbedding,
            String queryText,
            List<Memory> memories,
            int topK,
            float alpha) {
        
        if (memories == null || memories.isEmpty()) {
            return List.of();
        }
        
        // Calculate scores for each memory
        List<HybridResult> results = new ArrayList<>();
        
        // Get vector search results first
        List<VectorSearch.SearchResult> vectorResults = vectorSearch.search(queryEmbedding, memories, memories.size());
        Map<String, Float> vectorScores = vectorResults.stream()
            .collect(Collectors.toMap(
                r -> r.memory().id(),
                r -> r.similarity()
            ));
        
        // Calculate keyword scores
        Map<String, Float> keywordScores = calculateKeywordScores(queryText, memories);
        
        // Combine scores
        for (Memory memory : memories) {
            float vectorScore = vectorScores.getOrDefault(memory.id(), 0f);
            float keywordScore = keywordScores.getOrDefault(memory.id(), 0f);
            
            // Normalize scores to 0-1
            vectorScore = Math.max(0, Math.min(1, vectorScore));
            keywordScore = Math.max(0, Math.min(1, keywordScore));
            
            // Combined score with weight alpha
            float combinedScore = alpha * vectorScore + (1 - alpha) * keywordScore;
            
            results.add(new HybridResult(memory, vectorScore, keywordScore, combinedScore));
        }
        
        // Sort by combined score and get top K
        return results.stream()
            .sorted((a, b) -> Float.compare(b.combinedScore(), a.combinedScore()))
            .limit(topK)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate keyword relevance scores (simplified BM25-like)
     */
    private Map<String, Float> calculateKeywordScores(String query, List<Memory> memories) {
        Map<String, Float> scores = new HashMap<>();
        
        if (query == null || query.isEmpty()) {
            // No keyword score for all
            memories.forEach(m -> scores.put(m.id(), 0f));
            return scores;
        }
        
        // Tokenize query
        Set<String> queryTerms = Arrays.stream(query.toLowerCase().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
        
        if (queryTerms.isEmpty()) {
            memories.forEach(m -> scores.put(m.id(), 0f));
            return scores;
        }
        
        // Calculate TF-IDF-like score for each memory
        for (Memory memory : memories) {
            float score = 0;
            String content = memory.content().toLowerCase();
            
            for (String term : queryTerms) {
                // Count term occurrences
                int count = countOccurrences(content, term);
                
                // Simple TF-like score
                if (count > 0) {
                    score += (float) (count * (1 + Math.log(count)));
                }
            }
            
            // Normalize by content length
            score = score / (float) Math.sqrt(content.length());
            
            scores.put(memory.id(), score);
        }
        
        // Normalize to 0-1
        float maxScore = scores.values().stream().max(Float::compare).orElse(1f);
        if (maxScore > 0) {
            scores.replaceAll((id, s) -> s / maxScore);
        }
        
        return scores;
    }
    
    /**
     * Count occurrences of term in text
     */
    private int countOccurrences(String text, String term) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(term, index)) != -1) {
            count++;
            index += term.length();
        }
        
        return count;
    }
}
