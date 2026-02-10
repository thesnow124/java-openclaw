package com.openclawlite.common.dto;

import com.openclawlite.adapter.protocol.dto.GatewayConstants;

/**
 * Error shape for Gateway responses
 */
public record ErrorShape(
    String code,
    String message
) {
    public static ErrorShape invalidRequest(String message) {
        return new ErrorShape(GatewayConstants.ErrorCodes.INVALID_REQUEST, message);
    }

    public static ErrorShape unauthorized(String message) {
        return new ErrorShape(GatewayConstants.ErrorCodes.UNAUTHORIZED, message);
    }

    public static ErrorShape forbidden(String message) {
        return new ErrorShape(GatewayConstants.ErrorCodes.FORBIDDEN, message);
    }

    public static ErrorShape notFound(String message) {
        return new ErrorShape(GatewayConstants.ErrorCodes.NOT_FOUND, message);
    }

    public static ErrorShape validationError(String message) {
        return new ErrorShape(GatewayConstants.ErrorCodes.VALIDATION_ERROR, message);
    }

    public static ErrorShape internalError(String message) {
        return new ErrorShape(GatewayConstants.ErrorCodes.INTERNAL_ERROR, message);
    }

    public static ErrorShape unknownMethod(String method) {
        return new ErrorShape(GatewayConstants.ErrorCodes.UNKNOWN_METHOD,
            "unknown method: " + method);
    }
}
