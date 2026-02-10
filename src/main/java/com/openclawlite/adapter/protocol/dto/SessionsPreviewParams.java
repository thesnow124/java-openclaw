package com.openclawlite.adapter.protocol.dto;

/**
 * Parameters for previewing sessions
 */
public record SessionsPreviewParams(
    /**
     * Session keys to preview
     */
    String[] sessionKeys
) {}
