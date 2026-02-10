package com.openclawlite.openclaw.domain.channel.impl.telegram;

import com.openclawlite.openclaw.domain.channel.core.ChannelGatewayAdapter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Telegram Gateway adapter
 * Handles Gateway protocol integration for Telegram
 */
public class TelegramGatewayAdapter implements ChannelGatewayAdapter {

    private final TelegramChannel channel;
    private volatile boolean running = false;

    public TelegramGatewayAdapter(TelegramChannel channel) {
        this.channel = channel;
    }

    @Override
    public CompletableFuture<Void> start(String accountId) {
        return CompletableFuture.runAsync(() -> {
            try {
                channel.start(Map.of("accountId", accountId));
                running = true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to start Telegram channel", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop(String accountId) {
        return CompletableFuture.runAsync(() -> {
            try {
                channel.stop();
                running = false;
            } catch (Exception e) {
                throw new RuntimeException("Failed to stop Telegram channel", e);
            }
        });
    }

    @Override
    public boolean isRunning(String accountId) {
        return running;
    }

    @Override
    public CompletableFuture<Boolean> isConnected(String accountId) {
        return CompletableFuture.completedFuture(running);
    }
}
