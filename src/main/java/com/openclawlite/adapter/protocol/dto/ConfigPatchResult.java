package com.openclawlite.adapter.protocol.dto;
import java.util.Map;
public record ConfigPatchResult(boolean ok, Map<String, Object> config) {
    public static ConfigPatchResult success(Map<String, Object> config) { return new ConfigPatchResult(true, config); }
}
