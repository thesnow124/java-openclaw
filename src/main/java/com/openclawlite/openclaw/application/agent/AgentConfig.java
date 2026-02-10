package com.openclawlite.openclaw.application.agent;

/**
 * Agent configuration record
 * Represents an agent's configuration stored in the database
 */
public record AgentConfig(
    String agentId,
    String name,
    String workspace,
    String model,
    String avatar,
    long createdAt,
    long updatedAt
) {
    /**
     * Create a new AgentConfig with default timestamps
     */
    public static AgentConfig create(String agentId, String name, String workspace, String model, String avatar) {
        long now = System.currentTimeMillis();
        return new AgentConfig(agentId, name, workspace, model, avatar, now, now);
    }

    /**
     * Create with updated timestamp
     */
    public AgentConfig withUpdatedTimestamp() {
        return new AgentConfig(
            agentId,
            name,
            workspace,
            model,
            avatar,
            createdAt,
            System.currentTimeMillis()
        );
    }
}
