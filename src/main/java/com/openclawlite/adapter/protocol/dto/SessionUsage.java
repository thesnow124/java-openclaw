package com.openclawlite.adapter.protocol.dto;

/**
 * Session usage info
 */
public record SessionUsage(
    /**
     * Session key
     */
    String sessionKey,
    
    /**
     * Message count
     */
    int messageCount,
    
    /**
     * Last activity timestamp
     */
    Long lastActivityAt
) {}
