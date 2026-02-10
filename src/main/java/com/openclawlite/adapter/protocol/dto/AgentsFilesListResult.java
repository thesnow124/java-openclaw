package com.openclawlite.adapter.protocol.dto;

import java.util.List;

public record AgentsFilesListResult(
    String agentId,
    String workspace,
    List<AgentFileEntry> files
) {}
