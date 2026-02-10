package com.openclawlite.adapter.protocol.dto;

public record HealthResult(boolean healthy, String version, Long uptime) {
    public static HealthResult createHealthy() {
        return new HealthResult(true, GatewayConstants.PROTOCOL_VERSION, Long.valueOf(System.currentTimeMillis()));
    }
}
