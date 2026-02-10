package com.openclawlite.adapter.protocol.dto;

public record ChannelAccount(
    String accountId,
    String name,
    boolean enabled,
    boolean configured,
    boolean linked,
    boolean running,
    boolean connected
) {}
