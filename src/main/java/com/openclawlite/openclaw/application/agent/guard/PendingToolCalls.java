package com.openclawlite.openclaw.application.agent.guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pending tool calls for active sessions
 * Tracks tool calls that haven't received responses yet
 */
@Component
public class PendingToolCalls {

    private static final Logger log = LoggerFactory.getLogger(PendingToolCalls.class);

    /**
     * Map of session key to pending tool call IDs
     */
    private final Map<String, Map<String, ToolCallInfo>> pendingCalls = new ConcurrentHashMap<>();

    /**
     * Record for tool call information
     */
    public record ToolCallInfo(
        String toolCallId,
        String toolName,
        long timestamp,
        int timeoutSeconds
    ) {
        public boolean isExpired() {
            long elapsed = System.currentTimeMillis() - timestamp;
            return elapsed > timeoutSeconds * 1000L;
        }
    }

    /**
     * Register a pending tool call
     */
    public void register(String sessionKey, String toolCallId, String toolName, int timeoutSeconds) {
        pendingCalls.computeIfAbsent(sessionKey, k -> new ConcurrentHashMap<>())
            .put(toolCallId, new ToolCallInfo(toolCallId, toolName, System.currentTimeMillis(), timeoutSeconds));

        log.debug("Registered pending tool call: session={}, toolCallId={}, tool={}",
                  sessionKey, toolCallId, toolName);
    }

    /**
     * Complete a tool call
     */
    public void complete(String sessionKey, String toolCallId) {
        Map<String, ToolCallInfo> sessionCalls = pendingCalls.get(sessionKey);
        if (sessionCalls != null) {
            ToolCallInfo removed = sessionCalls.remove(toolCallId);
            if (removed != null) {
                log.debug("Completed tool call: session={}, toolCallId={}, tool={}",
                          sessionKey, toolCallId, removed.toolName());
            }

            // Clean up empty sessions
            if (sessionCalls.isEmpty()) {
                pendingCalls.remove(sessionKey);
            }
        }
    }

    /**
     * Get all pending tool calls for a session
     */
    public Map<String, ToolCallInfo> getPendingCalls(String sessionKey) {
        Map<String, ToolCallInfo> calls = pendingCalls.get(sessionKey);
        return calls != null ? Map.copyOf(calls) : Map.of();
    }

    /**
     * Check if a tool call is pending
     */
    public boolean isPending(String sessionKey, String toolCallId) {
        Map<String, ToolCallInfo> sessionCalls = pendingCalls.get(sessionKey);
        if (sessionCalls == null) {
            return false;
        }

        ToolCallInfo info = sessionCalls.get(toolCallId);
        return info != null && !info.isExpired();
    }

    /**
     * Clean up expired tool calls
     */
    public int cleanupExpired() {
        int removed = 0;

        for (Map.Entry<String, Map<String, ToolCallInfo>> sessionEntry : pendingCalls.entrySet()) {
            String sessionKey = sessionEntry.getKey();
            Map<String, ToolCallInfo> calls = sessionEntry.getValue();

            calls.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    log.warn("Expired tool call removed: session={}, toolCallId={}, tool={}",
                             sessionKey, entry.getKey(), entry.getValue().toolName());
                    return true;
                }
                return false;
            });

            if (calls.isEmpty()) {
                pendingCalls.remove(sessionKey);
            }

            removed += calls.size();
        }

        return removed;
    }

    /**
     * Clear all pending calls for a session
     */
    public void clearSession(String sessionKey) {
        Map<String, ToolCallInfo> removed = pendingCalls.remove(sessionKey);
        if (removed != null) {
            log.info("Cleared {} pending tool calls for session: {}", removed.size(), sessionKey);
        }
    }

    /**
     * Generate synthetic results for pending tool calls
     * Used when a session needs to recover from missing tool results
     */
    public Map<String, SyntheticResult> generateSyntheticResults(String sessionKey) {
        Map<String, ToolCallInfo> calls = getPendingCalls(sessionKey);
        Map<String, SyntheticResult> results = new ConcurrentHashMap<>();

        for (ToolCallInfo info : calls.values()) {
            String syntheticContent = String.format(
                "[Previous invocation of %s tool expired or was interrupted. " +
                "The tool may need to be called again to complete the operation.]",
                info.toolName()
            );

            results.put(info.toolCallId(), new SyntheticResult(
                info.toolCallId(),
                info.toolName(),
                syntheticContent,
                true
            ));

            log.info("Generated synthetic result for tool call: session={}, toolCallId={}, tool={}",
                    sessionKey, info.toolCallId(), info.toolName());
        }

        // Clear the pending calls after generating synthetic results
        clearSession(sessionKey);

        return results;
    }

    /**
     * Record for synthetic tool results
     */
    public record SyntheticResult(
        String toolCallId,
        String toolName,
        String content,
        boolean isSynthetic
    ) {}

    /**
     * Get statistics about pending calls
     */
    public PendingStats getStats() {
        int totalSessions = pendingCalls.size();
        int totalCalls = pendingCalls.values().stream()
            .mapToInt(Map::size)
            .sum();

        int expiredCalls = (int) pendingCalls.values().stream()
            .flatMap(m -> m.values().stream())
            .filter(ToolCallInfo::isExpired)
            .count();

        return new PendingStats(totalSessions, totalCalls, expiredCalls);
    }

    /**
     * Statistics record
     */
    public record PendingStats(
        int totalSessions,
        int totalCalls,
        int expiredCalls
    ) {}
}
