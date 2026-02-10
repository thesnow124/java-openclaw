package com.openclawlite.adapter.protocol.dto;

public record AgentsDeleteResult(
    boolean ok,
    String agentId,
    int removedBindings
) {
    public static AgentsDeleteResult success(String agentId, int removedBindings) {
        return new AgentsDeleteResult(true, agentId, removedBindings);
    }
}
