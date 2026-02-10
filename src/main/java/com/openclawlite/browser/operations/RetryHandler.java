package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 重试处理器
 *
 * <p>提供健壮的错误处理和重试机制。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>自动重试常见异常</li>
 *   <li>指数退避算法</li>
 *   <li>StaleElementReferenceException 处理</li>
 *   <li>超时和重试配置</li>
 *   <li>错误日志和监控</li>
 * </ul>
 */
public class RetryHandler {

    private static final Logger log = LoggerFactory.getLogger(RetryHandler.class);

    private final WebDriver driver;
    private final int defaultMaxRetries;
    private final long defaultBaseDelay;

    /**
     * 重试结果
     */
    public record RetryResult<T>(
        boolean success,          // 是否成功
        T result,                 // 结果
        String error,             // 错误信息
        int attempts,             // 尝试次数
        long totalTime            // 总耗时（毫秒）
    ) {
        public static <T> RetryResult<T> success(T result, int attempts, long totalTime) {
            return new RetryResult<>(true, result, null, attempts, totalTime);
        }

        public static <T> RetryResult<T> failure(String error, int attempts, long totalTime) {
            return new RetryResult<>(false, null, error, attempts, totalTime);
        }
    }

    /**
     * 重试配置
     */
    public static class RetryConfig {
        private int maxRetries = 3;
        private long baseDelay = 1000;          // 基础延迟（毫秒）
        private long maxDelay = 10000;          // 最大延迟（毫秒）
        private double backoffFactor = 2.0;     // 退避因子
        private boolean retryOnStaleElement = true;
        private boolean retryOnNoSuchElement = true;
        private boolean retryOnTimeout = true;
        private boolean retryOnWebDriverException = true;

        public RetryConfig() {
        }

        public RetryConfig maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryConfig baseDelay(long baseDelay) {
            this.baseDelay = baseDelay;
            return this;
        }

        public RetryConfig maxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public RetryConfig backoffFactor(double backoffFactor) {
            this.backoffFactor = backoffFactor;
            return this;
        }

        public RetryConfig retryOnStaleElement(boolean retry) {
            this.retryOnStaleElement = retry;
            return this;
        }

        public RetryConfig retryOnNoSuchElement(boolean retry) {
            this.retryOnNoSuchElement = retry;
            return this;
        }

        public RetryConfig retryOnTimeout(boolean retry) {
            this.retryOnTimeout = retry;
            return this;
        }

        public RetryConfig retryOnWebDriverException(boolean retry) {
            this.retryOnWebDriverException = retry;
            return this;
        }

