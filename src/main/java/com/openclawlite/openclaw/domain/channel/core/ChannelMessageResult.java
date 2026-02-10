package com.openclawlite.openclaw.domain.channel.core;

/**
 * Result of sending a message
 */
public record ChannelMessageResult(
    boolean success,
    String messageId,
    String error
) {
    public static ChannelMessageResult success(String messageId) {
        return new ChannelMessageResult(true, messageId, null);
    }

    public static ChannelMessageResult failure(String error) {
        return new ChannelMessageResult(false, null, error);
    }
}
