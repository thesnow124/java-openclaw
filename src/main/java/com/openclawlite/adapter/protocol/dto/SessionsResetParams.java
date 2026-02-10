package com.openclawlite.adapter.protocol.dto;

/**
 * Sessions reset params
 */
public record SessionsResetParams(
    /**
     * Session key to reset
     */
    String sessionKey,
    
    /**
     * New agent ID to use
     */
    String agentId
) {}
