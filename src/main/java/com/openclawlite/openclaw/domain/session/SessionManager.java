package com.openclawlite.openclaw.domain.session;

import com.openclawlite.common.enums.ChannelCapabilities;
import com.openclawlite.openclaw.domain.channel.core.ChannelMessage;
import com.openclawlite.openclaw.infrastructure.persistence.session.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Manages user sessions across channels.
 * Handles session creation, retrieval, and message history.
 */
@Service
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final SessionRepository repository;

    public SessionManager(SessionRepository repository) {
        this.repository = repository;
    }

    /**
     * Get or create a session for a channel message
     */
    public Mono<Session> getOrCreateSession(ChannelMessage message) {
        return Mono.fromCallable(() -> {
            String sessionKey = buildSessionKey(message);
            var existing = repository.findByKey(sessionKey);

            if (existing.isPresent()) {
                log.debug("Found existing session: key={}", sessionKey);
                return toDomainSession(existing.get());
            }

            // Create new session
            log.info("Creating new session: key={}, channel={}, chat={}",
                sessionKey, message.getChannelId(), message.getChatId());

            var newSession = new SessionRepository.Session(
                sessionKey,
                message.getChannelId(),
                message.getAccountId(),
                message.getChatId(),
                message.getChatType(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                message.getMetadata(),
                null
            );

            repository.save(newSession);
            return toDomainSession(newSession);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get session by key
     */
    public Mono<Session> getSession(String sessionKey) {
        return Mono.fromCallable(() -> {
            log.debug("Retrieving session: key={}", sessionKey);
            var session = repository.findByKey(sessionKey);
            return session.map(this::toDomainSession).orElse(null);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Update session activity
     */
    public Mono<Void> updateActivity(String sessionKey) {
        return Mono.fromRunnable(() -> {
            repository.updateActivity(sessionKey);
            log.debug("Updated activity: key={}", sessionKey);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Get message history for a session
     */
    public Flux<SessionMessage> getMessages(String sessionKey, int limit) {
        return Flux.defer(() -> {
            log.debug("Getting messages: key={}, limit={}", sessionKey, limit);
            List<SessionRepository.SessionMessage> messages = repository.findMessages(sessionKey, limit);
            return Flux.fromIterable(messages)
                .map(this::toDomainSessionMessage);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Add a message to session history
     */
    public Mono<SessionMessage> addMessage(String sessionKey, SessionMessage message) {
        return Mono.fromCallable(() -> {
            log.debug("Adding message: key={}, role={}", sessionKey, message.getRole());

            var repoMessage = new SessionRepository.SessionMessage(
                0,  // ID will be generated
                sessionKey,
                message.getRole(),
                message.getContent(),
                message.getToolCall(),
                message.getTimestamp()
            );

            var saved = repository.saveMessage(repoMessage);
            return toDomainSessionMessage(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Delete a session
     */
    public Mono<Void> deleteSession(String sessionKey) {
        return Mono.fromRunnable(() -> {
            repository.deleteMessages(sessionKey);
            repository.delete(sessionKey);
            log.info("Deleted session: key={}", sessionKey);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Get all sessions for a channel
     */
    public Flux<Session> getSessionsByChannel(String channelId) {
        return Flux.defer(() -> {
            List<SessionRepository.Session> sessions = repository.findByChannel(channelId);
            return Flux.fromIterable(sessions)
                .map(this::toDomainSession);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get all sessions for an account
     */
    public Flux<Session> getSessionsByAccount(String channelId, String accountId) {
        return Flux.defer(() -> {
            List<SessionRepository.Session> sessions = repository.findByAccount(channelId, accountId);
            return Flux.fromIterable(sessions)
                .map(this::toDomainSession);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Build session key from message
     */
    private String buildSessionKey(ChannelMessage message) {
        return String.format("%s:%s:%s",
            message.getChannelId(),
            message.getAccountId(),
            message.getChatId());
    }

    /**
     * Convert repository session to domain session
     */
    private Session toDomainSession(SessionRepository.Session repo) {
        return new Session(
            repo.sessionKey(),
            repo.channelId(),
            repo.accountId(),
            repo.chatId(),
            repo.chatType(),
            repo.createdAt(),
            repo.updatedAt(),
            repo.lastActivityAt(),
            repo.metadata()
        );
    }

    /**
     * Convert repository message to domain message
     */
    private SessionMessage toDomainSessionMessage(SessionRepository.SessionMessage repo) {
        return new SessionMessage(
            repo.id(),
            repo.sessionKey(),
            repo.role(),
            repo.content(),
            repo.toolCall(),
            repo.timestamp()
        );
    }

    // Domain data classes

    public static class Session {
        private final String sessionKey;
        private final String channelId;
        private final String accountId;
        private final String chatId;
        private final ChannelCapabilities.ChatType chatType;
        private final ZonedDateTime createdAt;
        private final ZonedDateTime updatedAt;
        private final ZonedDateTime lastActivityAt;
        private final Map<String, Object> metadata;

        public Session(String sessionKey, String channelId, String accountId,
                      String chatId, ChannelCapabilities.ChatType chatType,
                      ZonedDateTime createdAt, ZonedDateTime updatedAt,
                      ZonedDateTime lastActivityAt, Map<String, Object> metadata) {
            this.sessionKey = sessionKey;
            this.channelId = channelId;
            this.accountId = accountId;
            this.chatId = chatId;
            this.chatType = chatType;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.lastActivityAt = lastActivityAt;
            this.metadata = metadata;
        }

        public String getSessionKey() { return sessionKey; }
        public String getChannelId() { return channelId; }
        public String getAccountId() { return accountId; }
        public String getChatId() { return chatId; }
        public ChannelCapabilities.ChatType getChatType() { return chatType; }
        public ZonedDateTime getCreatedAt() { return createdAt; }
        public ZonedDateTime getUpdatedAt() { return updatedAt; }
        public ZonedDateTime getLastActivityAt() { return lastActivityAt; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    public static class SessionMessage {
        private final long id;
        private final String sessionKey;
        private final String role;
        private final String content;
        private final Map<String, Object> toolCall;
        private final ZonedDateTime timestamp;

        public SessionMessage(long id, String sessionKey, String role,
                             String content, Map<String, Object> toolCall,
                             ZonedDateTime timestamp) {
            this.id = id;
            this.sessionKey = sessionKey;
            this.role = role;
            this.content = content;
            this.toolCall = toolCall;
            this.timestamp = timestamp;
        }

        public long getId() { return id; }
        public String getSessionKey() { return sessionKey; }
        public String getRole() { return role; }
        public String getContent() { return content; }
        public Map<String, Object> getToolCall() { return toolCall; }
        public ZonedDateTime getTimestamp() { return timestamp; }
    }
}
