package com.openclawlite.adapter.channel.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Reconnection strategy for channels
 * Implements exponential backoff with jitter
 */
public class ReconnectStrategy {

    private static final Logger log = LoggerFactory.getLogger(ReconnectStrategy.class);

    private final long initialDelayMs;
    private final long maxDelayMs;
    private final double backoffMultiplier;
    private final double jitterFactor;

    private int attemptCount = 0;
    private long currentDelayMs;

    public ReconnectStrategy() {
        this(1000, 60000, 2.0, 0.1); // Default: 1s initial, 60s max, 2x backoff, 10% jitter
    }

    public ReconnectStrategy(long initialDelayMs, long maxDelayMs,
                            double backoffMultiplier, double jitterFactor) {
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.jitterFactor = jitterFactor;
        this.currentDelayMs = initialDelayMs;
    }

    /**
     * Get the next delay before reconnection attempt
     */
    public long getNextDelay() {
        long delay = calculateDelayWithJitter();

        // Update for next attempt
        attemptCount++;
        currentDelayMs = (long) Math.min(currentDelayMs * backoffMultiplier, maxDelayMs);

        return delay;
    }

    /**
     * Calculate delay with jitter
     */
    private long calculateDelayWithJitter() {
        long baseDelay = attemptCount == 0 ? initialDelayMs : currentDelayMs;
        long jitterRange = (long) (baseDelay * jitterFactor);
        long jitter = (long) (Math.random() * jitterRange * 2) - jitterRange;

        return Math.max(0, Math.min(baseDelay + jitter, maxDelayMs));
    }

    /**
     * Wait before next reconnection attempt
     */
    public void waitForNextAttempt() throws InterruptedException {
        long delay = getNextDelay();
        log.debug("Waiting {} ms before reconnection attempt {} (max: {} ms)",
                 delay, attemptCount + 1, maxDelayMs);
        TimeUnit.MILLISECONDS.sleep(delay);
    }

    /**
     * Reset the strategy (call after successful connection)
     */
    public void reset() {
        attemptCount = 0;
        currentDelayMs = initialDelayMs;
    }

    /**
     * Get current attempt count
     */
    public int getAttemptCount() {
        return attemptCount;
    }

    /**
     * Get current delay
     */
    public long getCurrentDelay() {
        return currentDelayMs;
    }
}
