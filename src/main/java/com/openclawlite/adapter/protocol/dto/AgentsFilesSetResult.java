package com.openclawlite.adapter.protocol.dto;

public record AgentsFilesSetResult(
    boolean ok,
    String agentId,
    String workspace,
    AgentFileEntry file
) {}
