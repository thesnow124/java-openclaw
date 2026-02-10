package com.openclawlite.adapter.plugin.skills;

import com.openclawlite.adapter.plugin.core.Plugin;
import com.openclawlite.adapter.plugin.core.PluginContext;
import com.openclawlite.common.exception.PluginException;
import com.openclawlite.adapter.plugin.core.PluginMetadata;
import com.openclawlite.common.enums.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Skills directory plugin loader
 * Loads skills from the skills/ directory as plugins
 */
@Component
public class SkillsPlugin implements Plugin {

    private static final Logger log = LoggerFactory.getLogger(SkillsPlugin.class);

    private final Path skillsDir;
    private final SkillsRegistry registry;
    private PluginContext context;

    public SkillsPlugin(@Qualifier("skillsDir") Path skillsDir, SkillsRegistry registry) {
        this.skillsDir = skillsDir;
        this.registry = registry;
    }
    
    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
            .id("openclaw.builtin.skills")
            .name("Skills Loader")
            .description("Loads skills from skills/ directory")
            .version("1.0.0")
            .author("OpenClaw Lite")
            .type(PluginType.TOOL)
            .build();
    }
    
    @Override
    public void initialize(PluginContext context) throws PluginException {
        this.context = context;
        
        // Create skills directory if it doesn't exist
        if (!Files.exists(skillsDir)) {
            try {
                Files.createDirectories(skillsDir);
                log.info("Created skills directory: {}", skillsDir);
            } catch (IOException e) {
                throw new PluginException(getMetadata().id(), 
                    "Failed to create skills directory", e);
            }
        }
        
        // Load skills
        loadSkills();
        
        log.info("Initialized Skills Plugin with {} skills", registry.getSkillCount());
    }
    
    @Override
    public void start() throws PluginException {
        log.info("Skills Plugin started");
    }
    
    @Override
    public void stop() throws PluginException {
        log.info("Skills Plugin stopped");
    }
    
    /**
     * Load all skills from skills directory
     */
    private void loadSkills() {
        try (Stream<Path> paths = Files.walk(skillsDir, 1)) {
            paths.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .forEach(this::loadSkill);
        } catch (IOException e) {
            log.error("Failed to load skills", e);
        }
    }
    
    /**
     * Load a single skill file
     */
    private void loadSkill(Path skillFile) {
        try {
            String content = Files.readString(skillFile);
            String skillName = extractSkillName(skillFile);
            
            Skill skill = new Skill(
                skillName,
                skillFile.toString(),
                content,
                Files.getLastModifiedTime(skillFile).toMillis()
            );
            
            registry.register(skill);
            
            log.debug("Loaded skill: {}", skillName);
        } catch (Exception e) {
            log.error("Failed to load skill: {}", skillFile, e);
        }
    }
    
    /**
     * Extract skill name from file path
     */
    private String extractSkillName(Path skillFile) {
        String filename = skillFile.getFileName().toString();
        return filename.substring(0, filename.length() - 3); // Remove .md
    }
    
    /**
     * Skill record
     */
    public record Skill(
        String name,
        String path,
        String content,
        long lastModified
    ) {}
}
