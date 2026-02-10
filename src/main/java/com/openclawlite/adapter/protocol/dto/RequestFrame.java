package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

/**
 * Gateway request frame
 */
public record RequestFrame(
    /**
     * Method name
     */
    String method,
    
    /**
     * Method parameters
     */
    Map<String, Object> params,
    
    /**
     * Request ID
     */
    String id
) implements GatewayFrame {
    @Override
    public String version() {
        return GatewayConstants.PROTOCOL_VERSION;
    }
}
