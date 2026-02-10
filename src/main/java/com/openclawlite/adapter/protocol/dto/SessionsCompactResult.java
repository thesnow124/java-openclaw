package com.openclawlite.adapter.protocol.dto;

/**
 * Sessions compact result
 */
public record SessionsCompactResult(
    /**
     * Whether compaction was successful
     */
    boolean ok,
    
    /**
     * Session key
     */
    String sessionKey,
    
    /**
     * Token count before compaction
     */
    int tokensBefore,
    
    /**
     * Token count after compaction
     */
    int tokensAfter,
    
    /**
     * Number of messages removed
     */
    int messagesRemoved
) {}
