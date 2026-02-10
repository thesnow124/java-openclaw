package com.openclawlite.adapter.protocol.dto;

public record ChannelStatusInfo(
    String channelId,
    String accountId,
    String name,
    boolean enabled,
    boolean configured,
    boolean linked,
    boolean running,
    boolean connected,
    int reconnectAttempts,
    Long lastConnectedAt,
    ChannelDisconnectInfo lastDisconnect,
    Long lastMessageAt,
    Long lastEventAt,
    String lastError,
    Long lastStartAt,
    Long lastStopAt,
    Long lastInboundAt,
    Long lastOutboundAt
) {}
