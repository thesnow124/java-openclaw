package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

/**
 * Gateway event frame
 */
public record EventFrame(
    /**
     * Event name
     */
    String event,
    
    /**
     * Event data
     */
    Map<String, Object> data,
    
    /**
     * Event ID
     */
    String id
) implements GatewayFrame {
    @Override
    public String version() {
        return GatewayConstants.PROTOCOL_VERSION;
    }
}
