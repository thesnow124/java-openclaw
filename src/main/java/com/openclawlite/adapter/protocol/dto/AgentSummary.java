package com.openclawlite.adapter.protocol.dto;

public record AgentSummary(
    String id,
    String name,
    AgentIdentityInfo identity
) {}
