package com.openclawlite.adapter.plugin.skills;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skills registry
 * Manages loaded skills
 */
@Component
public class SkillsRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(SkillsRegistry.class);
    
    private final Map<String, SkillsPlugin.Skill> skills = new ConcurrentHashMap<>();
    
    /**
     * Register a skill
     */
    public void register(SkillsPlugin.Skill skill) {
        skills.put(skill.name(), skill);
        log.debug("Registered skill: {}", skill.name());
    }
    
    /**
     * Unregister a skill
     */
    public void unregister(String skillName) {
        skills.remove(skillName);
        log.debug("Unregistered skill: {}", skillName);
    }
    
    /**
     * Get skill by name
     */
    public Optional<SkillsPlugin.Skill> getSkill(String name) {
        return Optional.ofNullable(skills.get(name));
    }
    
    /**
     * Get all skills
     */
    public Collection<SkillsPlugin.Skill> getAllSkills() {
        return skills.values();
    }
    
    /**
     * Get skill count
     */
    public int getSkillCount() {
        return skills.size();
    }
    
    /**
     * Check if skill exists
     */
    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }
}
