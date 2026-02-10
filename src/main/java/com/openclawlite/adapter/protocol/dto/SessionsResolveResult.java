package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

/**
 * Sessions resolve result
 */
public record SessionsResolveResult(
    /**
     * Session key
     */
    String sessionKey,
    
    /**
     * Agent ID
     */
    String agentId,
    
    /**
     * Session metadata
     */
    Map<String, Object> metadata
) {}
