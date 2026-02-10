package com.openclawlite.adapter.protocol.dto;

public record ModelChoice(
    String id,
    String name,
    String provider,
    Integer contextWindow,
    Boolean reasoning
) {}
