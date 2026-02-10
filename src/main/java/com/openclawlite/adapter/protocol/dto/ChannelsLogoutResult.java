package com.openclawlite.adapter.protocol.dto;

public record ChannelsLogoutResult(boolean ok) {
    public static ChannelsLogoutResult success() {
        return new ChannelsLogoutResult(true);
    }
}
