package com.openclawlite.adapter.protocol.dto;
import java.util.List;
public record SkillMetadata(String emoji, String homepage, String primaryEnv, SkillRequires requires, List<SkillInstall> install, List<String> os) {}
