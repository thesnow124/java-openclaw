package com.openclawlite.openclaw.domain.channel.impl.telegram;

import com.openclawlite.openclaw.domain.channel.core.ChannelConfigAdapter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Telegram configuration adapter
 */
public class TelegramConfigAdapter implements ChannelConfigAdapter {

    @Override
    public CompletableFuture<Boolean> validateConfig(String accountId, Map<String, Object> config) {
        String botToken = config != null ? (String) config.get("botToken") : null;

        if (botToken == null || botToken.trim().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        boolean valid = botToken.matches("\\d+:[A-Za-z0-9_-]+");
        return CompletableFuture.completedFuture(valid);
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
        return """
            1. Open Telegram and search for @BotFather
            2. Send /newbot to create a new bot
            3. Follow the prompts to choose a name and username
            4. Copy the bot token (format: 123456789:ABCdefGHIjklMNOpqrsTUVwxyz)
            5. Paste the token in the configuration
            """;
    }

    @Override
    public String[] getRequiredConfigKeys() {
        return new String[]{"botToken"};
    }
}
