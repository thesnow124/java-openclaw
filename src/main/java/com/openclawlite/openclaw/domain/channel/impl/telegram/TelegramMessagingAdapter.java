package com.openclawlite.openclaw.domain.channel.impl.telegram;

import com.openclawlite.openclaw.domain.channel.core.ChannelMessage;
import com.openclawlite.openclaw.domain.channel.core.ChannelMessagingAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Telegram messaging adapter
 * Handles receiving messages from Telegram
 */
public class TelegramMessagingAdapter implements ChannelMessagingAdapter {

    private final TelegramChannel channel;
    private final TelegramMessageConverter converter;

    public TelegramMessagingAdapter(TelegramChannel channel) {
        this.channel = channel;
        this.converter = new TelegramMessageConverter();
    }

    @Override
    public Flux<ChannelMessage> getIncomingMessages(String accountId) {
        return Flux.interval(Duration.ofMillis(100))
            .map(tick -> {
                try {
                    ChannelMessage msg = channel.pollMessage(100);
                    return msg != null ? msg : null;
                } catch (InterruptedException e) {
                    return null;
                }
            })
            .filter(msg -> msg != null);
    }

    @Override
    public Mono<Void> startListening(String accountId) {
        return Mono.fromRunnable(() -> {
            // Listening is handled by the channel's receiver thread
        });
    }

    @Override
    public Mono<Void> stopListening(String accountId) {
        return Mono.fromRunnable(() -> {
            // Handled by channel stop
        });
    }

    /**
     * Get message converter
     */
    public TelegramMessageConverter getConverter() {
        return converter;
    }

    /**
     * Convert raw Telegram message to internal format
     */
    public ChannelMessage convertMessage(Object rawMessage) {
        return converter.toInternal(rawMessage);
    }
}
