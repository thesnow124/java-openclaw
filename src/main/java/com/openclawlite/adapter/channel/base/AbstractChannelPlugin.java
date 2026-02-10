package com.openclawlite.adapter.channel.base;

import com.openclawlite.common.enums.ChannelCapabilities;
import com.openclawlite.openclaw.domain.channel.core.ChannelConfigAdapter;
import com.openclawlite.openclaw.domain.channel.core.ChannelMeta;
import com.openclawlite.openclaw.domain.channel.core.ChannelPlugin;
import com.openclawlite.openclaw.domain.channel.core.ChannelStatusAdapter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for channel plugins
 * Provides common functionality for channel implementations
 */
public abstract class AbstractChannelPlugin implements ChannelPlugin {

    private final ChannelMeta metadata;
    protected volatile boolean running = false;

    public AbstractChannelPlugin(ChannelMeta metadata) {
        this.metadata = metadata;
    }

    @Override
    public ChannelMeta getMeta() {
        return metadata;
    }

    @Override
    public ChannelCapabilities getCapabilities() {
        return ChannelCapabilities.of(ChannelCapabilities.ChatType.DIRECT);
    }

    @Override
    public Optional<ChannelConfigAdapter> getConfigAdapter() {
        return Optional.of(new DefaultConfigAdapter());
    }

    @Override
    public Optional<ChannelStatusAdapter> getStatusAdapter() {
        return Optional.of(new DefaultStatusAdapter());
    }

    /**
     * Check if channel is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Default configuration adapter
     */
    private class DefaultConfigAdapter implements ChannelConfigAdapter {

        @Override
        public CompletableFuture<Boolean> validateConfig(String accountId, Map<String, Object> config) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> isConfigured(String accountId) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> isLinked(String accountId) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public String getSetupInstructions() {
            return "Setup instructions for " + metadata.label();
        }

        @Override
        public String[] getRequiredConfigKeys() {
            return new String[0];
        }
    }

    /**
     * Default status adapter
     */
    private class DefaultStatusAdapter implements ChannelStatusAdapter {

        @Override
        public CompletableFuture<AccountStatus> getStatus(String accountId) {
            return CompletableFuture.completedFuture(new AccountStatus(
                accountId,
                metadata.label(),
                true,
                true,
                true,
                running,
                running,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Map.of()
            ));
        }

        @Override
        public CompletableFuture<ChannelStatusIssue[]> getIssues(String accountId) {
            return CompletableFuture.completedFuture(new ChannelStatusIssue[0]);
        }

        @Override
        public CompletableFuture<Map<String, Object>> probe(String accountId) {
            return CompletableFuture.completedFuture(Map.of(
                "running", running,
                "channelId", metadata.id()
            ));
        }
    }
}
