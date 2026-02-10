package com.openclawlite.adapter.protocol.dto;

public record AgentsDeleteParams(
    String agentId,
    boolean deleteFiles
) {}
