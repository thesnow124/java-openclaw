package com.openclawlite.adapter.protocol.dto;

public record AgentsCreateParams(
    String name,
    String workspace,
    String emoji,
    String avatar
) {}
