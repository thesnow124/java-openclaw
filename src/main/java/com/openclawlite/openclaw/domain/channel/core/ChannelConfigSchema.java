package com.openclawlite.openclaw.domain.channel.core;

import java.util.Map;

/**
 * Channel configuration schema
 */
public record ChannelConfigSchema(
    /**
     * Configuration schema as JSON Schema
     */
    Map<String, Object> schema,
    
    /**
     * Example configuration
     */
    Map<String, Object> example,
    
    /**
     * Required configuration fields
     */
    String[] required
) {
    /**
     * Create empty schema
     */
    public static ChannelConfigSchema empty() {
        return new ChannelConfigSchema(Map.of(), Map.of(), new String[0]);
    }
}
