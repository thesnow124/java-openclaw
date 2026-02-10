package com.openclawlite.openclaw.domain.channel.core;

import com.openclawlite.common.enums.ChannelCapabilities;
import java.util.Optional;

/**
 * Core interface for all channel plugins.
 * Each channel (WhatsApp, Telegram, Slack, etc.) must implement this interface.
 *
 * Based on OpenClaw TypeScript types.core.ts
 */
public interface ChannelPlugin {

    /**
     * Unique identifier for this channel (e.g., "whatsapp", "telegram", "slack")
     */
    String getId();

    /**
     * Metadata about this channel
     */
    ChannelMeta getMeta();

    /**
     * Capabilities supported by this channel
     */
    ChannelCapabilities getCapabilities();

    /**
     * Configuration adapter for setting up the channel
     */
    default Optional<ChannelConfigAdapter> getConfigAdapter() {
        return Optional.empty();
    }

    /**
     * Gateway adapter for running the channel as a gateway
     */
    default Optional<ChannelGatewayAdapter> getGatewayAdapter() {
        return Optional.empty();
    }

    /**
     * Messaging adapter for sending/receiving messages
     */
    default Optional<ChannelMessagingAdapter> getMessagingAdapter() {
        return Optional.empty();
    }

    /**
     * Outbound adapter for sending messages to the channel
     */
    default Optional<ChannelOutboundAdapter> getOutboundAdapter() {
        return Optional.empty();
    }

    /**
     * Authentication adapter for channel-specific auth
     */
    default Optional<ChannelAuthAdapter> getAuthAdapter() {
        return Optional.empty();
    }

    /**
     * Status adapter for checking channel health
     */
    default Optional<ChannelStatusAdapter> getStatusAdapter() {
        return Optional.empty();
    }
}
