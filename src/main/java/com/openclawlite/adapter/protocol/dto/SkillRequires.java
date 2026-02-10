package com.openclawlite.adapter.protocol.dto;
import java.util.List;
import java.util.Map;
public record SkillRequires(List<String> bins, Map<String, String> env, List<String> config) {}
