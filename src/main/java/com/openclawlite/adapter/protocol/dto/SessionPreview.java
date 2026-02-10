package com.openclawlite.adapter.protocol.dto;

import java.time.Instant;

/**
 * Preview of a session
 */
public record SessionPreview(
    /**
     * Session key
     */
    String sessionKey,
    
    /**
     * Channel ID
     */
    String channelId,
    
    /**
     * Account ID
     */
    String accountId,
    
    /**
     * Chat ID
     */
    String chatId,
    
    /**
     * Last activity timestamp
     */
    Instant lastActivityAt,
    
    /**
     * Preview of recent messages
     */
    String[] preview
) {}
