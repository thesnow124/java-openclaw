package com.openclawlite.adapter.protocol.dto;

public record AgentsCreateResult(
    boolean ok,
    String agentId,
    String name,
    String workspace
) {
    public static AgentsCreateResult success(String agentId, String name, String workspace) {
        return new AgentsCreateResult(true, agentId, name, workspace);
    }
}
