package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

/**
 * Sessions patch result
 */
public record SessionsPatchResult(
    /**
     * Whether patch was successful
     */
    boolean ok,
    
    /**
     * Session key
     */
    String sessionKey,
    
    /**
     * Updated metadata
     */
    Map<String, Object> metadata
) {}
