package com.openclawlite.browser.core;

import com.openclawlite.browser.model.BrowserCommand;
import com.openclawlite.browser.model.BrowserResponse;
import com.openclawlite.browser.proxy.BrowserProxy;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 浏览器管理器
 *
 * <p>负责浏览器的生命周期管理，包括启动、停止、状态查询等。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>管理多个浏览器实例</li>
 *   *   *   <li>支持配置文件（Profile）</li>
 *   *   *   <li>自动资源清理</li>
   *   * * </ul>
 */
@Component
public class BrowserManager {

    private static final Logger log = LoggerFactory.getLogger(BrowserManager.class);

    // 存储活跃的浏览器实例
    private final Map<String, WebDriver> browsers = new ConcurrentHashMap<>();

    // 存储每个 profile 对应的代理
    private final Map<String, BrowserProxy> proxies = new ConcurrentHashMap<>();

    /**
     * 获取浏览器状态
     */
    public BrowserResponse status(String profile) {
        try {
            WebDriver driver = browsers.get(profile);
            if (driver == null) {
                return BrowserResponse.success("status", Map.of(
                    "profile", profile,
                    "running", false,
                    "message", "浏览器未启动"
                ));
            }

            return BrowserResponse.success("status", Map.of(
                "profile", profile,
                "running", true,
                "currentUrl", driver.getCurrentUrl(),
                "title", driver.getTitle()
            ));

        } catch (Exception e) {
            log.error("获取浏览器状态失败: profile={}", profile, e);
            return BrowserResponse.failure("status", e);
        }
    }

    /**
     * 启动浏览器
     */
    public BrowserResponse start(String profile) {
        try {
            // 检查是否已经启动
            if (browsers.containsKey(profile)) {
                return BrowserResponse.success("start", Map.of(
                    "profile", profile,
                    "message", "浏览器已在运行"
                ));
            }

            log.info("启动浏览器: profile={}", profile);

            // 使用 WebDriverManager 设置 ChromeDriver
            WebDriverManager.chromedriver().setup();

            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // 无头模式
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--window-size=1920,1080");

            // 创建 WebDriver
            WebDriver driver = new ChromeDriver(options);
            browsers.put(profile, driver);

            log.info("浏览器启动成功: profile={}", profile);

            return BrowserResponse.success("start", Map.of(
                "profile", profile,
                "message", "浏览器启动成功"
            ));

        } catch (Exception e) {
            log.error("启动浏览器失败: profile={}", profile, e);
            return BrowserResponse.failure("start", e);
        }
    }

    /**
     * 停止浏览器
     */
    public BrowserResponse stop(String profile) {
        try {
            WebDriver driver = browsers.remove(profile);
            if (driver == null) {
                return BrowserResponse.success("stop", Map.of(
                    "profile", profile,
                    "message", "浏览器未运行"
                ));
            }

            log.info("停止浏览器: profile={}", profile);

            // 退出浏览器
            driver.quit();

            log.info("浏览器已停止: profile={}", profile);

            return BrowserResponse.success("stop", Map.of(
                "profile", profile,
                "message", "浏览器已停止"
            ));

        } catch (Exception e) {
            log.error("停止浏览器失败: profile={}", profile, e);
            return BrowserResponse.failure("stop", e);
        }
    }

    /**
     * 获取 WebDriver 实例
     */
    public WebDriver getDriver(String profile) {
        return browsers.get(profile);
    }

    /**
     * 检查浏览器是否运行
     */
    public boolean isRunning(String profile) {
        return browsers.containsKey(profile);
    }

    /**
     * 清理所有浏览器实例
     */
    public void cleanup() {
        log.info("清理所有浏览器实例...");

        browsers.forEach((profile, driver) -> {
            try {
                driver.quit();
                log.info("已清理浏览器: profile={}", profile);
            } catch (Exception e) {
                log.warn("清理浏览器失败: profile={}", profile, e);
            }
        });

        browsers.clear();
        proxies.clear();
    }
}
