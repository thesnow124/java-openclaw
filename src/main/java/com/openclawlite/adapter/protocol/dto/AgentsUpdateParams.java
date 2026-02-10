package com.openclawlite.adapter.protocol.dto;

public record AgentsUpdateParams(
    String agentId,
    String name,
    String workspace,
    String model,
    String avatar
) {}
