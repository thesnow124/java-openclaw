package com.openclawlite.adapter.protocol.dto;

/**
 * Result of previewing sessions
 */
public record SessionsPreviewResult(
    /**
     * Array of session previews
     */
    SessionPreview[] sessions
) {}
