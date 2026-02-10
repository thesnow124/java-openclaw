package com.openclawlite.openclaw.application.agent.guard;

import java.util.Optional;

/**
 * Result of repairing session tool results
 */
public record RepairResult(
    /**
     * Whether the repair was successful
     */
    boolean success,

    /**
     * Number of tool results repaired
     */
    int repairedCount,

    /**
     * Number of tool results removed
     */
    int removedCount,

    /**
     * Error message if repair failed
     */
    Optional<String> error
) {
    /**
     * Create a successful repair result
     */
    public static RepairResult success(int repairedCount, int removedCount) {
        return new RepairResult(true, repairedCount, removedCount, Optional.empty());
    }

    /**
     * Create a failed repair result
     */
    public static RepairResult failure(String error) {
        return new RepairResult(false, 0, 0, Optional.of(error));
    }
}
