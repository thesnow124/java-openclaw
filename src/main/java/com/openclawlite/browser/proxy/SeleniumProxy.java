package com.openclawlite.browser.proxy;

import com.openclawlite.browser.core.BrowserManager;
import com.openclawlite.browser.model.BrowserCommand;
import com.openclawlite.browser.model.BrowserParams;
import com.openclawlite.browser.model.BrowserResponse;
import com.openclawlite.browser.operations.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Selenium WebDriver 浏览器代理实现
 *
 * <p>使用 Selenium WebDriver 实现浏览器操作。</p>
 */
@Component
public class SeleniumProxy implements BrowserProxy {

    private static final Logger log = LoggerFactory.getLogger(SeleniumProxy.class);

    private final BrowserManager browserManager;

    // 操作管理器（懒加载）
    private final Map<String, ElementLocator> elementLocators = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, SmartWaiter> smartWaiters = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, TabManager> tabManagers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, StorageManager> storageManagers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, JavaScriptExecutor> jsExecutors = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, RetryHandler> retryHandlers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, PerformanceMonitor> performanceMonitors = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, DialogHandler> dialogHandlers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, InteractionHandler> interactionHandlers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, com.openclawlite.browser.operations.FileHandler> fileHandlers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, FramedHelper> framedHelpers = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, NetworkMonitor> networkMonitors = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public SeleniumProxy(BrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    @Override
    public BrowserResponse status(String profile) {
        return browserManager.status(profile);
    }

    @Override
    public BrowserResponse start(String profile) {
        return browserManager.start(profile);
    }

    @Override
    public BrowserResponse stop(String profile) {
        return browserManager.stop(profile);
    }

    @Override
    public BrowserResponse navigate(String targetId, String url) {
        try {
            WebDriver driver = getDriver(targetId);
            if (driver == null) {
                return BrowserResponse.failure("navigate",
                    "浏览器未启动或标签页不存在: " + targetId);
            }

            log.info("导航到URL: targetId={}, url={}", targetId, url);

            driver.navigate().to(url);

            // 等待页面加载完成
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.urlContains(url));

            return BrowserResponse.success("navigate", Map.of(
                "targetId", targetId,
                "url", driver.getCurrentUrl(),
                "title", driver.getTitle()
            ));

        } catch (TimeoutException e) {
            log.warn("页面加载超时: targetId={}, url={}", targetId, url);
            return BrowserResponse.failure("navigate",
                "页面加载超时: " + e.getMessage());
        } catch (Exception e) {
            log.error("导航失败: targetId={}, url={}", targetId, url, e);
            return BrowserResponse.failure("navigate", e);
        }
    }

    @Override
    public BrowserResponse snapshot(String targetId, SnapshotOptions options) {
        try {
            WebDriver driver = getDriver(targetId);
            if (driver == null) {
                return BrowserResponse.failure("snapshot",
                    "浏览器未启动或标签页不存在: " + targetId);
            }

            log.info("获取页面快照: targetId={}, options={}", targetId, options);

            StringBuilder sb = new StringBuilder();
            sb.append("URL: ").append(driver.getCurrentUrl()).append("\n");
            sb.append("Title: ").append(driver.getTitle()).append("\n\n");

            // 获取页面源码
            if (options.includeHtml()) {
                sb.append("HTML:\n").append(driver.getPageSource()).append("\n\n");
            }

            // 获取页面文本内容
            sb.append("Text Content:\n").append(driver.findElement(By.tagName("body")).getText()).append("\n\n");

            // 如果需要，获取 ARIA 树
            if (options.ariaFormat()) {
                sb.append("ARIA Tree:\n");
                // TODO: 实现 ARIA 树获取
                sb.append("(ARIA 树功能待实现)\n");
            }

            return BrowserResponse.success("snapshot", sb.toString());

        } catch (Exception e) {
            log.error("获取快照失败: targetId={}", targetId, e);
            return BrowserResponse.failure("snapshot", e);
        }
    }

