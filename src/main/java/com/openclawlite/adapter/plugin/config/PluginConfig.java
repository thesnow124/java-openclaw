package com.openclawlite.adapter.plugin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Plugin system configuration
 */
@Component
@ConfigurationProperties(prefix = "openclaw.plugins")
public class PluginConfig {
    
    /**
     * Current API version
     */
    private String currentApiVersion = "1.0.0";
    
    /**
     * Plugin directories to scan
     */
    private List<String> pluginDirs = List.of("plugins");
    
    /**
     * Plugin data directory
     */
    private String pluginDataDir = "data/plugins";
    
    /**
     * Enable hot reload
     */
    private boolean hotReloadEnabled = false;
    
    /**
     * Hot reload check interval (milliseconds)
     */
    private long hotReloadInterval = 5000;
    
    /**
     * Enable plugin isolation
     */
    private boolean isolationEnabled = true;
    
    /**
     * Auto-start plugins on load
     */
    private boolean autoStart = true;
    
    /**
     * Disabled plugins (by ID)
     */
    private List<String> disabledPlugins = List.of();
    
    // Getters and setters
    
    public String getCurrentApiVersion() {
        return currentApiVersion;
    }
    
    public void setCurrentApiVersion(String currentApiVersion) {
        this.currentApiVersion = currentApiVersion;
    }
    
    public List<String> getPluginDirs() {
        return pluginDirs;
    }
    
    public void setPluginDirs(List<String> pluginDirs) {
        this.pluginDirs = pluginDirs;
    }
    
    public List<Path> getPluginDirectories() {
        return pluginDirs.stream()
            .map(Path::of)
            .toList();
    }
    
    public String getPluginDataDir() {
        return pluginDataDir;
    }
    
    public void setPluginDataDir(String pluginDataDir) {
        this.pluginDataDir = pluginDataDir;
    }
    
    public Path getPluginDataPath() {
        return Path.of(pluginDataDir);
    }
    
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }
    
    public void setHotReloadEnabled(boolean hotReloadEnabled) {
        this.hotReloadEnabled = hotReloadEnabled;
    }
    
    public long getHotReloadInterval() {
        return hotReloadInterval;
    }
    
    public void setHotReloadInterval(long hotReloadInterval) {
        this.hotReloadInterval = hotReloadInterval;
    }
    
    public boolean isIsolationEnabled() {
        return isolationEnabled;
    }
    
    public void setIsolationEnabled(boolean isolationEnabled) {
        this.isolationEnabled = isolationEnabled;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    public List<String> getDisabledPlugins() {
        return disabledPlugins;
    }
    
    public void setDisabledPlugins(List<String> disabledPlugins) {
        this.disabledPlugins = disabledPlugins;
    }
    
    public boolean isPluginDisabled(String pluginId) {
        return disabledPlugins.contains(pluginId);
    }
}
