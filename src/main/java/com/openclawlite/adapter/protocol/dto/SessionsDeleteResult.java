package com.openclawlite.adapter.protocol.dto;

/**
 * Sessions delete result
 */
public record SessionsDeleteResult(
    /**
     * Whether deletion was successful
     */
    boolean ok,
    
    /**
     * Deleted session key
     */
    String sessionKey
) {}
