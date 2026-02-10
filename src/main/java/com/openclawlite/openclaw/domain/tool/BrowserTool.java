package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.browser.core.BrowserManager;
import com.openclawlite.browser.model.*;
import com.openclawlite.browser.operations.*;
import com.openclawlite.browser.proxy.BrowserProxy;
import com.openclawlite.browser.proxy.SeleniumProxy;
import com.openclawlite.openclaw.domain.agent.ToolCall;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 浏览器工具
 *
 * <p>提供浏览器自动化操作的 ToolHandler 实现。</p>
 *
 * <p>支持的操作：</p>
 * <ul>
 *   <li>start - 启动浏览器</li>
 *   <li>stop - 停止浏览器</li>
 *   <li>status - 获取浏览器状态</li>
 *   <li>navigate - 导航到 URL</li>
 *   <li>snapshot - 获取页面快照</li>
 *   <li>screenshot - 截图</li>
 *   <li>act - 元素交互（click, type, press, wait）</li>
 *   <li>tabs - 获取标签页列表</li>
 *   <li>close_tab - 关闭标签页</li>
 *   <li>cookie - Cookie 操作</li>
 *   <li>storage - 存储操作</li>
 *   <li>execute_js - 执行 JavaScript</li>
 *   <li>performance - 获取性能指标</li>
 *   <li>dialog - 处理对话框</li>
 *   <li>upload - 上传文件</li>
 *   <li>download - 下载文件</li>
 * </ul>
 */
@Component
public class BrowserTool implements ToolHandler {

    private static final Logger log = LoggerFactory.getLogger(BrowserTool.class);

    private final BrowserProxy browserProxy;
    private final BrowserManager browserManager;

    @Autowired
    public BrowserTool(BrowserProxy browserProxy, BrowserManager browserManager) {
        this.browserProxy = browserProxy;
        this.browserManager = browserManager;
    }

    @Override
    public String name() {
        return "browser";
    }

