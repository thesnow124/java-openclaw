package com.openclawlite.openclaw.domain.channel.core;

import reactor.core.publisher.Flux;

/**
 * Adapter for receiving messages from a channel.
 * Provides a reactive stream of incoming messages.
 */
public interface ChannelMessagingAdapter {

    /**
     * Get a reactive stream of incoming messages for the given account
     */
    Flux<ChannelMessage> getIncomingMessages(String accountId);

    /**
     * Start listening for messages
     */
    reactor.core.publisher.Mono<Void> startListening(String accountId);

    /**
     * Stop listening for messages
     */
    reactor.core.publisher.Mono<Void> stopListening(String accountId);
}
