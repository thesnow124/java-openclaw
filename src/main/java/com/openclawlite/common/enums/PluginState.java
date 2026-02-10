package com.openclawlite.common.enums;

/**
 * Plugin state enumeration
 */
public enum PluginState {
    /**
     * Plugin is loaded but not initialized
     */
    LOADED,

    /**
     * Plugin is initialized
     */
    INITIALIZED,

    /**
     * Plugin is running
     */
    STARTED,

    /**
     * Plugin is stopped
     */
    STOPPED,

    /**
     * Plugin failed to load/start
     */
    FAILED,

    /**
     * Plugin is disabled
     */
    DISABLED
}
