package com.openclawlite.adapter.protocol.dto;

/**
 * Hello response (full version with heartbeat)
 */
public record HelloOkFull(
    /**
     * Protocol version
     */
    String version,
    
    /**
     * Heartbeat interval in milliseconds
     */
    int heartbeatIntervalMs
) {}
