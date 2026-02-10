package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 性能监控器
 *
 * <p>监控和分析页面性能指标。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>页面加载时间统计</li>
 *   <li>资源加载分析</li>
 *   <li>性能指标收集</li>
 *   <li>内存使用监控</li>
 *   <li>自定义性能标记</li>
 *   <li>性能报告生成</li>
 * </ul>
 */
public class PerformanceMonitor {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);

    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;

    /**
     * 性能指标
     */
    public record PerformanceMetrics(
        // 导航时间
        long navigationStart,           // 导航开始时间
        long fetchStart,                // 获取开始时间
        long domainLookupStart,         // DNS 查询开始
        long domainLookupEnd,           // DNS 查询结束
        long connectStart,              // 连接开始
        long connectEnd,                // 连接结束
        long requestStart,              // 请求开始
        long responseStart,             // 响应开始
        long responseEnd,               // 响应结束
        long domLoading,                // DOM 加载开始
        long domInteractive,            // DOM 交互完成
        long domContentLoaded,          // DOM 内容加载完成
        long domComplete,               // DOM 完全加载
        long loadEventStart,            // Load 事件开始
        long loadEventEnd,              // Load 事件结束

        // 计算出的时间差
        long dnsTime,                   // DNS 查询耗时
        long connectTime,               // 连接耗时
        long requestTime,               // 请求耗时
        long responseTime,              // 响应耗时
        long domProcessingTime,         // DOM 处理耗时
        long domContentLoadedTime,      // DOM 内容加载耗时
        long windowLoadTime,            // Window Load 耗时
        long totalPageLoadTime,         // 总页面加载耗时

        // 资源统计
        int resourceCount,              // 资源数量
        long totalResourceSize,         // 总资源大小（字节）

        // 内存使用
        long jsHeapSizeLimit,           // JS 堆大小限制
        long totalJSHeapSize,           // 总 JS 堆大小
        long usedJSHeapSize,            // 已使用 JS 堆大小

        // 自定义标记
        Map<String, Long> marks,        // 性能标记
        Map<String, Long> measures      // 性能测量
    ) {
        public String toSummary() {
            return String.format(
                "页面性能摘要:\n" +
                "  DNS 查询: %d ms\n" +
                "  TCP 连接: %d ms\n" +
                "  请求响应: %d ms\n" +
                "  DOM 处理: %d ms\n" +
                "  DOM 加载: %d ms\n" +
                "  页面加载: %d ms\n" +
                "  总耗时: %d ms\n" +
                "  资源数量: %d\n" +
                "  总资源大小: %.2f KB\n" +
                "  JS 堆使用: %.2f MB / %.2f MB",
                dnsTime, connectTime, requestTime + responseTime,
                domProcessingTime, domContentLoadedTime,
                windowLoadTime, totalPageLoadTime,
                resourceCount, totalResourceSize / 1024.0,
                usedJSHeapSize / 1024.0 / 1024.0, jsHeapSizeLimit / 1024.0 / 1024.0
            );
        }
    }

    /**
     * 资源信息
     */
    public record ResourceInfo(
        String name,                    // 资源名称
        String initiatorType,           // 发起者类型
        String type,                    // 资源类型
        long duration,                  // 加载耗时（毫秒）
        long size,                      // 资源大小（字节）
        String transferSize,            // 传输大小
        boolean cached,                 // 是否缓存
        String statusCode               // 状态码
    ) {}

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public PerformanceMonitor(WebDriver driver) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    /**
     * 获取性能指标
     *
     * @return PerformanceMetrics 性能指标
     */
    @SuppressWarnings("unchecked")
    public PerformanceMetrics getPerformanceMetrics() {
        try {
            // 获取 Navigation Timing
            Map<String, Object> timing = (Map<String, Object>) jsExecutor.executeScript(
                "var timing = performance.timing || {};" +
                "return {" +
                "  navigationStart: timing.navigationStart," +
                "  fetchStart: timing.fetchStart," +
                "  domainLookupStart: timing.domainLookupStart," +
                "  domainLookupEnd: timing.domainLookupEnd," +
                "  connectStart: timing.connectStart," +
                "  connectEnd: timing.connectEnd," +
                "  requestStart: timing.requestStart," +
                "  responseStart: timing.responseStart," +
                "  responseEnd: timing.responseEnd," +
                "  domLoading: timing.domLoading," +
                "  domInteractive: timing.domInteractive," +
                "  domContentLoaded: timing.domContentLoadedEventEnd," +
                "  domComplete: timing.domComplete," +
                "  loadEventStart: timing.loadEventStart," +
                "  loadEventEnd: timing.loadEventEnd" +
                "};"
            );

            // 计算时间差
            long navStart = getLong(timing, "navigationStart");
            long dnsTime = getLong(timing, "domainLookupEnd") - getLong(timing, "domainLookupStart");
            long connectTime = getLong(timing, "connectEnd") - getLong(timing, "connectStart");
            long requestTime = getLong(timing, "responseStart") - getLong(timing, "requestStart");
            long responseTime = getLong(timing, "responseEnd") - getLong(timing, "responseStart");
            long domProcessingTime = getLong(timing, "domInteractive") - getLong(timing, "domLoading");
            long domContentLoadedTime = getLong(timing, "domContentLoaded") - getLong(timing, "domInteractive");
            long windowLoadTime = getLong(timing, "loadEventEnd") - getLong(timing, "loadEventStart");
            long totalPageLoadTime = getLong(timing, "loadEventEnd") - navStart;

            // 获取资源信息
            List<Map<String, Object>> resources = (List<Map<String, Object>>) jsExecutor.executeScript(
                "return performance.getEntriesByType('resource').map(function(r) {" +
                "  return {" +
                "    name: r.name," +
                "    initiatorType: r.initiatorType," +
                "    duration: r.duration," +
                "    transferSize: r.transferSize," +
                "    encodedBodySize: r.encodedBodySize," +
                "    decodedBodySize: r.decodedBodySize" +
                "  };" +
                "});"
            );

            int resourceCount = resources.size();
            long totalResourceSize = resources.stream()
                .mapToLong(r -> getLong(r, "decodedBodySize"))
                .sum();

            // 获取内存使用（仅 Chrome）
            long jsHeapSizeLimit = 0;
            long totalJSHeapSize = 0;
            long usedJSHeapSize = 0;

            try {
                Map<String, Object> memory = (Map<String, Object>) jsExecutor.executeScript(
                    "if (window.performance && window.performance.memory) {" +
                    "  return {" +
                    "    jsHeapSizeLimit: performance.memory.jsHeapSizeLimit," +
                    "    totalJSHeapSize: performance.memory.totalJSHeapSize," +
                    "    usedJSHeapSize: performance.memory.usedJSHeapSize" +
                    "  };" +
                "} else {" +
                    "  return {};" +
                    "}"
                );

                jsHeapSizeLimit = getLong(memory, "jsHeapSizeLimit");
                totalJSHeapSize = getLong(memory, "totalJSHeapSize");
                usedJSHeapSize = getLong(memory, "usedJSHeapSize");
            } catch (Exception e) {
                log.debug("内存信息获取失败（仅 Chrome 支持）");
            }

            // 获取性能标记
            Map<String, Long> marks = getPerformanceMarks();
            Map<String, Long> measures = getPerformanceMeasures();

            return new PerformanceMetrics(
                navStart,
                getLong(timing, "fetchStart"),
                getLong(timing, "domainLookupStart"),
                getLong(timing, "domainLookupEnd"),
                getLong(timing, "connectStart"),
                getLong(timing, "connectEnd"),
                getLong(timing, "requestStart"),
                getLong(timing, "responseStart"),
                getLong(timing, "responseEnd"),
                getLong(timing, "domLoading"),
                getLong(timing, "domInteractive"),
                getLong(timing, "domContentLoaded"),
                getLong(timing, "domComplete"),
                getLong(timing, "loadEventStart"),
                getLong(timing, "loadEventEnd"),
                dnsTime,
                connectTime,
                requestTime,
                responseTime,
                domProcessingTime,
                domContentLoadedTime,
                windowLoadTime,
                totalPageLoadTime,
                resourceCount,
                totalResourceSize,
                jsHeapSizeLimit,
                totalJSHeapSize,
                usedJSHeapSize,
                marks,
                measures
            );

        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            return null;
        }
    }

    /**
     * 获取资源列表
     *
     * @return List<ResourceInfo> 资源列表
     */
    @SuppressWarnings("unchecked")
    public List<ResourceInfo> getResources() {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) jsExecutor.executeScript(
                "return performance.getEntriesByType('resource').map(function(r) {" +
                "  return {" +
                "    name: r.name," +
                "    initiatorType: r.initiatorType," +
                "    entryType: r.entryType," +
                "    duration: r.duration," +
                "    transferSize: r.transferSize," +
                "    encodedBodySize: r.encodedBodySize," +
                "    decodedBodySize: r.decodedBodySize," +
                "    startTime: r.startTime," +
                "    responseEnd: r.responseEnd" +
                "  };" +
                "});"
            );

            List<ResourceInfo> resources = new ArrayList<>();

            for (Map<String, Object> entry : entries) {
                String name = getString(entry, "name");
                String initiatorType = getString(entry, "initiatorType");
                String type = getResourceType(name);
                long duration = getLong(entry, "duration");
                long size = getLong(entry, "decodedBodySize");
                long transferSize = getLong(entry, "transferSize");

                boolean cached = (transferSize == 0 || transferSize == 1);

                // 获取状态码（需要通过 PerformanceObserver）
                String statusCode = "200"; // 默认值，实际需要通过网络日志获取

                resources.add(new ResourceInfo(
                    name, initiatorType, type, duration, size,
                    formatSize(transferSize), cached, statusCode
                ));
            }

            return resources;

        } catch (Exception e) {
            log.error("获取资源列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 添加性能标记
     *
     * @param name 标记名称
     * @return boolean 是否成功
     */
    public boolean addMark(String name) {
        try {
            jsExecutor.executeScript("performance.mark('" + name + "');");
            log.debug("添加性能标记: {}", name);
            return true;
        } catch (Exception e) {
            log.error("添加性能标记失败: {}", name, e);
            return false;
        }
    }

    /**
     * 清除性能标记
     *
     * @param name 标记名称
     * @return boolean 是否成功
     */
    public boolean clearMark(String name) {
        try {
            jsExecutor.executeScript("performance.clearMarks('" + name + "');");
            log.debug("清除性能标记: {}", name);
            return true;
        } catch (Exception e) {
            log.error("清除性能标记失败: {}", name, e);
            return false;
        }
    }

    /**
     * 测量两个标记之间的时间
     *
     * @param name 测量名称
     * @param startMark 开始标记
     * @param endMark 结束标记
     * @return boolean 是否成功
     */
    public boolean measure(String name, String startMark, String endMark) {
        try {
            jsExecutor.executeScript(
                "performance.measure('" + name + "', '" + startMark + "', '" + endMark + "');"
            );
            log.debug("测量性能: {} ({} -> {})", name, startMark, endMark);
            return true;
        } catch (Exception e) {
            log.error("测量性能失败: {}", name, e);
            return false;
        }
    }

    /**
     * 获取所有性能标记
     *
     * @return Map<String, Long> 标记映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Long> getPerformanceMarks() {
        try {
            List<Map<String, Object>> marks = (List<Map<String, Object>>) jsExecutor.executeScript(
                "return performance.getEntriesByType('mark').map(function(m) {" +
                "  return { name: m.name, startTime: m.startTime };" +
                "});"
            );

            Map<String, Long> result = new HashMap<>();
            for (Map<String, Object> mark : marks) {
                result.put(getString(mark, "name"), getLong(mark, "startTime"));
            }

            return result;

        } catch (Exception e) {
            log.error("获取性能标记失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取所有性能测量
     *
     * @return Map<String, Long> 测量映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Long> getPerformanceMeasures() {
        try {
            List<Map<String, Object>> measures = (List<Map<String, Object>>) jsExecutor.executeScript(
                "return performance.getEntriesByType('measure').map(function(m) {" +
                "  return { name: m.name, duration: m.duration };" +
                "});"
            );

            Map<String, Long> result = new HashMap<>();
            for (Map<String, Object> measure : measures) {
                result.put(getString(measure, "name"), getLong(measure, "duration"));
            }

            return result;

        } catch (Exception e) {
            log.error("获取性能测量失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 生成性能报告
     *
     * @return String 性能报告
     */
    public String generatePerformanceReport() {
        PerformanceMetrics metrics = getPerformanceMetrics();

        if (metrics == null) {
            return "无法获取性能指标";
        }

        StringBuilder report = new StringBuilder();
        report.append("=" .repeat(80)).append("\n");
        report.append("页面性能报告\n");
        report.append("=" .repeat(80)).append("\n\n");

        // 时间线
        report.append("【时间线】\n");
        report.append(String.format("  DNS 查询:        %6d ms\n", metrics.dnsTime()));
        report.append(String.format("  TCP 连接:        %6d ms\n", metrics.connectTime()));
        report.append(String.format("  请求时间:        %6d ms\n", metrics.requestTime()));
        report.append(String.format("  响应时间:        %6d ms\n", metrics.responseTime()));
        report.append(String.format("  DOM 处理:        %6d ms\n", metrics.domProcessingTime()));
        report.append(String.format("  DOM 内容加载:    %6d ms\n", metrics.domContentLoadedTime()));
        report.append(String.format("  Window 加载:     %6d ms\n", metrics.windowLoadTime()));
        report.append(String.format("  总耗时:          %6d ms\n", metrics.totalPageLoadTime()));
        report.append("\n");

        // 资源统计
        report.append("【资源统计】\n");
        report.append(String.format("  资源数量:        %d\n", metrics.resourceCount()));
        report.append(String.format("  总大小:          %.2f KB\n", metrics.totalResourceSize() / 1024.0));

        // 资源分类统计
        List<ResourceInfo> resources = getResources();
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Long> typeSize = new HashMap<>();

        for (ResourceInfo resource : resources) {
            typeCount.merge(resource.type(), 1, Integer::sum);
            typeSize.merge(resource.type(), resource.size(), Long::sum);
        }

        report.append("\n【资源分类】\n");
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            long size = typeSize.getOrDefault(type, 0L);
            report.append(String.format("  %s: %d 个, %.2f KB\n", type, count, size / 1024.0));
        }

        // 内存使用
        if (metrics.jsHeapSizeLimit() > 0) {
            report.append("\n【内存使用】\n");
            report.append(String.format("  JS 堆限制:      %.2f MB\n",
                metrics.jsHeapSizeLimit() / 1024.0 / 1024.0));
            report.append(String.format("  总 JS 堆:       %.2f MB\n",
                metrics.totalJSHeapSize() / 1024.0 / 1024.0));
            report.append(String.format("  已使用:         %.2f MB (%.1f%%)\n",
                metrics.usedJSHeapSize() / 1024.0 / 1024.0,
                (metrics.usedJSHeapSize() * 100.0 / metrics.jsHeapSizeLimit())));
        }

        // 自定义标记
        if (!metrics.marks().isEmpty()) {
            report.append("\n【性能标记】\n");
            for (Map.Entry<String, Long> entry : metrics.marks().entrySet()) {
                report.append(String.format("  %s: %.2f ms\n", entry.getKey(), entry.getValue() / 1000000.0));
            }
        }

        // 自定义测量
        if (!metrics.measures().isEmpty()) {
            report.append("\n【性能测量】\n");
            for (Map.Entry<String, Long> entry : metrics.measures().entrySet()) {
                report.append(String.format("  %s: %.2f ms\n", entry.getKey(), entry.getValue() / 1000000.0));
            }
        }

        // 性能评分
        report.append("\n【性能评分】\n");
        report.append(generatePerformanceScore(metrics));

        report.append("\n").append("=".repeat(80)).append("\n");

        return report.toString();
    }

    /**
     * 生成性能评分
     *
     * @param metrics 性能指标
     * @return String 评分报告
     */
    private String generatePerformanceScore(PerformanceMetrics metrics) {
        int score = 100;

        // 页面加载时间评分
        if (metrics.totalPageLoadTime() > 3000) {
            score -= 20;
        } else if (metrics.totalPageLoadTime() > 2000) {
            score -= 10;
        } else if (metrics.totalPageLoadTime() > 1000) {
            score -= 5;
        }

        // DOM 处理时间评分
        if (metrics.domProcessingTime() > 1000) {
            score -= 15;
        } else if (metrics.domProcessingTime() > 500) {
            score -= 8;
        }

        // 资源数量评分
        if (metrics.resourceCount() > 100) {
            score -= 10;
        } else if (metrics.resourceCount() > 50) {
            score -= 5;
        }

        // 内存使用评分
        if (metrics.jsHeapSizeLimit() > 0) {
            double usage = metrics.usedJSHeapSize() * 100.0 / metrics.jsHeapSizeLimit();
            if (usage > 80) {
                score -= 15;
            } else if (usage > 60) {
                score -= 8;
            }
        }

        score = Math.max(0, score);

        String grade;
        if (score >= 90) {
            grade = "优秀";
        } else if (score >= 80) {
            grade = "良好";
        } else if (score >= 60) {
            grade = "中等";
        } else {
            grade = "较差";
        }

        return String.format("  总分: %d/100 (%s)\n", score, grade);
    }

    /**
     * 等待页面加载完成并获取性能指标
     *
     * @param timeout 超时时间（秒）
     * @return PerformanceMetrics 性能指标
     */
    public PerformanceMetrics waitForPageLoad(int timeout) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(webDriver -> {
                Object readyState = ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState");
                return "complete".equals(readyState);
            });

            // 等待一小段时间确保所有资源加载完成
            Thread.sleep(500);

            return getPerformanceMetrics();

        } catch (Exception e) {
            log.error("等待页面加载完成失败", e);
            return null;
        }
    }

    /**
     * 清除所有性能数据
     *
     * @return boolean 是否成功
     */
    public boolean clearPerformanceData() {
        try {
            jsExecutor.executeScript(
                "performance.clearMarks();" +
                "performance.clearMeasures();" +
                "if (performance.clearResourceTimings) {" +
                "  performance.clearResourceTimings();" +
                "}"
            );
            log.info("清除性能数据");
            return true;
        } catch (Exception e) {
            log.error("清除性能数据失败", e);
            return false;
        }
    }

    // ==================== 辅助方法 ====================

    private long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private String getResourceType(String url) {
        if (url == null) {
            return "unknown";
        }

        String lower = url.toLowerCase();

        if (lower.endsWith(".js")) {
            return "script";
        } else if (lower.endsWith(".css")) {
            return "stylesheet";
        } else if (lower.matches(".*\\.(jpg|jpeg|png|gif|webp|svg|ico)")) {
            return "image";
        } else if (lower.matches(".*\\.(woff|woff2|ttf|eot|otf)")) {
            return "font";
        } else if (lower.contains(".html") || lower.contains(".htm")) {
            return "document";
        } else if (lower.matches(".*\\.(mp4|webm|ogg|mp3|wav)")) {
            return "media";
        } else {
            return "other";
        }
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
