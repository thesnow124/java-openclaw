package com.openclawlite.adapter.protocol.dto;

/**
 * Message summary
 */
public record MessageSummary(
    /**
     * Message ID
     */
    long id,
    
    /**
     * Message role (user/assistant/system)
     */
    String role,
    
    /**
     * Message content
     */
    String content,
    
    /**
     * Message timestamp
     */
    Long timestamp
) {}
