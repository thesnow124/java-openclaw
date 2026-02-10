package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

public record SessionsPatchParams(String sessionKey, Map<String, Object> metadata) {}
