package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v125.network.Network;
import org.openqa.selenium.devtools.v125.network.model.Request;
import org.openqa.selenium.devtools.v125.network.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 网络监控器
 *
 * <p>监控和拦截网络请求。</p>
 *
 * <p>注意：此功能需要 Chrome DevTools 支持（Chrome/Edge）</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>网络请求日志记录</li>
 *   <li>请求/响应捕获</li>
 *   <li>网络性能分析</li>
 *   <li>请求拦截</li>
 *   <li>Mock 响应</li>
 * </ul>
 */
public class NetworkMonitor {

    private static final Logger log = LoggerFactory.getLogger(NetworkMonitor.class);

    private final WebDriver driver;
    private DevTools devTools;
    private boolean enabled = false;

    // 请求日志
    private final List<HttpRequest> requests = new ArrayList<>();
    private final Map<String, HttpRequest> requestMap = new ConcurrentHashMap<>();
    private final Map<String, HttpResponse> responseMap = new ConcurrentHashMap<>();

    // 拦截规则
    // 注意：Selenium 4.x DevTools 拦截功能有限，此字段保留供未来扩展
    // private final Map<String, Network.InterceptionPattern> interceptionPatterns = new ConcurrentHashMap<>();
    private final Map<String, MockResponse> mockResponses = new ConcurrentHashMap<>();

    /**
     * HTTP 请求
     */
    public record HttpRequest(
        String requestId,              // 请求 ID
        String url,                    // URL
        String method,                 // HTTP 方法
        Map<String, String> headers,   // 请求头
        String postData,               // POST 数据
        long timestamp,                // 时间戳
        String initiator               // 发起者
    ) {
        public HttpRequest {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (postData == null) {
                postData = "";
            }
        }
    }

    /**
     * HTTP 响应
     */
    public record HttpResponse(
        String requestId,              // 请求 ID
        String url,                    // URL
        int statusCode,                // 状态码
        String statusText,             // 状态文本
        Map<String, String> headers,   // 响应头
        String body,                   // 响应体
        long timestamp,                // 时间戳
        long duration                  // 耗时（毫秒）
    ) {
        public HttpResponse {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (body == null) {
                body = "";
            }
        }
    }

    /**
     * Mock 响应
     */
    public static class MockResponse {
        private final int statusCode;
        private final String statusText;
        private final Map<String, String> headers;
        private final String body;

        public MockResponse(int statusCode, String body) {
            this(statusCode, "OK", new HashMap<>(), body);
        }

