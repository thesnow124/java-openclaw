package com.openclawlite.adapter.plugin.core;

import java.nio.file.Path;
import java.util.Map;

/**
 * Plugin context
 * Provides runtime information and services to plugins
 */
public interface PluginContext {
    
    /**
     * Get plugin working directory
     */
    Path getWorkingDirectory();
    
    /**
     * Get plugin data directory
     */
    Path getDataDirectory();
    
    /**
     * Get plugin configuration
     */
    Map<String, Object> getConfig();
    
    /**
     * Get configuration value
     */
    default <T> T getConfigValue(String key, Class<T> type) {
        return null;
    }
    
    /**
     * Get configuration value with default
     */
    default <T> T getConfigValue(String key, T defaultValue) {
        return defaultValue;
    }
    
    /**
     * Get plugin class loader
     */
    ClassLoader getClassLoader();
    
    /**
     * Get logger for the plugin
     */
    org.slf4j.Logger getLogger();
}
