package com.openclawlite.openclaw.domain.channel.core;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter for running a channel as a gateway server.
 * Handles starting/stopping the channel and managing connection lifecycle.
 */
public interface ChannelGatewayAdapter {

    /**
     * Start the channel gateway for the given account
     */
    CompletableFuture<Void> start(String accountId);

    /**
     * Stop the channel gateway for the given account
     */
    CompletableFuture<Void> stop(String accountId);

    /**
     * Check if the channel is running
     */
    boolean isRunning(String accountId);

    /**
     * Check if the channel is connected
     */
    CompletableFuture<Boolean> isConnected(String accountId);
}
