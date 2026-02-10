package com.openclawlite.openclaw.domain.channel.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter for channel-specific authentication.
 */
public interface ChannelAuthAdapter {

    /**
     * Authenticate with the channel using credentials
     */
    CompletableFuture<Boolean> authenticate(String accountId, Map<String, String> credentials);

    /**
     * Refresh authentication token if needed
     */
    CompletableFuture<Boolean> refreshToken(String accountId);

    /**
     * Logout from the channel
     */
    CompletableFuture<Void> logout(String accountId);

    /**
     * Check if current authentication is valid
     */
    CompletableFuture<Boolean> isAuthenticated(String accountId);

    /**
     * Get authentication URL for OAuth flows
     */
    CompletableFuture<String> getAuthUrl(String accountId, String callbackUrl);
}
