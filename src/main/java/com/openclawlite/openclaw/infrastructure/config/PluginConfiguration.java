package com.openclawlite.openclaw.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class PluginConfiguration {

    @Bean
    public Path skillsDir(AppProperties properties) {
        return Paths.get(properties.getSkillsDir());
    }

    @Bean
    public Path toolsDir(AppProperties properties) {
        return Paths.get(properties.getToolsDir());
    }

    @Bean
    public Path workspaceDir(AppProperties properties) {
        return Paths.get(properties.getWorkspaceDir());
    }
}
