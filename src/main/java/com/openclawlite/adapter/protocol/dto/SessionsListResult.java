package com.openclawlite.adapter.protocol.dto;

/**
 * Result of listing sessions
 */
public record SessionsListResult(
    /**
     * List of session summaries
     */
    SessionSummary[] sessions,
    
    /**
     * Total count
     */
    int total
) {}
