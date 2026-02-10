package com.openclawlite.adapter.protocol.dto;
public record SendResult(boolean ok) {
    public static SendResult success() { return new SendResult(true); }
}