    @Override
    public BrowserResponse screenshot(String targetId, ScreenshotOptions options) {
        try {
            WebDriver driver = getDriver(targetId);
            if (driver == null) {
                return BrowserResponse.failure("screenshot",
                    "浏览器未启动或标签页不存在: " + targetId);
            }

            log.info("截图: targetId={}, options={}", targetId, options);

            // 创建截图目录
            File screenshotDir = new File("screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // 生成截图文件名
            String filename = "screenshot_" + System.currentTimeMillis() + "." + options.format();
            File screenshotFile = new File(screenshotDir, filename);

            // 截图并保存
            File tempScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            org.openqa.selenium.io.FileHandler.copy(tempScreenshot, screenshotFile);

            String path = screenshotFile.getAbsolutePath();

            log.info("截图已保存: {}", path);

            return BrowserResponse.success("screenshot", Map.of(
                "path", path,
                "format", options.format(),
                "size", screenshotFile.length()
            ));

        } catch (Exception e) {
            log.error("截图失败: targetId={}", targetId, e);
            return BrowserResponse.failure("screenshot", e);
        }
    }

    @Override
    public BrowserResponse act(String targetId, BrowserCommand command) {
        try {
            WebDriver driver = getDriver(targetId);
            if (driver == null) {
                return BrowserResponse.failure("act",
                    "浏览器未启动或标签页不存在: " + targetId);
            }

            BrowserParams params = command.params();
            String action = command.action();

            log.info("执行元素操作: targetId={}, action={}", targetId, action);

            return switch (action) {
                case "click" -> handleClick(driver, params);
                case "type" -> handleType(driver, params);
                case "press" -> handlePress(driver, params);
                case "wait" -> handleWait(driver, params);
                default -> BrowserResponse.failure("act",
                    "不支持的操作: " + action);
            };

        } catch (Exception e) {
            log.error("元素操作失败: targetId={}, action={}", targetId, command.action(), e);
            return BrowserResponse.failure("act", e);
        }
    }

    @Override
    public BrowserResponse tabs(String profile) {
        try {
            WebDriver driver = browserManager.getDriver(profile);
            if (driver == null) {
                return BrowserResponse.success("tabs", Map.of(
                    "profile", profile,
                    "tabs", List.of(),
                    "message", "浏览器未启动"
                ));
            }

            TabManager tabManager = getTabManager(profile);
            List<TabManager.TabInfo> tabs = tabManager.getAllTabs();

            // 转换为 Map 格式
            List<Map<String, Object>> tabMaps = new java.util.ArrayList<>();
            for (TabManager.TabInfo tab : tabs) {
                Map<String, Object> tabMap = new java.util.HashMap<>();
                tabMap.put("id", tab.tabId());
                tabMap.put("url", tab.url() != null ? tab.url() : "");
                tabMap.put("title", tab.title() != null ? tab.title() : "");
                tabMap.put("windowHandle", tab.windowHandle());
                tabMap.put("createdAt", tab.createdAt());
                tabMaps.add(tabMap);
            }

            return BrowserResponse.success("tabs", Map.of(
                "profile", profile,
                "tabs", tabMaps,
                "count", tabManager.getTabCount()
            ));

        } catch (Exception e) {
            log.error("获取标签页列表失败: profile={}", profile, e);
            return BrowserResponse.failure("tabs", e);
        }
    }

    @Override
    public BrowserResponse closeTab(String targetId) {
        try {
            // targetId 格式: profile:tabId
            String[] parts = targetId.split(":", 2);
            String profile = parts[0];
            String tabId = parts.length > 1 ? parts[1] : targetId;

            TabManager tabManager = getTabManager(profile);

            boolean success;
            if (tabManager.hasTab(tabId)) {
                success = tabManager.closeTab(tabId);
            } else {
                // 尝试作为窗口句柄关闭
                success = tabManager.closeTabByHandle(tabId);
            }

            if (success) {
                return BrowserResponse.success("closeTab", Map.of(
                    "targetId", targetId,
                    "message", "标签页已关闭"
                ));
            } else {
                return BrowserResponse.failure("closeTab", "标签页不存在或关闭失败: " + targetId);
            }

        } catch (Exception e) {
            log.error("关闭标签页失败: targetId={}", targetId, e);
            return BrowserResponse.failure("closeTab", e);
        }
    }

    /**
     * 获取 WebDriver 实例
     */
    private WebDriver getDriver(String targetId) {
        // 目前简单的实现：假设 targetId 就是 profile
        // TODO: 实现标签页管理后，这里需要根据 targetId 查找对应的 WebDriver
        return browserManager.getDriver(targetId);
    }

    /**
     * 处理点击操作
     */
    private BrowserResponse handleClick(WebDriver driver, BrowserParams params) {
        try {
            if (params.selector() == null) {
                return BrowserResponse.failure("click",
                    "缺少 selector 参数");
            }

            WebElement element = driver.findElement(By.cssSelector(params.selector()));
            element.click();

            return BrowserResponse.success("click", "元素点击成功");

        } catch (NoSuchElementException e) {
            return BrowserResponse.failure("click",
                "未找到元素: " + params.selector());
        } catch (Exception e) {
            return BrowserResponse.failure("click", e);
        }
    }

    /**
     * 处理输入操作
     */
    private BrowserResponse handleType(WebDriver driver, BrowserParams params) {
        try {
            if (params.selector() == null) {
                return BrowserResponse.failure("type",
                    "缺少 selector 参数");
            }

            WebElement element = driver.findElement(By.cssSelector(params.selector()));
            element.clear();
            element.sendKeys(params.text());

            return BrowserResponse.success("type", "文本输入成功");

        } catch (NoSuchElementException e) {
            return BrowserResponse.failure("type",
                "未找到元素: " + params.selector());
        } catch (Exception e) {
            return BrowserResponse.failure("type", e);
        }
    }

    /**
     * 处理按键操作
     */
    private BrowserResponse handlePress(WebDriver driver, BrowserParams params) {
        try {
            if (params.keys() == null) {
                return BrowserResponse.failure("press",
                    "缺少 keys 参数");
            }

            Actions actions = new Actions(driver);
            actions.sendKeys(params.keys());
            actions.perform();

            return BrowserResponse.success("press", "按键成功");

        } catch (Exception e) {
            return BrowserResponse.failure("press", e);
        }
    }

    /**
     * 处理等待操作
     */
    private BrowserResponse handleWait(WebDriver driver, BrowserParams params) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(params.timeout()));

