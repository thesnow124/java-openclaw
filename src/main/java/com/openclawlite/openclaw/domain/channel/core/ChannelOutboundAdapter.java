package com.openclawlite.openclaw.domain.channel.core;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter for sending messages to a channel.
 */
public interface ChannelOutboundAdapter {

    /**
     * Send a text message
     */
    CompletableFuture<ChannelMessageResult> sendTextMessage(
        String accountId,
        String chatId,
        String text
    );

    /**
     * Send a message with media attachment
     */
    CompletableFuture<ChannelMessageResult> sendMediaMessage(
        String accountId,
        String chatId,
        String mimeType,
        byte[] data,
        String caption
    );

    /**
     * Send typing indicator
     */
    CompletableFuture<Void> sendTypingIndicator(
        String accountId,
        String chatId,
        boolean isTyping
    );

    /**
     * Result of sending a message
     */
    record ChannelMessageResult(
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
}
