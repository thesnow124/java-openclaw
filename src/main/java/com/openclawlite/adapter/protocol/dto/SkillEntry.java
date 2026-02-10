package com.openclawlite.adapter.protocol.dto;
public record SkillEntry(String key, String name, String description, boolean enabled, boolean userInvocable, SkillMetadata metadata) {}
