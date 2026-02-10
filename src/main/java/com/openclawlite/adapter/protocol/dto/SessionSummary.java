package com.openclawlite.adapter.protocol.dto;

import java.time.Instant;

/**
 * Summary of a session
 */
public record SessionSummary(
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
     * Chat type
     */
    String chatType,
    
    /**
     * Last activity timestamp
     */
    Instant lastActivityAt,
    
    /**
     * Message count
     */
    int messageCount
) {}
