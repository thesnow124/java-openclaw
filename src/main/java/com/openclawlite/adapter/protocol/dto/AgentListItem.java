package com.openclawlite.adapter.protocol.dto;

import java.time.Instant;

/**
 * Agent list item
 */
public record AgentListItem(
    /**
     * Agent ID
     */
    String id,
    
    /**
     * Agent name
     */
    String name,
    
    /**
     * Agent emoji
     */
    String emoji,
    
    /**
     * Agent avatar URL
     */
    String avatar,
    
    /**
     * Model used by this agent
     */
    String model,
    
    /**
     * Creation timestamp
     */
    Instant createdAt,
    
    /**
     * Last update timestamp
     */
    Instant updatedAt
) {}
