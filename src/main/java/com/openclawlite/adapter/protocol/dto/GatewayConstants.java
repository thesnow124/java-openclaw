package com.openclawlite.adapter.protocol.dto;

/**
 * Gateway protocol constants
 */
public class GatewayConstants {
    public static final String PROTOCOL_VERSION = "1.0";
    public static final String DEFAULT_AGENT_ID = "default";

    // Error codes
    public static class ErrorCodes {
        public static final String INVALID_REQUEST = "INVALID_REQUEST";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String UNKNOWN_METHOD = "UNKNOWN_METHOD";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    }

    // Authorization scopes
    public static class Scopes {
        public static final String ADMIN = "operator.admin";
        public static final String READ = "operator.read";
        public static final String WRITE = "operator.write";
        public static final String APPROVALS = "operator.approvals";
        public static final String PAIRING = "operator.pairing";
    }
}
