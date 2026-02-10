package com.openclawlite.openclaw.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Performance tuning configuration
 */
@Component
@ConfigurationProperties(prefix = "performance")
public class PerformanceConfig {

    /**
     * Connection pool settings
     */
    private int maxPoolSize = 10;
    private int minIdle = 2;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;

    /**
     * Cache settings
     */
    private boolean cacheEnabled = true;
    private long cacheTtl = 300000;  // 5 minutes
    private int maxCacheSize = 1000;

    /**
     * Async processing settings
     */
    private int corePoolSize = 4;
    private int maxPoolSizeAsync = 20;
    private int queueCapacity = 100;

    /**
     * Database settings
     */
    private int batchSize = 100;
    private int fetchSize = 50;

    // Getters and Setters
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }

    public int getMinIdle() { return minIdle; }
    public void setMinIdle(int minIdle) { this.minIdle = minIdle; }

    public long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }

    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }

    public long getCacheTtl() { return cacheTtl; }
    public void setCacheTtl(long cacheTtl) { this.cacheTtl = cacheTtl; }

    public int getMaxCacheSize() { return maxCacheSize; }
    public void setMaxCacheSize(int maxCacheSize) { this.maxCacheSize = maxCacheSize; }

    public int getCorePoolSize() { return corePoolSize; }
    public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }

    public int getMaxPoolSizeAsync() { return maxPoolSizeAsync; }
    public void setMaxPoolSizeAsync(int maxPoolSizeAsync) { this.maxPoolSizeAsync = maxPoolSizeAsync; }

    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public int getFetchSize() { return fetchSize; }
    public void setFetchSize(int fetchSize) { this.fetchSize = fetchSize; }
}
