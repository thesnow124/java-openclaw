package com.openclawlite.adapter.protocol.dto;

/**
 * Response to hello message
 */
public record HelloOk(
    /**
     * Protocol version
     */
    String version,
    
    /**
     * Server information
     */
    String server
) {}
