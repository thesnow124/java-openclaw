package com.openclawlite.openclaw.domain.channel.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter for channel configuration setup.
 * Handles authentication, account linking, and configuration validation.
 */
public interface ChannelConfigAdapter {

    /**
     * Validate configuration for this channel
     */
    CompletableFuture<Boolean> validateConfig(String accountId, Map<String, Object> config);

    /**
     * Check if account is properly configured
     */
    CompletableFuture<Boolean> isConfigured(String accountId);

    /**
     * Check if account is linked (authenticated)
     */
    CompletableFuture<Boolean> isLinked(String accountId);

    /**
     * Get setup instructions for this channel
     */
    String getSetupInstructions();

    /**
     * Get required configuration keys
     */
    String[] getRequiredConfigKeys();
}
