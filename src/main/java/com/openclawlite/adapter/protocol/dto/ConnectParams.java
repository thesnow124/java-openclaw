package com.openclawlite.adapter.protocol.dto;

/**
 * Parameters for connecting to Gateway
 */
public record ConnectParams(
    /**
     * API token for authentication
     */
    String token,
    
    /**
     * Protocol version
     */
    String version
) {
    /**
     * Create connect params with default version
     */
    public static ConnectParams withToken(String token) {
        return new ConnectParams(token, GatewayConstants.PROTOCOL_VERSION);
    }
}
