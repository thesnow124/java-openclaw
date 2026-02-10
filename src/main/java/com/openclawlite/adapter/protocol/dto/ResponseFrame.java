package com.openclawlite.adapter.protocol.dto;

import com.openclawlite.common.dto.ErrorShape;

/**
 * Gateway response frame
 */
public record ResponseFrame(
    /**
     * Whether the request was successful
     */
    boolean ok,
    
    /**
     * Result data (if successful)
     */
    Object result,
    
    /**
     * Error information (if failed)
     */
    ErrorShape error,
    
    /**
     * Response ID (matches request ID)
     */
    String id
) implements GatewayFrame {
    @Override
    public String version() {
        return GatewayConstants.PROTOCOL_VERSION;
    }
    
    /**
     * Create a successful response
     */
    public static ResponseFrame success(Object result, String id) {
        return new ResponseFrame(true, result, null, id);
    }
    
    /**
     * Create an error response
     */
    public static ResponseFrame error(ErrorShape error, String id) {
        return new ResponseFrame(false, null, error, id);
    }
}
