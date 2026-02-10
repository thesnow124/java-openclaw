package com.openclawlite.openclaw.domain.memory;

import com.openclawlite.openclaw.infrastructure.persistence.memory.MemoryRepository;
import com.openclawlite.openclaw.infrastructure.embedding.EmbeddingProvider;
import com.openclawlite.openclaw.domain.memory.search.HybridSearch;
import com.openclawlite.openclaw.domain.memory.search.VectorSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory service
 * High-level API for managing memories with embeddings
 */
@Service
public class MemoryService {
    
    private static final Logger log = LoggerFactory.getLogger(MemoryService.class);
    
    private final MemoryRepository memoryRepository;
    private final EmbeddingProvider embeddingProvider;
    private final VectorSearch vectorSearch;
    private final HybridSearch hybridSearch;
    
    public MemoryService(
            MemoryRepository memoryRepository,
            EmbeddingProvider embeddingProvider,
            VectorSearch vectorSearch,
            HybridSearch hybridSearch) {
        this.memoryRepository = memoryRepository;
        this.embeddingProvider = embeddingProvider;
        this.vectorSearch = vectorSearch;
        this.hybridSearch = hybridSearch;
    }
    
    /**
     * Add a memory with automatic embedding generation
     */
    public Memory addMemory(String sessionId, String content, String role) {
        return addMemory(sessionId, content, role, 1);
    }
    
    /**
     * Add a memory with importance
     */
    public Memory addMemory(String sessionId, String content, String role, int importance) {
        try {
            // Generate embedding
            float[] embedding = embeddingProvider.embed(content);
            
            // Create memory
            Memory memory = Memory.builder()
                .sessionId(sessionId)
                .content(content)
                .role(role)
                .embedding(embedding)
                .importance(importance)
                .build();
            
            // Save
            memoryRepository.save(memory);
            
            log.info("Added memory: {} for session: {}", memory.id(), sessionId);
            
            return memory;
            
        } catch (Exception e) {
            log.error("Failed to add memory", e);
            throw new RuntimeException("Failed to add memory", e);
        }
    }
    
    /**
     * Add a memory with custom embedding
     */
    public void addMemory(Memory memory) {
        memoryRepository.save(memory);
    }
    
    /**
     * Add multiple memories (batch)
     */
    public List<Memory> addMemories(List<MemoryData> memories) {
        List<Memory> results = new ArrayList<>();
        
        // Batch embedding generation
        List<String> texts = memories.stream()
            .map(MemoryData::content)
            .toList();
        
        List<float[]> embeddings = embeddingProvider.embedBatch(texts);
        
        // Create and save memories
        for (int i = 0; i < memories.size(); i++) {
            MemoryData data = memories.get(i);
            float[] embedding = embeddings.get(i);
            
            Memory memory = Memory.builder()
                .sessionId(data.sessionId())
                .content(data.content())
                .role(data.role())
                .embedding(embedding)
                .importance(data.importance())
                .build();
            
            memoryRepository.save(memory);
            results.add(memory);
        }
        
        log.info("Added {} memories", results.size());
        
        return results;
    }
    
    /**
     * Get memory by ID
     */
    public java.util.Optional<Memory> getMemory(String id) {
        return memoryRepository.findById(id);
    }
    
    /**
     * Get all memories for a session
     */
    public List<Memory> getSessionMemories(String sessionId) {
        return memoryRepository.findBySessionId(sessionId);
    }
    
    /**
     * Get recent memories
     */
    public List<Memory> getRecentMemories(int limit) {
        return memoryRepository.findRecent(limit);
    }
    
    /**
     * Vector similarity search
     */
    public List<VectorSearch.SearchResult> searchSimilar(String query, String sessionId, int topK) {
        try {
            // Generate query embedding
            float[] queryEmbedding = embeddingProvider.embed(query);
            
            // Get memories for session
            List<Memory> memories = memoryRepository.findBySessionId(sessionId);
            
            // Search
            return vectorSearch.search(queryEmbedding, memories, topK);
            
        } catch (Exception e) {
            log.error("Failed to search memories", e);
            return List.of();
        }
    }
    
    /**
     * Vector similarity search across all sessions
     */
    public List<VectorSearch.SearchResult> searchSimilarGlobal(String query, int topK) {
        try {
            // Generate query embedding
            float[] queryEmbedding = embeddingProvider.embed(query);
            
            // Get recent memories (limit to reasonable number)
            List<Memory> memories = memoryRepository.findRecent(1000);
            
            // Search
            return vectorSearch.search(queryEmbedding, memories, topK);
            
        } catch (Exception e) {
            log.error("Failed to search memories globally", e);
            return List.of();
        }
    }
    
    /**
     * Hybrid search (vector + keyword)
     */
    public List<HybridSearch.HybridResult> searchHybrid(String query, String sessionId, int topK, float alpha) {
        try {
            // Generate query embedding
            float[] queryEmbedding = embeddingProvider.embed(query);
            
            // Get memories for session
            List<Memory> memories = memoryRepository.findBySessionId(sessionId);
            
            // Search
            return hybridSearch.search(queryEmbedding, query, memories, topK, alpha);
            
        } catch (Exception e) {
            log.error("Failed to search memories", e);
            return List.of();
        }
    }
    
    /**
     * Delete memory
     */
    public boolean deleteMemory(String id) {
        return memoryRepository.deleteById(id);
    }
    
    /**
     * Delete all memories for a session
     */
    public int deleteSessionMemories(String sessionId) {
        return memoryRepository.deleteBySessionId(sessionId);
    }
    
    /**
     * Get memory count for session
     */
    public int getSessionMemoryCount(String sessionId) {
        return memoryRepository.countBySessionId(sessionId);
    }
    
    /**
     * Memory data record for batch operations
     */
    public record MemoryData(
        String sessionId,
        String content,
        String role,
        int importance
    ) {
        public MemoryData(String sessionId, String content, String role) {
            this(sessionId, content, role, 1);
        }
    }
}
