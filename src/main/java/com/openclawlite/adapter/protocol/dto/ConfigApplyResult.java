package com.openclawlite.adapter.protocol.dto;
import java.util.Map;
public record ConfigApplyResult(boolean ok, Map<String, Object> config) {
    public static ConfigApplyResult success(Map<String, Object> config) { return new ConfigApplyResult(true, config); }
}
