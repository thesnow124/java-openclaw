package com.openclawlite.adapter.plugin.core;

import com.openclawlite.common.enums.PluginType;

/**
 * Plugin metadata
 * Describes a plugin's identity and capabilities
 */
public record PluginMetadata(
    /**
     * Unique plugin ID (e.g., "com.example.myplugin")
     */
    String id,
    
    /**
     * Human-readable name
     */
    String name,
    
    /**
     * Plugin description
     */
    String description,
    
    /**
     * Plugin version
     */
    String version,
    
    /**
     * Plugin author
     */
    String author,
    
    /**
     * Minimum OpenClaw version required
     */
    String minApiVersion,
    
    /**
     * Plugin type (CHANNEL, TOOL, SKILL, etc.)
     */
    PluginType type,
    
    /**
     * Dependencies on other plugins
     */
    java.util.List<String> dependencies
) {
    public PluginMetadata {
        if (dependencies == null) {
            dependencies = java.util.List.of();
        }
    }
    
    /**
     * Create builder for PluginMetadata
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for PluginMetadata
     */
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private String version = "1.0.0";
        private String author;
        private String minApiVersion = "1.0.0";
        private PluginType type = PluginType.GENERAL;
        private java.util.List<String> dependencies = java.util.List.of();
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder minApiVersion(String minApiVersion) {
            this.minApiVersion = minApiVersion;
            return this;
        }
        
        public Builder type(PluginType type) {
            this.type = type;
            return this;
        }
        
        public Builder dependencies(java.util.List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }
        
        public PluginMetadata build() {
            return new PluginMetadata(id, name, description, version, author, 
                minApiVersion, type, dependencies);
        }
    }
}
