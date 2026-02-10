package com.openclawlite.adapter.protocol.dto;

/**
 * Sessions reset result
 */
public record SessionsResetResult(
    /**
     * Whether reset was successful
     */
    boolean ok,
    
    /**
     * Session key
     */
    String sessionKey
) {}
