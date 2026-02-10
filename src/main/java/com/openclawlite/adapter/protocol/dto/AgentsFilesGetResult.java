package com.openclawlite.adapter.protocol.dto;

public record AgentsFilesGetResult(
    String agentId,
    String workspace,
    AgentFileEntry file
) {}
