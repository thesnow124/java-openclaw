package com.openclawlite.adapter.protocol.dto;

import java.util.List;

/**
 * Connection parameters (full version with role and scopes)
 */
public record ConnectParamsFull(
    /**
     * Protocol version
     */
    String version,
    
    /**
     * Connection role
     */
    String role,
    
    /**
     * Authorization scopes
     */
    List<String> scopes
) {
    public ConnectParamsFull {
        if (version == null) {
            version = GatewayConstants.PROTOCOL_VERSION;
        }
        if (role == null) {
            role = "operator";
        }
        if (scopes == null) {
            scopes = List.of();
        }
    }
}
