package com.openclawlite.adapter.protocol.dto;

import java.util.List;

public record AgentsListResult(
    String defaultId,
    String mainKey,
    String scope,
    List<AgentSummary> agents
) {}