        public MockResponse(int statusCode, String statusText, Map<String, String> headers, String body) {
            this.statusCode = statusCode;
            this.statusText = statusText;
            this.headers = new HashMap<>(headers);
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public String getStatusText() { return statusText; }
        public Map<String, String> getHeaders() { return new HashMap<>(headers); }
        public String getBody() { return body; }
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public NetworkMonitor(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * 启用网络监控
     *
     * @return boolean 是否成功
     */
    public boolean enable() {
        try {
            if (!(driver instanceof ChromeDriver)) {
                log.warn("网络监控仅支持 Chrome/Edge 浏览器");
                return false;
            }

            devTools = ((ChromeDriver) driver).getDevTools();
            devTools.createSession();

            // 启用 Network 领域（需要 Optional 参数）
            devTools.send(Network.enable(
                Optional.of(1000000),  // maxTotalBufferSize
                Optional.of(1000000),  // maxResourceBufferSize
                Optional.of(100)       // maxPostDataSize
            ));

            // 设置请求拦截
            devTools.addListener(Network.requestWillBeSent(), request -> {
                Map<String, Object> headersMap = request.getRequest().getHeaders();
                Map<String, String> headers = new HashMap<>();
                if (headersMap != null) {
                    for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                        headers.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }

                HttpRequest httpRequest = new HttpRequest(
                    request.getRequestId().toString(),
                    request.getRequest().getUrl(),
                    request.getRequest().getMethod(),
                    headers,
                    request.getRequest().getPostData() != null ?
                        request.getRequest().getPostData().toString() : "",
                    System.currentTimeMillis(),
                    request.getInitiator() != null ?
                        request.getInitiator().toString() : ""
                );

                requests.add(httpRequest);
                requestMap.put(request.getRequestId().toString(), httpRequest);

                log.debug("HTTP 请求: {} {}", httpRequest.method(), httpRequest.url());
            });

            // 设置响应监听
            devTools.addListener(Network.responseReceived(), response -> {
                Map<String, Object> headersMap = response.getResponse().getHeaders();
                Map<String, String> headers = new HashMap<>();
                if (headersMap != null) {
                    for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                        headers.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }

                HttpResponse httpResponse = new HttpResponse(
                    response.getRequestId().toString(),
                    response.getResponse().getUrl(),
                    response.getResponse().getStatus(),
                    response.getResponse().getStatusText(),
                    headers,
                    "",
                    System.currentTimeMillis(),
                    0
                );

                responseMap.put(response.getRequestId().toString(), httpResponse);

                log.debug("HTTP 响应: {} {} - {}",
                    response.getResponse().getUrl(),
                    response.getResponse().getStatus(),
                    response.getResponse().getStatusText()
                );
            });

            enabled = true;
            log.info("网络监控已启用");
            return true;

        } catch (Exception e) {
            log.error("启用网络监控失败", e);
            return false;
        }
    }

    /**
     * 禁用网络监控
     *
     * @return boolean 是否成功
     */
    public boolean disable() {
        try {
            if (devTools != null) {
                devTools.send(Network.disable());
                devTools.disconnectSession();
            }

            enabled = false;
            log.info("网络监控已禁用");
            return true;

        } catch (Exception e) {
            log.error("禁用网络监控失败", e);
            return false;
        }
    }

    /**
     * 检查是否已启用
     *
     * @return boolean 是否已启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取所有请求
     *
     * @return List<HttpRequest> 请求列表
     */
    public List<HttpRequest> getAllRequests() {
        return new ArrayList<>(requests);
    }

    /**
     * 获取所有响应
     *
     * @return List<HttpResponse> 响应列表
     */
    public List<HttpResponse> getAllResponses() {
        return new ArrayList<>(responseMap.values());
    }

    /**
     * 根据 URL 过滤请求
     *
     * @param urlPattern URL 模式（支持通配符）
     * @return List<HttpRequest> 匹配的请求列表
     */
    public List<HttpRequest> getRequestsByUrl(String urlPattern) {
        List<HttpRequest> filtered = new ArrayList<>();

        for (HttpRequest request : requests) {
            if (matchesPattern(request.url(), urlPattern)) {
                filtered.add(request);
            }
        }

        return filtered;
    }

    /**
     * 获取请求的响应
     *
     * @param requestId 请求 ID
     * @return HttpResponse 响应对象
     */
    public HttpResponse getResponse(String requestId) {
        return responseMap.get(requestId);
    }

    /**
     * 根据 URL 获取响应
     *
     * @param url URL
     * @return HttpResponse 响应对象
     */
    public HttpResponse getResponseByUrl(String url) {
        for (HttpRequest request : requests) {
            if (request.url().equals(url)) {
                return responseMap.get(request.requestId());
            }
        }
        return null;
    }

    /**
     * 清除所有日志
     *
     * @return boolean 是否成功
     */
    public boolean clearLogs() {
        requests.clear();
        requestMap.clear();
        responseMap.clear();
        log.info("清除网络日志");
        return true;
    }

    /**
     * 获取网络统计
     *
     * @return Map<String, Object> 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalRequests", requests.size());
        stats.put("totalResponses", responseMap.size());

        // 按状态码统计
        Map<Integer, Integer> statusCounts = new HashMap<>();
        for (HttpResponse response : responseMap.values()) {
            statusCounts.merge(response.statusCode(), 1, Integer::sum);
        }
        stats.put("statusCounts", statusCounts);

        // 按方法统计
        Map<String, Integer> methodCounts = new HashMap<>();
        for (HttpRequest request : requests) {
            methodCounts.merge(request.method(), 1, Integer::sum);
        }
        stats.put("methodCounts", methodCounts);

        // 计算总流量
        long totalBytes = 0;
        for (HttpResponse response : responseMap.values()) {
            String contentLength = response.headers().get("content-length");
            if (contentLength != null) {
                try {
                    totalBytes += Long.parseLong(contentLength);
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }
        stats.put("totalBytes", totalBytes);

        return stats;
    }

    /**
     * 等待请求完成
     *
     * @param urlPattern URL 模式
     * @param timeout 超时时间（秒）
     * @return boolean 是否完成
     */
    public boolean waitForRequest(String urlPattern, int timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (!getRequestsByUrl(urlPattern).isEmpty()) {
                log.info("请求已完成: pattern={}", urlPattern);
                return true;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.warn("等待请求超时: pattern={}", urlPattern);
        return false;
    }

    /**
     * 等待响应完成
     *
     * @param urlPattern URL 模式
     * @param timeout 超时时间（秒）
     * @return boolean 是否完成
     */
    public boolean waitForResponse(String urlPattern, int timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            for (HttpResponse response : responseMap.values()) {
                if (matchesPattern(response.url(), urlPattern)) {
                    log.info("响应已完成: pattern={}", urlPattern);
                    return true;
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.warn("等待响应超时: pattern={}", urlPattern);
        return false;
    }

    /**
     * 设置 Mock 响应
     *
     * @param urlPattern URL 模式
     * @param mockResponse Mock 响应
     * @return boolean 是否成功
     */
    public boolean setMockResponse(String urlPattern, MockResponse mockResponse) {
        try {
            mockResponses.put(urlPattern, mockResponse);
            log.info("设置 Mock 响应: pattern={}, status={}", urlPattern, mockResponse.getStatusCode());
            return true;
        } catch (Exception e) {
            log.error("设置 Mock 响应失败: pattern={}", urlPattern, e);
            return false;
        }
    }

    /**
     * 清除 Mock 响应
     *
     * @param urlPattern URL 模式
     * @return boolean 是否成功
     */
    public boolean clearMockResponse(String urlPattern) {
        mockResponses.remove(urlPattern);
        log.info("清除 Mock 响应: pattern={}", urlPattern);
        return true;
    }

    /**
     * 清除所有 Mock 响应
     *
     * @return boolean 是否成功
     */
    public boolean clearAllMockResponses() {
        mockResponses.clear();
        log.info("清除所有 Mock 响应");
        return true;
    }

    // ==================== 辅助方法 ====================

    /**
     * 模式匹配
     *
     * @param text 文本
     * @param pattern 模式（支持 * 通配符）
     * @return boolean 是否匹配
     */
    private boolean matchesPattern(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }

        // 转换为正则表达式
        String regex = pattern.replace(".", "\\.")
                             .replace("*", ".*")
                             .replace("?", ".");

        return text.matches(regex);
    }

    /**
     * 获取请求体大小
     *
     * @param request 请求对象
     * @return long 大小（字节）
     */
    public long getRequestSize(HttpRequest request) {
        long size = 0;

        // 计算头部大小
        for (Map.Entry<String, String> header : request.headers().entrySet()) {
            size += header.getKey().length() + header.getValue().length() + 4; // ": " + "\r\n"
        }

        // 计算请求体大小
        if (request.postData() != null) {
            size += request.postData().length();
        }

        return size;
    }

    /**
     * 获取响应体大小
     *
     * @param response 响应对象
     * @return long 大小（字节）
     */
    public long getResponseSize(HttpResponse response) {
        long size = 0;

        // 从 Content-Length 获取
        String contentLength = response.headers().get("content-length");
        if (contentLength != null) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                // 忽略
            }
        }

        // 计算头部大小
        for (Map.Entry<String, String> header : response.headers().entrySet()) {
            size += header.getKey().length() + header.getValue().length() + 4;
        }

        // 计算响应体大小
        size += response.body().length();

        return size;
    }

    /**
     * 生成网络报告
     *
     * @return String 网络报告
     */
    public String generateNetworkReport() {
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("网络监控报告\n");
        report.append("=".repeat(80)).append("\n\n");

        // 统计信息
        Map<String, Object> stats = getStatistics();

        report.append("【统计信息】\n");
        report.append(String.format("  总请求数: %d\n", stats.get("totalRequests")));
        report.append(String.format("  总响应数: %d\n", stats.get("totalResponses")));
        report.append(String.format("  总流量: %.2f KB\n",
            (Long) stats.get("totalBytes") / 1024.0));

        // 状态码分布
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) stats.get("statusCounts");
        if (!statusCounts.isEmpty()) {
            report.append("\n【状态码分布】\n");
            for (Map.Entry<Integer, Integer> entry : statusCounts.entrySet()) {
                report.append(String.format("  %d: %d\n", entry.getKey(), entry.getValue()));
            }
        }

        // 方法分布
        @SuppressWarnings("unchecked")
        Map<String, Integer> methodCounts = (Map<String, Integer>) stats.get("methodCounts");
        if (!methodCounts.isEmpty()) {
            report.append("\n【请求方法分布】\n");
            for (Map.Entry<String, Integer> entry : methodCounts.entrySet()) {
                report.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            }
        }

        // 请求列表
        report.append("\n【请求列表】\n");
        for (HttpRequest request : requests) {
            HttpResponse response = responseMap.get(request.requestId());
            String status = response != null ? String.valueOf(response.statusCode()) : "PENDING";
            report.append(String.format("  [%s] %s %s\n", status, request.method(), request.url()));
        }

        report.append("\n").append("=".repeat(80)).append("\n");

        return report.toString();
    }
}
