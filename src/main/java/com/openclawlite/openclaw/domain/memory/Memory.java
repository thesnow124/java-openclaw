package com.openclawlite.openclaw.domain.memory;

import java.util.Map;

/**
 * Memory record
 * Represents a piece of text with its embedding
 */
public record Memory(
    String id,
    String sessionId,
    String content,
    float[] embedding,
    String role,
    long timestamp,
    Map<String, Object> metadata,
    int importance
) {
    /**
     * Create a new memory
     */
    public Memory {
        if (importance <= 0) {
            importance = 1;  // Default importance
        }
    }
    
    /**
     * Get embedding dimension
     */
    public int getDimension() {
        return embedding != null ? embedding.length : 0;
    }
    
    /**
     * Builder for Memory
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String sessionId;
        private String content;
        private float[] embedding;
        private String role;
        private Long timestamp;
        private Map<String, Object> metadata;
        private Integer importance;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder embedding(float[] embedding) {
            this.embedding = embedding;
            return this;
        }
        
        public Builder role(String role) {
            this.role = role;
            return this;
        }
        
        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder importance(Integer importance) {
            this.importance = importance;
            return this;
        }
        
        public Memory build() {
            return new Memory(
                id != null ? id : java.util.UUID.randomUUID().toString(),
                sessionId,
                content,
                embedding,
                role,
                timestamp != null ? timestamp : System.currentTimeMillis(),
                metadata != null ? metadata : Map.of(),
                importance != null ? importance : 1
            );
        }
    }
}