            if (params.selector() != null) {
                WebElement element = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(params.selector())
                    )
                );
                return BrowserResponse.success("wait", "元素已出现: " + params.selector());
            } else {
                Thread.sleep(params.timeout());
                return BrowserResponse.success("wait", "等待完成");
            }

        } catch (TimeoutException e) {
            return BrowserResponse.failure("wait",
                "等待超时: " + params.selector());
        } catch (Exception e) {
            return BrowserResponse.failure("wait", e);
        }
    }

    // ==================== 操作管理器获取方法 ====================

    /**
     * 获取元素定位器
     */
    private ElementLocator getElementLocator(String profile) {
        return elementLocators.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new ElementLocator(driver) : null;
        });
    }

    /**
     * 获取智能等待器
     */
    private SmartWaiter getSmartWaiter(String profile) {
        return smartWaiters.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new SmartWaiter(driver) : null;
        });
    }

    /**
     * 获取标签页管理器
     */
    private TabManager getTabManager(String profile) {
        return tabManagers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new TabManager(driver) : null;
        });
    }

    /**
     * 获取存储管理器
     */
    private StorageManager getStorageManager(String profile) {
        return storageManagers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new StorageManager(driver) : null;
        });
    }

    /**
     * 获取 JavaScript 执行器
     */
    private JavaScriptExecutor getJavaScriptExecutor(String profile) {
        return jsExecutors.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new JavaScriptExecutor(driver) : null;
        });
    }

    /**
     * 获取重试处理器
     */
    private RetryHandler getRetryHandler(String profile) {
        return retryHandlers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new RetryHandler(driver) : null;
        });
    }

    /**
     * 获取性能监控器
     */
    private PerformanceMonitor getPerformanceMonitor(String profile) {
        return performanceMonitors.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new PerformanceMonitor(driver) : null;
        });
    }

    /**
     * 获取对话框处理器
     */
    private DialogHandler getDialogHandler(String profile) {
        return dialogHandlers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new DialogHandler(driver) : null;
        });
    }

    /**
     * 获取交互处理器
     */
    private InteractionHandler getInteractionHandler(String profile) {
        return interactionHandlers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new InteractionHandler(driver) : null;
        });
    }

    /**
     * 获取文件处理器
     */
    private com.openclawlite.browser.operations.FileHandler getFileHandler(String profile) {
        return fileHandlers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            // FileHandler 使用默认下载目录
            return driver != null ? new com.openclawlite.browser.operations.FileHandler(driver) : null;
        });
    }

    /**
     * 获取框架辅助器
     */
    private FramedHelper getFramedHelper(String profile) {
        return framedHelpers.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new FramedHelper(driver) : null;
        });
    }

    /**
     * 获取网络监控器
     */
    private NetworkMonitor getNetworkMonitor(String profile) {
        return networkMonitors.computeIfAbsent(profile, p -> {
            WebDriver driver = browserManager.getDriver(p);
            return driver != null ? new NetworkMonitor(driver) : null;
        });
    }
}
