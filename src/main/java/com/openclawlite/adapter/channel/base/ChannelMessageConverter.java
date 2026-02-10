package com.openclawlite.adapter.channel.base;

import com.openclawlite.common.enums.ChannelCapabilities;
import com.openclawlite.openclaw.domain.channel.core.ChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Base class for message converters
 * Handles conversion between channel-specific formats and internal format
 */
public abstract class ChannelMessageConverter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Convert channel message to internal format
     */
    public abstract ChannelMessage toInternal(Object rawMessage);

    /**
     * Convert internal message to channel format
     */
    public abstract Object fromInternal(ChannelMessage message);

    /**
     * Extract sender ID from raw message
     */
    public abstract String extractSenderId(Object rawMessage);

    /**
     * Extract chat ID from raw message
     */
    public abstract String extractChatId(Object rawMessage);

    /**
     * Extract timestamp from raw message
     */
    protected long extractTimestamp(Object rawMessage) {
        return Instant.now().toEpochMilli();
    }

    /**
     * Extract text content from raw message
     */
    protected String extractText(Object rawMessage) {
        return "";
    }

    /**
     * Create basic channel message
     */
    protected ChannelMessage createMessage(
            String channelId,
            String senderId,
            String chatId,
            String content) {

        ChannelMessage msg = new ChannelMessage();
        msg.setMessageId(java.util.UUID.randomUUID().toString());
        msg.setChannelId(channelId);
        msg.setSenderId(senderId);
        msg.setChatId(chatId);
        msg.setChatType(determineChatType(chatId));
        msg.setText(content);
        msg.setTimestamp(java.time.ZonedDateTime.now());
        msg.setMetadata(java.util.Map.of());
        return msg;
    }

    /**
     * Determine chat type from ID or message
     */
    protected ChannelCapabilities.ChatType determineChatType(String chatId) {
        // Default to direct message
        return ChannelCapabilities.ChatType.DIRECT;
    }

    /**
     * Extract metadata from raw message
     */
    protected java.util.Map<String, Object> extractMetadata(Object rawMessage) {
        return java.util.Map.of();
    }
}