    @Override
    public String description() {
        return "浏览器自动化工具，支持完整的浏览器操作（启动、停止、导航、交互、截图等）";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "browser",
              "actions": {
                "start": {"profile": "配置文件名（默认：default）"},
                "stop": {"profile": "配置文件名"},
                "status": {"profile": "配置文件名"},
                "navigate": {"profile": "配置文件名", "url": "目标URL"},
                "snapshot": {"profile": "配置文件名", "includeHtml": false},
                "screenshot": {"profile": "配置文件名", "format": "png"},
                "act": {"profile": "配置文件名", "action": "click", "selector": "CSS选择器", "text": "输入文本"},
                "tabs": {"profile": "配置文件名"},
                "close_tab": {"targetId": "标签页ID"},
                "execute_js": {"profile": "配置文件名", "script": "JavaScript代码"},
                "performance": {"profile": "配置文件名"}
              }
            }
            """;
    }

    @Override
    public String execute(ToolCall call, ToolContext context) {
        try {
            Map<String, Object> args = call.getArguments();
            String action = getAction(args);

            log.info("执行浏览器操作: action={}, args={}", action, args);

            return switch (action) {
                // 浏览器生命周期
                case "start" -> handleStart(args);
                case "stop" -> handleStop(args);
                case "status" -> handleStatus(args);

                // 页面操作
                case "navigate" -> handleNavigate(args);
                case "snapshot" -> handleSnapshot(args);
                case "screenshot" -> handleScreenshot(args);

                // 元素交互
                case "act" -> handleAct(args);

                // 标签页管理
                case "tabs" -> handleTabs(args);
                case "close_tab" -> handleCloseTab(args);

                // Cookie 和存储
                case "cookie" -> handleCookie(args);
                case "storage" -> handleStorage(args);

                // JavaScript 执行
                case "execute_js" -> handleExecuteJs(args);

                // 性能监控
                case "performance" -> handlePerformance(args);

                // 对话框处理
                case "dialog" -> handleDialog(args);

                // 文件操作
                case "upload" -> handleUpload(args);
                case "download" -> handleDownload(args);

                default -> "❌ 不支持的操作: " + action + "\n支持的操作: " + String.join(", ",
                    "start", "stop", "status", "navigate", "snapshot", "screenshot",
                    "act", "tabs", "close_tab", "cookie", "storage", "execute_js",
                    "performance", "dialog", "upload", "download"
                );
            };

        } catch (Exception e) {
            log.error("浏览器操作失败", e);
            return "❌ 浏览器操作失败: " + e.getMessage();
        }
    }

    /**
     * 获取操作类型
     */
    private String getAction(Map<String, Object> args) {
        if (args.containsKey("action")) {
            return String.valueOf(args.get("action"));
        }
        // 默认操作：如果只有 profile 参数，则返回 status
        if (args.containsKey("profile") && args.size() == 1) {
            return "status";
        }
        return "status";
    }

    /**
     * 获取 profile 参数
     */
    private String getProfile(Map<String, Object> args) {
        return args.containsKey("profile") ? String.valueOf(args.get("profile")) : "default";
    }

    // ==================== 浏览器生命周期 ====================

    private String handleStart(Map<String, Object> args) {
        String profile = getProfile(args);
        BrowserResponse response = browserProxy.start(profile);

        if (response.success()) {
            return "✅ 浏览器启动成功: profile=" + profile;
        } else {
            return "❌ 浏览器启动失败: " + response.error();
        }
    }

    private String handleStop(Map<String, Object> args) {
        String profile = getProfile(args);
        BrowserResponse response = browserProxy.stop(profile);

        if (response.success()) {
            return "✅ 浏览器已停止: profile=" + profile;
        } else {
            return "❌ 浏览器停止失败: " + response.error();
        }
    }

    private String handleStatus(Map<String, Object> args) {
        String profile = getProfile(args);
        BrowserResponse response = browserProxy.status(profile);

        if (response.success()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.data();
            StringBuilder sb = new StringBuilder();
            sb.append("✅ 浏览器状态:\n");
            sb.append("  profile: ").append(data.get("profile")).append("\n");
            sb.append("  running: ").append(data.get("running")).append("\n");
            if (Boolean.TRUE.equals(data.get("running"))) {
                sb.append("  url: ").append(data.get("currentUrl")).append("\n");
                sb.append("  title: ").append(data.get("title"));
            }
            return sb.toString();
        } else {
            return "❌ 获取状态失败: " + response.error();
        }
    }

    // ==================== 页面操作 ====================

    private String handleNavigate(Map<String, Object> args) {
        String profile = getProfile(args);
        String url = getRequiredArg(args, "url", "URL");

        BrowserResponse response = browserProxy.navigate(profile, url);

        if (response.success()) {
            return "✅ 导航成功: url=" + url;
        } else {
            return "❌ 导航失败: " + response.error();
        }
    }

    private String handleSnapshot(Map<String, Object> args) {
        String profile = getProfile(args);
        boolean includeHtml = getBooleanArg(args, "includeHtml", false);

        BrowserProxy.SnapshotOptions options = new BrowserProxy.SnapshotOptions(
            true,   // aiFormat
            false,  // ariaFormat
            includeHtml
        );

        BrowserResponse response = browserProxy.snapshot(profile, options);

        if (response.success()) {
            return "✅ 页面快照:\n\n" + response.data();
        } else {
            return "❌ 获取快照失败: " + response.error();
        }
    }

    private String handleScreenshot(Map<String, Object> args) {
        String profile = getProfile(args);
        String format = getArg(args, "format", "png");

        BrowserProxy.ScreenshotOptions options = new BrowserProxy.ScreenshotOptions(
            format,
            false,  // fullPage
            80      // quality
        );

        BrowserResponse response = browserProxy.screenshot(profile, options);

        if (response.success()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.data();
            return "✅ 截图成功:\n" +
                   "  path: " + data.get("path") + "\n" +
                   "  format: " + data.get("format") + "\n" +
                   "  size: " + data.get("size") + " bytes";
        } else {
            return "❌ 截图失败: " + response.error();
        }
    }

    // ==================== 元素交互 ====================

    private String handleAct(Map<String, Object> args) {
        String profile = getProfile(args);
        String action = getRequiredArg(args, "action", "操作类型");
        String selector = getArg(args, "selector", "");
        String text = getArg(args, "text", "");
        int timeout = getIntArg(args, "timeout", 30000);

        BrowserParams params = new BrowserParams(
            null,   // url
            text,   // text
            selector,
            null,   // button
            null,   // keys
            null,   // filePath
            null,   // format
            null,   // fullPage
            timeout,
            null    // extra
        );

        BrowserCommand command = new BrowserCommand(action, profile, params);
        BrowserResponse response = browserProxy.act(profile, command);

        if (response.success()) {
            return "✅ 操作成功: action=" + action +
                   (selector != null && !selector.isEmpty() ? ", selector=" + selector : "");
        } else {
            return "❌ 操作失败: " + response.error();
        }
    }

    // ==================== 标签页管理 ====================

    private String handleTabs(Map<String, Object> args) {
        String profile = getProfile(args);
        BrowserResponse response = browserProxy.tabs(profile);

        if (response.success()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.data();
            List<?> tabs = (List<?>) data.get("tabs");

            StringBuilder sb = new StringBuilder();
            sb.append("✅ 标签页列表 (count=").append(tabs.size()).append("):\n");

            for (int i = 0; i < tabs.size(); i++) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tab = (Map<String, Object>) tabs.get(i);
                sb.append(String.format("  [%d] id=%s, url=%s, title=%s\n",
                    i, tab.get("id"), tab.get("url"), tab.get("title")));
            }

            return sb.toString();
        } else {
            return "❌ 获取标签页失败: " + response.error();
        }
    }

    private String handleCloseTab(Map<String, Object> args) {
        String targetId = getRequiredArg(args, "targetId", "标签页ID");
        BrowserResponse response = browserProxy.closeTab(targetId);

        if (response.success()) {
            return "✅ 标签页已关闭: targetId=" + targetId;
        } else {
            return "❌ 关闭标签页失败: " + response.error();
        }
    }

    // ==================== Cookie 和存储 ====================

    private String handleCookie(Map<String, Object> args) {
        String profile = getProfile(args);
        String action = getArg(args, "action", "getAll");

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        StorageManager storageManager = new StorageManager(driver);

        return switch (action) {
            case "getAll" -> {
                List<StorageManager.CookieInfo> cookies = storageManager.getAllCookies();
                StringBuilder sb = new StringBuilder();
                sb.append("✅ Cookies (count=").append(cookies.size()).append("):\n");
                for (StorageManager.CookieInfo cookie : cookies) {
                    sb.append(String.format("  %s=%s (domain=%s)\n",
                        cookie.name(), cookie.value(), cookie.domain()));
                }
                yield sb.toString();
            }
            case "get" -> {
                String name = getRequiredArg(args, "name", "Cookie名称");
                StorageManager.CookieInfo cookie = storageManager.getCookie(name);
                if (cookie != null) {
                    yield "✅ Cookie: " + name + "=" + cookie.value();
                } else {
                    yield "❌ Cookie 不存在: " + name;
                }
            }
            case "delete" -> {
                String name = getRequiredArg(args, "name", "Cookie名称");
                boolean success = storageManager.deleteCookie(name);
                yield success ? "✅ Cookie 已删除: " + name : "❌ 删除失败";
            }
            case "deleteAll" -> {
                int count = storageManager.deleteAllCookies();
                yield "✅ 已删除 " + count + " 个 Cookie";
            }
            default -> "❌ 不支持的 Cookie 操作: " + action;
        };
    }

    private String handleStorage(Map<String, Object> args) {
        String profile = getProfile(args);
        String action = getArg(args, "action", "getAll");

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        StorageManager storageManager = new StorageManager(driver);

        return switch (action) {
            case "getAll" -> {
                Map<String, Object> stats = storageManager.getStorageStats();
                StringBuilder sb = new StringBuilder();
                sb.append("✅ 存储统计:\n");
                sb.append("  cookies: ").append(stats.getOrDefault("cookies", 0)).append("\n");
                sb.append("  localStorage: ").append(stats.getOrDefault("localStorage", 0)).append("\n");
                sb.append("  sessionStorage: ").append(stats.getOrDefault("sessionStorage", 0));
                yield sb.toString();
            }
            case "clearAll" -> {
                Map<String, Integer> result = storageManager.clearAllStorage();
                yield "✅ 存储已清空: " + result;
            }
            case "getLocalStorage" -> {
                String key = getRequiredArg(args, "key", "键名");
                String value = storageManager.getLocalStorageItem(key);
                yield value != null ? "✅ localStorage[" + key + "] = " + value : "❌ 键不存在";
            }
            case "setLocalStorage" -> {
                String key = getRequiredArg(args, "key", "键名");
                String value = getRequiredArg(args, "value", "值");
                boolean success = storageManager.setLocalStorageItem(key, value);
                yield success ? "✅ localStorage 已设置" : "❌ 设置失败";
            }
            default -> "❌ 不支持的存储操作: " + action;
        };
    }

    // ==================== JavaScript 执行 ====================

    private String handleExecuteJs(Map<String, Object> args) {
        String profile = getProfile(args);
        String script = getRequiredArg(args, "script", "JavaScript代码");

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        JavaScriptExecutor jsExecutor = new JavaScriptExecutor(driver);
        JavaScriptExecutor.ScriptResult result = jsExecutor.executeScript(script);

        if (result.success()) {
            return "✅ JavaScript 执行成功:\n  result: " + result.result();
        } else {
            return "❌ JavaScript 执行失败: " + result.error();
        }
    }

    // ==================== 性能监控 ====================

    private String handlePerformance(Map<String, Object> args) {
        String profile = getProfile(args);

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        PerformanceMonitor monitor = new PerformanceMonitor(driver);
        return "✅ 性能报告:\n\n" + monitor.generatePerformanceReport();
    }

    // ==================== 对话框处理 ====================

    private String handleDialog(Map<String, Object> args) {
        String profile = getProfile(args);
        String action = getArg(args, "action", "get");

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        DialogHandler dialogHandler = new DialogHandler(driver);

        return switch (action) {
            case "accept" -> {
                boolean success = dialogHandler.acceptAlert();
                yield success ? "✅ 已接受对话框" : "❌ 操作失败";
            }
            case "dismiss" -> {
                boolean success = dialogHandler.dismissConfirm();
                yield success ? "✅ 已拒绝对话框" : "❌ 操作失败";
            }
            case "get" -> {
                DialogHandler.DialogInfo info = dialogHandler.getDialogInfo();
                if (info.present()) {
                    yield "✅ 对话框信息: type=" + info.type() + ", message=" + info.message();
                } else {
                    yield "❌ 没有检测到对话框";
                }
            }
            default -> "❌ 不支持的对话框操作: " + action;
        };
    }

    // ==================== 文件操作 ====================

    private String handleUpload(Map<String, Object> args) {
        String profile = getProfile(args);
        String selector = getRequiredArg(args, "selector", "文件输入选择器");
        String filePath = getRequiredArg(args, "filePath", "文件路径");

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        FileHandler fileHandler = new FileHandler(driver);
        FileHandler.UploadInfo info = fileHandler.uploadFile(selector, filePath);

        if (info.success()) {
            return "✅ 文件上传成功:\n" +
                   "  fileName: " + info.fileName() + "\n" +
                   "  fileSize: " + info.fileSize() + " bytes";
        } else {
            return "❌ 文件上传失败: " + info.errorMessage();
        }
    }

    private String handleDownload(Map<String, Object> args) {
        String profile = getProfile(args);
        String url = getRequiredArg(args, "url", "下载URL");

        WebDriver driver = browserManager.getDriver(profile);
        if (driver == null) {
            return "❌ 浏览器未启动: profile=" + profile;
        }

        FileHandler fileHandler = new FileHandler(driver);
        FileHandler.DownloadInfo info = fileHandler.downloadFile(url);

        if (info.completed()) {
            return "✅ 文件下载成功:\n" +
                   "  fileName: " + info.fileName() + "\n" +
                   "  fileSize: " + info.fileSize() + " bytes\n" +
                   "  duration: " + info.getDuration() + " ms\n" +
                   "  filePath: " + info.filePath();
        } else {
            return "❌ 文件下载失败: url=" + url;
        }
    }

    // ==================== 辅助方法 ====================

    private String getRequiredArg(Map<String, Object> args, String key, String description) {
        if (!args.containsKey(key)) {
            throw new IllegalArgumentException("缺少参数: " + key + " (" + description + ")");
        }
        return String.valueOf(args.get(key));
    }

    private String getArg(Map<String, Object> args, String key, String defaultValue) {
        if (!args.containsKey(key)) {
            return defaultValue;
        }
        Object value = args.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    private boolean getBooleanArg(Map<String, Object> args, String key, boolean defaultValue) {
        if (!args.containsKey(key)) {
            return defaultValue;
        }
        Object value = args.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int getIntArg(Map<String, Object> args, String key, int defaultValue) {
        if (!args.containsKey(key)) {
            return defaultValue;
        }
        Object value = args.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
