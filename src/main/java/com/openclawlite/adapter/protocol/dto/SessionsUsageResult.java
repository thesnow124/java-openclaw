package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

/**
 * Sessions usage result
 */
public record SessionsUsageResult(
    /**
     * Total number of sessions
     */
    int totalSessions,
    
    /**
     * Total number of messages
     */
    int totalMessages,
    
    /**
     * Per-session usage breakdown
     */
    Map<String, SessionUsage> sessions
) {}