        // Getters
        public int getMaxRetries() { return maxRetries; }
        public long getBaseDelay() { return baseDelay; }
        public long getMaxDelay() { return maxDelay; }
        public double getBackoffFactor() { return backoffFactor; }
        public boolean isRetryOnStaleElement() { return retryOnStaleElement; }
        public boolean isRetryOnNoSuchElement() { return retryOnNoSuchElement; }
        public boolean isRetryOnTimeout() { return retryOnTimeout; }
        public boolean isRetryOnWebDriverException() { return retryOnWebDriverException; }
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public RetryHandler(WebDriver driver) {
        this(driver, 3, 1000);
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param defaultMaxRetries 默认最大重试次数
     * @param defaultBaseDelay 默认基础延迟（毫秒）
     */
    public RetryHandler(WebDriver driver, int defaultMaxRetries, long defaultBaseDelay) {
        this.driver = driver;
        this.defaultMaxRetries = defaultMaxRetries;
        this.defaultBaseDelay = defaultBaseDelay;
    }

    /**
     * 执行带重试的操作
     *
     * @param supplier 操作提供者
     * @param <T> 返回类型
     * @return RetryResult 重试结果
     */
    public <T> RetryResult<T> executeWithRetry(Supplier<T> supplier) {
        return executeWithRetry(supplier, new RetryConfig());
    }

    /**
     * 执行带重试的操作（自定义配置）
     *
     * @param supplier 操作提供者
     * @param config 重试配置
     * @param <T> 返回类型
     * @return RetryResult 重试结果
     */
    public <T> RetryResult<T> executeWithRetry(Supplier<T> supplier, RetryConfig config) {
        long startTime = System.currentTimeMillis();
        int maxRetries = config.getMaxRetries();
        long baseDelay = config.getBaseDelay();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.debug("执行操作（尝试 {}/{}）", attempt, maxRetries);

                T result = supplier.get();
                long totalTime = System.currentTimeMillis() - startTime;

                if (attempt > 1) {
                    log.info("操作成功（重试 {}/{} 次）", attempt - 1, maxRetries);
                }

                return RetryResult.success(result, attempt, totalTime);

            } catch (StaleElementReferenceException e) {
                if (!config.isRetryOnStaleElement()) {
                    return createFailure(e.getMessage(), attempt, startTime);
                }

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, baseDelay, config);
                    log.warn("StaleElementReferenceException，{}ms 后重试（尝试 {}/{}）",
                            delay, attempt, maxRetries);
                    sleep(delay);
                }

            } catch (NoSuchElementException e) {
                if (!config.isRetryOnNoSuchElement()) {
                    return createFailure(e.getMessage(), attempt, startTime);
                }

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, baseDelay, config);
                    log.warn("NoSuchElementException，{}ms 后重试（尝试 {}/{}）",
                            delay, attempt, maxRetries);
                    sleep(delay);
                }

            } catch (TimeoutException e) {
                if (!config.isRetryOnTimeout()) {
                    return createFailure(e.getMessage(), attempt, startTime);
                }

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, baseDelay, config);
                    log.warn("TimeoutException，{}ms 后重试（尝试 {}/{}）",
                            delay, attempt, maxRetries);
                    sleep(delay);
                }

            } catch (WebDriverException e) {
                if (!config.isRetryOnWebDriverException()) {
                    return createFailure(e.getMessage(), attempt, startTime);
                }

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, baseDelay, config);
                    log.warn("WebDriverException，{}ms 后重试（尝试 {}/{}）: {}",
                            delay, attempt, maxRetries, e.getMessage());
                    sleep(delay);
                }

            } catch (Exception e) {
                log.error("未预期的异常，不再重试: {}", e.getMessage(), e);
                return createFailure(e.getMessage(), attempt, startTime);
            }
        }

        // 所有重试都失败
        long totalTime = System.currentTimeMillis() - startTime;
        String error = "所有重试都失败（尝试 " + maxRetries + " 次）";
        log.error(error);
        return RetryResult.failure(error, maxRetries, totalTime);
    }

    /**
     * 执行带重试的 Runnable 操作
     *
     * @param runnable Runnable 操作
     * @return RetryResult<Void> 重试结果
     */
    public RetryResult<Void> executeWithRetry(Runnable runnable) {
        return executeWithRetry(runnable, new RetryConfig());
    }

    /**
     * 执行带重试的 Runnable 操作（自定义配置）
     *
     * @param runnable Runnable 操作
     * @param config 重试配置
     * @return RetryResult<Void> 重试结果
     */
    public RetryResult<Void> executeWithRetry(Runnable runnable, RetryConfig config) {
        Supplier<Void> supplier = () -> {
            runnable.run();
            return null;
        };
        return executeWithRetry(supplier, config);
    }

    /**
     * 查找元素（带重试）
     *
     * @param by By 定位器
     * @return RetryResult<WebElement> 重试结果
     */
    public RetryResult<WebElement> findElementWithRetry(By by) {
        return executeWithRetry(() -> driver.findElement(by));
    }

    /**
     * 查找元素（带重试和自定义配置）
     *
     * @param by By 定位器
     * @param config 重试配置
     * @return RetryResult<WebElement> 重试结果
     */
    public RetryResult<WebElement> findElementWithRetry(By by, RetryConfig config) {
        return executeWithRetry(() -> driver.findElement(by), config);
    }

    /**
     * 点击元素（带重试）
     *
     * @param element WebElement 元素
     * @return RetryResult<Void> 重试结果
     */
    public RetryResult<Void> clickWithRetry(WebElement element) {
        return executeWithRetry(() -> {
            element.click();
        });
    }

    /**
     * 点击元素（带重试和自定义配置）
     *
     * @param element WebElement 元素
     * @param config 重试配置
     * @return RetryResult<Void> 重试结果
     */
    public RetryResult<Void> clickWithRetry(WebElement element, RetryConfig config) {
        return executeWithRetry(() -> {
            element.click();
        }, config);
    }

    /**
     * 发送按键（带重试）
     *
     * @param element WebElement 元素
     * @param text 文本
     * @return RetryResult<Void> 重试结果
     */
    public RetryResult<Void> sendKeysWithRetry(WebElement element, String text) {
        return executeWithRetry(() -> {
            element.clear();
            element.sendKeys(text);
        });
    }

    /**
     * 发送按键（带重试和自定义配置）
     *
     * @param element WebElement 元素
     * @param text 文本
     * @param config 重试配置
     * @return RetryResult<Void> 重试结果
     */
    public RetryResult<Void> sendKeysWithRetry(WebElement element, String text, RetryConfig config) {
        return executeWithRetry(() -> {
            element.clear();
            element.sendKeys(text);
        }, config);
    }

    /**
     * 等待元素出现（带重试）
     *
     * @param by By 定位器
     * @param timeout 超时时间（秒）
     * @return RetryResult<WebElement> 重试结果
     */
    public RetryResult<WebElement> waitForElementWithRetry(By by, int timeout) {
        return executeWithRetry(() -> {
            org.openqa.selenium.support.ui.WebDriverWait wait =
                new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, Duration.ofSeconds(timeout));
            return wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(by));
        });
    }

    /**
     * 等待元素可见（带重试）
     *
     * @param by By 定位器
     * @param timeout 超时时间（秒）
     * @return RetryResult<WebElement> 重试结果
     */
    public RetryResult<WebElement> waitForVisibleWithRetry(By by, int timeout) {
        return executeWithRetry(() -> {
            org.openqa.selenium.support.ui.WebDriverWait wait =
                new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, Duration.ofSeconds(timeout));
            return wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(by));
        });
    }

    /**
     * 等待元素可点击（带重试）
     *
     * @param by By 定位器
     * @param timeout 超时时间（秒）
     * @return RetryResult<WebElement> 重试结果
     */
    public RetryResult<WebElement> waitForClickableWithRetry(By by, int timeout) {
        return executeWithRetry(() -> {
            org.openqa.selenium.support.ui.WebDriverWait wait =
                new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, Duration.ofSeconds(timeout));
            return wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(by));
        });
    }

    /**
     * 创建失败结果
     *
     * @param error 错误信息
     * @param attempt 尝试次数
     * @param startTime 开始时间
     * @param <T> 返回类型
     * @return RetryResult<T> 失败结果
     */
    private <T> RetryResult<T> createFailure(String error, int attempt, long startTime) {
        long totalTime = System.currentTimeMillis() - startTime;
        log.error("操作失败: {}（尝试 {}/{}）", error, attempt, defaultMaxRetries);
        return RetryResult.failure(error, attempt, totalTime);
    }

    /**
     * 计算退避延迟时间（指数退避算法）
     *
     * @param attempt 当前尝试次数
     * @param baseDelay 基础延迟
     * @param config 重试配置
     * @return long 延迟时间（毫秒）
     */
    private long calculateDelay(int attempt, long baseDelay, RetryConfig config) {
        // 指数退避: baseDelay * (backoffFactor ^ (attempt - 1))
        long delay = (long) (baseDelay * Math.pow(config.getBackoffFactor(), attempt - 1));

        // 限制最大延迟
        return Math.min(delay, config.getMaxDelay());
    }

    /**
     * 睡眠指定时间
     *
     * @param millis 毫秒
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("睡眠被中断", e);
        }
    }

    /**
     * 获取默认重试配置
     *
     * @return RetryConfig 默认配置
     */
    public RetryConfig getDefaultConfig() {
        return new RetryConfig()
            .maxRetries(defaultMaxRetries)
            .baseDelay(defaultBaseDelay);
    }

    /**
     * 获取快速失败配置（不重试）
     *
     * @return RetryConfig 快速失败配置
     */
    public RetryConfig getFailFastConfig() {
        return new RetryConfig()
            .maxRetries(1)
            .baseDelay(0)
            .retryOnStaleElement(false)
            .retryOnNoSuchElement(false)
            .retryOnTimeout(false)
            .retryOnWebDriverException(false);
    }

    /**
     * 获取激进重试配置（多次重试）
     *
     * @return RetryConfig 激进重试配置
     */
    public RetryConfig getAggressiveRetryConfig() {
        return new RetryConfig()
            .maxRetries(10)
            .baseDelay(500)
            .backoffFactor(1.5); // 较小的退避因子
    }

    /**
     * 获取保守重试配置（较少重试，较长延迟）
     *
     * @return RetryConfig 保守重试配置
     */
    public RetryConfig getConservativeRetryConfig() {
        return new RetryConfig()
            .maxRetries(2)
            .baseDelay(2000)
            .backoffFactor(3.0); // 较大的退避因子
    }
}
