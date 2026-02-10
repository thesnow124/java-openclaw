package com.openclawlite.adapter.protocol.dto;

public record AgentFileEntry(
    String name,
    String path,
    boolean missing,
    Long size,
    Long updatedAtMs,
    String content
) {}
