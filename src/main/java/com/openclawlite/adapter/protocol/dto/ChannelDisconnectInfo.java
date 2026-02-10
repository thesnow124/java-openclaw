package com.openclawlite.adapter.protocol.dto;

public record ChannelDisconnectInfo(
    Long at,
    Integer status,
    String error,
    Boolean loggedOut
) {}
