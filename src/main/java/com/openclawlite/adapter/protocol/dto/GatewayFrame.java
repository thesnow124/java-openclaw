package com.openclawlite.adapter.protocol.dto;

/**
 * Gateway frame - base interface for all Gateway protocol frames
 */
public sealed interface GatewayFrame permits RequestFrame, ResponseFrame, EventFrame {
    /**
     * Protocol version
     */
    String version();
    
    /**
     * Frame ID
     */
    String id();
}
