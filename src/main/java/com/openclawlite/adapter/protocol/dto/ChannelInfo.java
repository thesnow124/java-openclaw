package com.openclawlite.adapter.protocol.dto;

public record ChannelInfo(
    String id,
    String label,
    String blurb,
    boolean configured
) {}
