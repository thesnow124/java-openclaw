package com.openclawlite.common.exception;

/**
 * Exception thrown when plugin operations fail
 */
public class PluginException extends Exception {

    private final String pluginId;

    public PluginException(String pluginId, String message) {
        super(message);
        this.pluginId = pluginId;
    }

    public PluginException(String pluginId, String message, Throwable cause) {
        super(message, cause);
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    /**
     * Exception thrown when plugin cannot be found
     */
    public static class PluginNotFoundException extends PluginException {
        public PluginNotFoundException(String pluginId) {
            super(pluginId, "Plugin not found: " + pluginId);
        }
    }

    /**
     * Exception thrown when plugin version is incompatible
     */
    public static class IncompatibleVersionException extends PluginException {
        public IncompatibleVersionException(String pluginId, String required, String actual) {
            super(pluginId,
                String.format("Plugin %s requires API version %s but current version is %s",
                    pluginId, required, actual));
        }
    }

    /**
     * Exception thrown when plugin dependencies are not satisfied
     */
    public static class DependencyMissingException extends PluginException {
        public DependencyMissingException(String pluginId, String dependency) {
            super(pluginId,
                String.format("Plugin %s depends on %s which is not available",
                    pluginId, dependency));
        }
    }

    /**
     * Exception thrown when plugin initialization fails
     */
    public static class InitializationException extends PluginException {
        public InitializationException(String pluginId, String message, Throwable cause) {
            super(pluginId, "Failed to initialize plugin " + pluginId + ": " + message, cause);
        }
    }
}
