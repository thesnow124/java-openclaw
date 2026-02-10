package com.openclawlite.openclaw.domain.channel.core;

import java.time.Instant;

/**
 * Result of sending a message through a channel
 */
public record ChannelSendResult(
    /**
     * Whether the message was sent successfully
     */
    boolean success,
    
    /**
     * The message ID assigned by the channel
     */
    String messageId,
    
    /**
     * Error message if sending failed
     */
    String error,
    
    /**
     * Timestamp when the message was sent
     */
    Instant timestamp
) {
    /**
     * Create a successful result
     */
    public static ChannelSendResult success(String messageId) {
        return new ChannelSendResult(true, messageId, null, Instant.now());
    }
    
    /**
     * Create a failed result
     */
    public static ChannelSendResult failure(String error) {
        return new ChannelSendResult(false, null, error, Instant.now());
    }
}
