package com.openclawlite.adapter.protocol.dto;

/**
 * Parameters for listing sessions
 */
public record SessionsListParams(
    /**
     * Channel ID filter
     */
    String channelId,
    
    /**
     * Account ID filter
     */
    String accountId,
    
    /**
     * Limit number of results
     */
    int limit,
    
    /**
     * Offset for pagination
     */
    int offset
) {
    /**
     * Create params with defaults
     */
    public static SessionsListParams create() {
        return new SessionsListParams(null, null, 100, 0);
    }
}
