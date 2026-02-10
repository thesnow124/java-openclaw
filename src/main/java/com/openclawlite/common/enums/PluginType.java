package com.openclawlite.common.enums;

/**
 * Plugin type enumeration
 */
public enum PluginType {
    /**
     * General purpose plugin
     */
    GENERAL,

    /**
     * Channel adapter (e.g., WhatsApp, Telegram)
     */
    CHANNEL,

    /**
     * Tool/Skill plugin
     */
    TOOL,

    /**
     * AI model provider
     */
    MODEL_PROVIDER,

    /**
     * Message handler/middleware
     */
    MIDDLEWARE,

    /**
     * Storage backend
     */
    STORAGE,

    /**
     * Embedded database
     */
    DATABASE
}
