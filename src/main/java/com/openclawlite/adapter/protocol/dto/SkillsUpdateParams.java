package com.openclawlite.adapter.protocol.dto;
import java.util.Map;
public record SkillsUpdateParams(String skillKey, Boolean enabled, String apiKey, Map<String, String> env) {}
