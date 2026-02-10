package com.openclawlite.adapter.protocol.dto;
import java.util.List;
import java.util.Map;
public record SkillsStatusResult(List<SkillEntry> skills, Map<String, Object> env) {}
