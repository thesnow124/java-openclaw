package com.openclawlite.adapter.protocol.dto;

public record AgentsUpdateResult(boolean ok, String agentId) {
    public static AgentsUpdateResult success(String agentId) {
        return new AgentsUpdateResult(true, agentId);
    }
}
