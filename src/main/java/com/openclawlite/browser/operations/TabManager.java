package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 标签页管理器
 *
 * <p>管理浏览器多窗口和标签页操作。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>获取所有窗口/标签页列表</li>
 *   <li>切换到指定窗口/标签页</li>
 *   <li>打开新标签页</li>
 *   <li>关闭标签页</li>
 *   <li>窗口句柄管理</li>
 * </ul>
 */
public class TabManager {

    private static final Logger log = LoggerFactory.getLogger(TabManager.class);

    private final WebDriver driver;
    private final Map<String, String> tabIdMap; // 标签页ID -> 窗口句柄
    private final Map<String, TabInfo> tabInfoMap; // 窗口句柄 -> 标签页信息

    /**
     * 标签页信息
     */
    public record TabInfo(
        String tabId,           // 标签页ID
        String windowHandle,    // 窗口句柄
        String url,             // 当前URL
        String title,           // 页面标题
        long createdAt          // 创建时间
    ) {
        public TabInfo {
            if (tabId == null) {
                tabId = "tab_" + System.currentTimeMillis();
            }
            if (createdAt == 0) {
                createdAt = System.currentTimeMillis();
            }
        }
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public TabManager(WebDriver driver) {
        this.driver = driver;
        this.tabIdMap = new ConcurrentHashMap<>();
        this.tabInfoMap = new ConcurrentHashMap<>();

        // 初始化现有标签页
        initializeExistingTabs();
    }

    /**
     * 初始化现有标签页
     */
    private void initializeExistingTabs() {
        Set<String> windowHandles = driver.getWindowHandles();
        String currentHandle = driver.getWindowHandle();

        for (String handle : windowHandles) {
            try {
                driver.switchTo().window(handle);
                String tabId = "tab_" + handle.substring(0, Math.min(8, handle.length()));
                String url = driver.getCurrentUrl();
                String title = driver.getTitle();

                TabInfo info = new TabInfo(tabId, handle, url, title, System.currentTimeMillis());
                tabIdMap.put(tabId, handle);
                tabInfoMap.put(handle, info);

                log.debug("初始化标签页: tabId={}, url={}", tabId, url);
            } catch (Exception e) {
                log.warn("初始化标签页失败: handle={}", handle, e);
            }
        }

        // 切换回原来的窗口
        try {
            driver.switchTo().window(currentHandle);
        } catch (Exception e) {
            log.warn("切换回原窗口失败", e);
        }
    }

    /**
     * 获取所有标签页
     *
     * @return List<TabInfo> 标签页列表
     */
    public List<TabInfo> getAllTabs() {
        refreshTabInfo();
        return new ArrayList<>(tabInfoMap.values());
    }

    /**
     * 获取当前标签页信息
     *
     * @return TabInfo 当前标签页信息
     */
    public TabInfo getCurrentTab() {
        String currentHandle = driver.getWindowHandle();
        return tabInfoMap.get(currentHandle);
    }

    /**
     * 通过标签页ID获取标签页信息
     *
     * @param tabId 标签页ID
     * @return TabInfo 标签页信息
     */
    public TabInfo getTabById(String tabId) {
        String handle = tabIdMap.get(tabId);
        if (handle == null) {
            return null;
        }
        return tabInfoMap.get(handle);
    }

    /**
     * 切换到指定标签页
     *
     * @param tabId 标签页ID
     * @return boolean 是否切换成功
     */
    public boolean switchToTab(String tabId) {
        String handle = tabIdMap.get(tabId);
        if (handle == null) {
            log.warn("标签页不存在: tabId={}", tabId);
            return false;
        }

        try {
            driver.switchTo().window(handle);
            log.info("切换到标签页: tabId={}", tabId);
            return true;
        } catch (Exception e) {
            log.error("切换标签页失败: tabId={}", tabId, e);
            return false;
        }
    }

    /**
     * 切换到指定窗口句柄
     *
     * @param windowHandle 窗口句柄
     * @return boolean 是否切换成功
     */
    public boolean switchToWindow(String windowHandle) {
        try {
            driver.switchTo().window(windowHandle);
            log.info("切换到窗口: handle={}", windowHandle);
            return true;
        } catch (Exception e) {
            log.error("切换窗口失败: handle={}", windowHandle, e);
            return false;
        }
    }

    /**
     * 切换到新打开的标签页
     *
     * @return String 新标签页ID
     */
    public String switchToNewTab() {
        Set<String> beforeHandles = driver.getWindowHandles();

        // 等待新标签页出现
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(500);
                Set<String> afterHandles = driver.getWindowHandles();

                // 找出新标签页
                afterHandles.removeAll(beforeHandles);
                if (!afterHandles.isEmpty()) {
                    String newHandle = afterHandles.iterator().next();
                    driver.switchTo().window(newHandle);

                    // 创建新标签页信息
                    String tabId = "tab_" + newHandle.substring(0, Math.min(8, newHandle.length()));
                    TabInfo info = new TabInfo(tabId, newHandle, driver.getCurrentUrl(),
                                             driver.getTitle(), System.currentTimeMillis());
                    tabIdMap.put(tabId, newHandle);
                    tabInfoMap.put(newHandle, info);

                    log.info("切换到新标签页: tabId={}", tabId);
                    return tabId;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.error("未检测到新标签页");
        return null;
    }

    /**
     * 打开新标签页
     *
     * @return String 新标签页ID
     */
    public String openNewTab() {
        try {
            // 使用 JavaScript 打开新标签页
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.open('about:blank', '_blank');");

            return switchToNewTab();
        } catch (Exception e) {
            log.error("打开新标签页失败", e);
            return null;
        }
    }

    /**
     * 打开新标签页并导航到指定URL
     *
     * @param url 目标URL
     * @return String 新标签页ID
     */
    public String openNewTab(String url) {
        String tabId = openNewTab();
        if (tabId != null) {
            try {
                driver.navigate().to(url);
                log.info("在新标签页打开URL: tabId={}, url={}", tabId, url);
            } catch (Exception e) {
                log.error("导航失败: url={}", url, e);
            }
        }
        return tabId;
    }

    /**
     * 关闭当前标签页
     *
     * @return boolean 是否关闭成功
     */
    public boolean closeCurrentTab() {
        String currentHandle = driver.getWindowHandle();
        return closeTabByHandle(currentHandle);
    }

    /**
     * 关闭指定标签页
     *
     * @param tabId 标签页ID
     * @return boolean 是否关闭成功
     */
    public boolean closeTab(String tabId) {
        String handle = tabIdMap.get(tabId);
        if (handle == null) {
            log.warn("标签页不存在: tabId={}", tabId);
            return false;
        }

        return closeTabByHandle(handle);
    }

    /**
     * 通过窗口句柄关闭标签页
     *
     * @param windowHandle 窗口句柄
     * @return boolean 是否关闭成功
     */
    public boolean closeTabByHandle(String windowHandle) {
        try {
            // 如果是当前窗口，先切换到其他窗口
            String currentHandle = driver.getWindowHandle();
            if (windowHandle.equals(currentHandle)) {
                Set<String> handles = driver.getWindowHandles();
                handles.remove(windowHandle);

                if (!handles.isEmpty()) {
                    String newHandle = handles.iterator().next();
                    driver.switchTo().window(newHandle);
                }
            }

            // 切换到目标窗口并关闭
            driver.switchTo().window(windowHandle);
            driver.close();

            // 从映射中移除
            TabInfo info = tabInfoMap.remove(windowHandle);
            if (info != null) {
                tabIdMap.remove(info.tabId());
                log.info("关闭标签页: tabId={}", info.tabId());
            }

            return true;
        } catch (Exception e) {
            log.error("关闭标签页失败: handle={}", windowHandle, e);
            return false;
        }
    }

    /**
     * 关闭其他所有标签页
     *
     * @return int 关闭的标签页数量
     */
    public int closeOtherTabs() {
        String currentHandle = driver.getWindowHandle();
        Set<String> handles = new HashSet<>(driver.getWindowHandles());
        handles.remove(currentHandle);

        int closedCount = 0;
        for (String handle : handles) {
            if (closeTabByHandle(handle)) {
                closedCount++;
            }
        }

        log.info("关闭其他标签页: count={}", closedCount);
        return closedCount;
    }

    /**
     * 刷新标签页信息
     */
    private void refreshTabInfo() {
        Set<String> currentHandles = driver.getWindowHandles();

        // 更新现有标签页信息
        for (String handle : currentHandles) {
            try {
                TabInfo info = tabInfoMap.get(handle);
                if (info != null) {
                    // 更新URL和标题
                    String currentTabId = info.tabId();
                    driver.switchTo().window(handle);
                    TabInfo updatedInfo = new TabInfo(
                        currentTabId,
                        handle,
                        driver.getCurrentUrl(),
                        driver.getTitle(),
                        info.createdAt()
                    );
                    tabInfoMap.put(handle, updatedInfo);
                }
            } catch (Exception e) {
                log.warn("刷新标签页信息失败: handle={}", handle, e);
            }
        }

        // 清理已关闭的标签页
        Set<String> knownHandles = new HashSet<>(tabInfoMap.keySet());
        knownHandles.removeAll(currentHandles);
        for (String closedHandle : knownHandles) {
            TabInfo info = tabInfoMap.remove(closedHandle);
            if (info != null) {
                tabIdMap.remove(info.tabId());
            }
        }
    }

    /**
     * 获取标签页数量
     *
     * @return int 标签页数量
     */
    public int getTabCount() {
        return driver.getWindowHandles().size();
    }

    /**
     * 检查标签页是否存在
     *
     * @param tabId 标签页ID
     * @return boolean 是否存在
     */
    public boolean hasTab(String tabId) {
        return tabIdMap.containsKey(tabId);
    }

    /**
     * 通过URL查找标签页
     *
     * @param url URL（支持部分匹配）
     * @return List<TabInfo> 匹配的标签页列表
     */
    public List<TabInfo> findTabsByUrl(String url) {
        List<TabInfo> result = new ArrayList<>();
        for (TabInfo info : tabInfoMap.values()) {
            if (info.url() != null && info.url().contains(url)) {
                result.add(info);
            }
        }
        return result;
    }

    /**
     * 通过标题查找标签页
     *
     * @param title 标题（支持部分匹配）
     * @return List<TabInfo> 匹配的标签页列表
     */
    public List<TabInfo> findTabsByTitle(String title) {
        List<TabInfo> result = new ArrayList<>();
        for (TabInfo info : tabInfoMap.values()) {
            if (info.title() != null && info.title().contains(title)) {
                result.add(info);
            }
        }
        return result;
    }

    /**
     * 获取所有窗口句柄
     *
     * @return Set<String> 窗口句柄集合
     */
    public Set<String> getAllWindowHandles() {
        return driver.getWindowHandles();
    }

    /**
     * 获取当前窗口句柄
     *
     * @return String 当前窗口句柄
     */
    public String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }
}
