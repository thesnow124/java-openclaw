package com.openclawlite.adapter.protocol.dto;

/**
 * Sessions delete params
 */
public record SessionsDeleteParams(
    /**
     * Session key to delete
     */
    String sessionKey
) {}
