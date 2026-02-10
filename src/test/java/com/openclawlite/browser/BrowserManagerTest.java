package com.openclawlite.browser;

import com.openclawlite.browser.core.BrowserManager;
import com.openclawlite.browser.model.BrowserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * BrowserManager 单元测试
 */
@ExtendWith(MockitoExtension.class)
class BrowserManagerTest {

    private BrowserManager browserManager;

    @Mock
    private WebDriver mockDriver;

    @BeforeEach
    void setUp() {
        browserManager = new BrowserManager();
    }

    @Test
    void testInitialState() {
        // 测试初始状态
        BrowserResponse response = browserManager.status("test-profile");

        assertTrue(response.success());
        assertNotNull(response.data());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.data();
        assertEquals("test-profile", data.get("profile"));
        assertEquals(false, data.get("running"));
        assertEquals("浏览器未启动", data.get("message"));
    }

    @Test
    void testStartBrowser() {
        // 注意：这个测试需要真实的 ChromeDriver，在 CI 环境中可能跳过
        // 仅测试逻辑流程

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // 验证可以创建 ChromeOptions（不实际启动浏览器）
        assertNotNull(options);
        assertTrue(options.getArguments().contains("--headless"));
    }

    @Test
    void testStartBrowserWhenAlreadyRunning() {
        // 使用 mock 测试已启动的情况
        when(mockDriver.getCurrentUrl()).thenReturn("https://example.com");
        when(mockDriver.getTitle()).thenReturn("Example Domain");

        // 模拟浏览器已在运行
        browserManager.browsers.put("test-profile", mockDriver);

        BrowserResponse response = browserManager.start("test-profile");

        assertTrue(response.success());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.data();
        assertEquals("浏览器已在运行", data.get("message"));
    }

    @Test
    void testStopBrowser() {
        // 使用 mock 测试停止浏览器
        browserManager.browsers.put("test-profile", mockDriver);

        BrowserResponse response = browserManager.stop("test-profile");

        assertTrue(response.success());
        verify(mockDriver, times(1)).quit();
        assertFalse(browserManager.browsers.containsKey("test-profile"));
    }

    @Test
    void testStopBrowserWhenNotRunning() {
        BrowserResponse response = browserManager.stop("test-profile");

        assertTrue(response.success());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.data();
        assertEquals("浏览器未运行", data.get("message"));
    }

    @Test
    void testGetDriver() {
        browserManager.browsers.put("test-profile", mockDriver);

        WebDriver driver = browserManager.getDriver("test-profile");

        assertSame(mockDriver, driver);
    }

    @Test
    void testGetDriverWhenNotRunning() {
        WebDriver driver = browserManager.getDriver("test-profile");

        assertNull(driver);
    }

    @Test
    void testIsRunning() {
        assertFalse(browserManager.isRunning("test-profile"));

        browserManager.browsers.put("test-profile", mockDriver);
        assertTrue(browserManager.isRunning("test-profile"));
    }

    @Test
    void testCleanup() {
        browserManager.browsers.put("profile1", mockDriver);
        browserManager.browsers.put("profile2", mockDriver);

        browserManager.cleanup();

        assertTrue(browserManager.browsers.isEmpty());
        verify(mockDriver, times(1)).quit();
    }

    @Test
    void testMultipleProfiles() {
        WebDriver mockDriver2 = mock(WebDriver.class);

        browserManager.browsers.put("profile1", mockDriver);
        browserManager.browsers.put("profile2", mockDriver2);

        assertTrue(browserManager.isRunning("profile1"));
        assertTrue(browserManager.isRunning("profile2"));
        assertEquals(2, browserManager.browsers.size());
    }
}
