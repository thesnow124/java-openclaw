package com.openclawlite.openclaw.domain.channel.core;

import com.openclawlite.common.enums.ChannelCapabilities;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized message format for all channels.
 */
public class ChannelMessage {
    private String messageId;
    private String channelId;
    private String accountId;
    private ChannelCapabilities.ChatType chatType;
    private String chatId;
    private String senderId;
    private String senderName;
    private String senderUsername;
    private ZonedDateTime timestamp;
    private String text;
    private List<MediaAttachment> media;
    private MessageContext context;
    private Map<String, Object> metadata;

    // Constructors
    public ChannelMessage() {}

    public ChannelMessage(String channelId, String accountId, String chatId,
                         ChannelCapabilities.ChatType chatType, String text) {
        this.channelId = channelId;
        this.accountId = accountId;
        this.chatId = chatId;
        this.chatType = chatType;
        this.text = text;
        this.timestamp = ZonedDateTime.now();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public ChannelCapabilities.ChatType getChatType() { return chatType; }
    public void setChatType(ChannelCapabilities.ChatType chatType) { this.chatType = chatType; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<MediaAttachment> getMedia() { return media; }
    public void setMedia(List<MediaAttachment> media) { this.media = media; }

    public MessageContext getContext() { return context; }
    public void setContext(MessageContext context) { this.context = context; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    /**
     * Media attachment in a message
     */
    public static class MediaAttachment {
        private String mimeType;
        private String url;
        private byte[] data;
        private String filename;
        private long size;

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public byte[] getData() { return data; }
        public void setData(byte[] data) { this.data = data; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }

    /**
     * Message context for replies, threads, etc.
     */
    public static class MessageContext {
        private String replyToMessageId;
        private String threadId;
        private String messageThreadId;
        private Map<String, Object> metadata;

        public String getReplyToMessageId() { return replyToMessageId; }
        public void setReplyToMessageId(String replyToMessageId) { this.replyToMessageId = replyToMessageId; }

        public String getThreadId() { return threadId; }
        public void setThreadId(String threadId) { this.threadId = threadId; }

        public String getMessageThreadId() { return messageThreadId; }
        public void setMessageThreadId(String messageThreadId) { this.messageThreadId = messageThreadId; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
