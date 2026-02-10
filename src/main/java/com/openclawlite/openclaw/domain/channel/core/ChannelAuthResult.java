package com.openclawlite.openclaw.domain.channel.core;

import java.time.Instant;

/**
 * Result of channel authentication
 */
public record ChannelAuthResult(
    /**
     * Whether authentication was successful
     */
    boolean success,
    
    /**
     * Authenticated user ID
     */
    String userId,
    
    /**
     * Authentication token
     */
    String token,
    
    /**
     * Error message if authentication failed
     */
    String error,
    
    /**
     * Expiration time
     */
    Instant expiresAt
) {
    /**
     * Create successful auth result
     */
    public static ChannelAuthResult success(String userId, String token) {
        return new ChannelAuthResult(true, userId, token, null, null);
    }
    
    /**
     * Create failed auth result
     */
    public static ChannelAuthResult failure(String error) {
        return new ChannelAuthResult(false, null, null, error, null);
    }
}
