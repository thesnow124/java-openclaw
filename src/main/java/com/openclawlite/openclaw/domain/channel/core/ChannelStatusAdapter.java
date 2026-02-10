package com.openclawlite.openclaw.domain.channel.core;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter for checking channel health and status.
 */
public interface ChannelStatusAdapter {

    /**
     * Overall status snapshot for an account
     */
    record AccountStatus(
        String accountId,
        String name,
        boolean enabled,
        boolean configured,
        boolean linked,
        boolean running,
        boolean connected,
        int reconnectAttempts,
        Instant lastConnectedAt,
        DisconnectInfo lastDisconnect,
        Instant lastMessageAt,
        Instant lastEventAt,
        String lastError,
        Instant lastStartAt,
        Instant lastStopAt,
        Instant lastInboundAt,
        Instant lastOutboundAt,
        Map<String, Object> metadata
    ) {}

    /**
     * Information about last disconnect
     */
    record DisconnectInfo(
        Instant at,
        Integer status,
        String error,
        boolean loggedOut
    ) {}

    /**
     * Get current status for an account
     */
    CompletableFuture<AccountStatus> getStatus(String accountId);

    /**
     * Check if there are any issues with the channel
     */
    CompletableFuture<ChannelStatusIssue[]> getIssues(String accountId);

    /**
     * Detailed probe of channel status (may make API calls)
     */
    CompletableFuture<Map<String, Object>> probe(String accountId);

    /**
     * Status issue description
     */
    record ChannelStatusIssue(
        String channel,
        String accountId,
        String kind,  // "intent", "permissions", "config", "auth", "runtime"
        String message,
        String fix
    ) {}
}
