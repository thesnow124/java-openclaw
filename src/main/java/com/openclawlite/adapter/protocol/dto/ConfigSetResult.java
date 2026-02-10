package com.openclawlite.adapter.protocol.dto;
public record ConfigSetResult(boolean ok) {
    public static ConfigSetResult success() { return new ConfigSetResult(true); }
}
