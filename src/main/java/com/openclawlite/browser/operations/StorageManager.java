package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 存储管理器
 *
 * <p>管理 Cookie、LocalStorage 和 SessionStorage。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>Cookie 增删改查</li>
 *   <li>LocalStorage 操作</li>
 *   <li>SessionStorage 操作</li>
 *   <li>导出/导入 Cookie</li>
 *   <li>存储清理</li>
 * </ul>
 */
public class StorageManager {

    private static final Logger log = LoggerFactory.getLogger(StorageManager.class);

    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;

    /**
     * Cookie 信息
     */
    public record CookieInfo(
        String name,             // Cookie 名称
        String value,            // Cookie 值
        String domain,           // 域名
        String path,             // 路径
        Date expiry,             // 过期时间
        boolean secure,          // 是否安全
        boolean httpOnly         // 是否仅 HTTP
    ) {
        public String toJson() {
            return String.format(
                "{\"name\":\"%s\",\"value\":\"%s\",\"domain\":\"%s\",\"path\":\"%s\",\"expiry\":%s,\"secure\":%s,\"httpOnly\":%s}",
                name, value, domain, path,
                expiry != null ? expiry.getTime() : "null",
                secure, httpOnly
            );
        }
    }

    /**
     * LocalStorage 项目
     */
    public record StorageItem(
        String key,              // 键
        String value             // 值
    ) {
        public String toJson() {
            return String.format("{\"key\":\"%s\",\"value\":\"%s\"}", key, value);
        }
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public StorageManager(WebDriver driver) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    // ==================== Cookie 操作 ====================

    /**
     * 获取所有 Cookie
     *
     * @return List<CookieInfo> Cookie 列表
     */
    public List<CookieInfo> getAllCookies() {
        Set<Cookie> cookies = driver.manage().getCookies();
        return cookies.stream()
            .map(this::toCookieInfo)
            .collect(Collectors.toList());
    }

    /**
     * 获取指定名称的 Cookie
     *
     * @param name Cookie 名称
     * @return CookieInfo Cookie 信息，不存在返回 null
     */
    public CookieInfo getCookie(String name) {
        Cookie cookie = driver.manage().getCookieNamed(name);
        return cookie != null ? toCookieInfo(cookie) : null;
    }

    /**
     * 添加 Cookie
     *
     * @param name Cookie 名称
     * @param value Cookie 值
     * @return boolean 是否添加成功
     */
    public boolean addCookie(String name, String value) {
        return addCookie(name, value, null, null, null, false, false);
    }

    /**
     * 添加 Cookie（完整参数）
     *
     * @param name Cookie 名称
     * @param value Cookie 值
     * @param domain 域名
     * @param path 路径
     * @param expiry 过期时间
     * @param secure 是否安全
     * @param httpOnly 是否仅 HTTP
     * @return boolean 是否添加成功
     */
    public boolean addCookie(String name, String value, String domain, String path,
                            Date expiry, boolean secure, boolean httpOnly) {
        try {
            Cookie cookie;

            // 如果有过期时间，使用完整构造函数
            if (expiry != null) {
                cookie = new Cookie(name, value, domain, path, expiry, secure, httpOnly);
            } else {
                // 使用 Builder
                Cookie.Builder builder = new Cookie.Builder(name, value);

                if (domain != null) {
                    builder = builder.domain(domain);
                }
                if (path != null) {
                    builder = builder.path(path);
                }
                if (secure) {
                    builder = builder.isSecure(true);
                }
                if (httpOnly) {
                    builder = builder.isHttpOnly(true);
                }

                cookie = builder.build();
            }

            driver.manage().addCookie(cookie);
            log.debug("添加 Cookie: name={}, domain={}", name, domain);
            return true;
        } catch (Exception e) {
            log.error("添加 Cookie 失败: name={}", name, e);
            return false;
        }
    }

    /**
     * 删除指定名称的 Cookie
     *
     * @param name Cookie 名称
     * @return boolean 是否删除成功
     */
    public boolean deleteCookie(String name) {
        try {
            driver.manage().deleteCookieNamed(name);
            log.debug("删除 Cookie: name={}", name);
            return true;
        } catch (Exception e) {
            log.error("删除 Cookie 失败: name={}", name, e);
            return false;
        }
    }

    /**
     * 删除所有 Cookie
     *
     * @return int 删除的 Cookie 数量
     */
    public int deleteAllCookies() {
        try {
            Set<Cookie> cookies = driver.manage().getCookies();
            int count = cookies.size();
            driver.manage().deleteAllCookies();
            log.info("删除所有 Cookie: count={}", count);
            return count;
        } catch (Exception e) {
            log.error("删除所有 Cookie 失败", e);
            return 0;
        }
    }

    /**
     * 导出所有 Cookie 为 JSON
     *
     * @return String JSON 格式的 Cookie 数据
     */
    public String exportCookiesAsJson() {
        List<CookieInfo> cookies = getAllCookies();
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < cookies.size(); i++) {
            json.append("  ").append(cookies.get(i).toJson());
            if (i < cookies.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    /**
     * 导入 Cookie（从 JSON）
     *
     * @param jsonCookie JSON 格式的 Cookie 数据
     * @return boolean 是否导入成功
     */
    public boolean importCookieFromJson(String jsonCookie) {
        try {
            // 简化的 JSON 解析
            Map<String, Object> cookieData = new Json().toType(jsonCookie, Map.class);
            String name = (String) cookieData.get("name");
            String value = (String) cookieData.get("value");
            String domain = (String) cookieData.get("domain");
            String path = (String) cookieData.get("path");

            Long expiryTime = (Long) cookieData.get("expiry");
            Date expiry = expiryTime != null ? new Date(expiryTime) : null;

            Boolean secure = (Boolean) cookieData.get("secure");
            Boolean httpOnly = (Boolean) cookieData.get("httpOnly");

            return addCookie(name, value, domain, path, expiry,
                           secure != null ? secure : false,
                           httpOnly != null ? httpOnly : false);
        } catch (Exception e) {
            log.error("导入 Cookie 失败: json={}", jsonCookie, e);
            return false;
        }
    }

    // ==================== LocalStorage 操作 ====================

    /**
     * 获取所有 LocalStorage 项目
     *
     * @return List<StorageItem> LocalStorage 项目列表
     */
    @SuppressWarnings("unchecked")
    public List<StorageItem> getAllLocalStorage() {
        try {
            int length = ((Long) jsExecutor.executeScript(
                "return window.localStorage.length;")).intValue();

            List<StorageItem> items = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                String key = (String) jsExecutor.executeScript(
                    "return window.localStorage.key(" + i + ");");
                String value = (String) jsExecutor.executeScript(
                    "return window.localStorage.getItem(arguments[0]);", key);

                items.add(new StorageItem(key, value));
            }

            return items;
        } catch (Exception e) {
            log.error("获取 LocalStorage 失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取 LocalStorage 项目
     *
     * @param key 键
     * @return String 值，不存在返回 null
     */
    public String getLocalStorageItem(String key) {
        try {
            return (String) jsExecutor.executeScript(
                "return window.localStorage.getItem(arguments[0]);", key);
        } catch (Exception e) {
            log.error("获取 LocalStorage 失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 设置 LocalStorage 项目
     *
     * @param key 键
     * @param value 值
     * @return boolean 是否设置成功
     */
    public boolean setLocalStorageItem(String key, String value) {
        try {
            jsExecutor.executeScript(
                "window.localStorage.setItem(arguments[0], arguments[1]);", key, value);
            log.debug("设置 LocalStorage: key={}", key);
            return true;
        } catch (Exception e) {
            log.error("设置 LocalStorage 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 删除 LocalStorage 项目
     *
     * @param key 键
     * @return boolean 是否删除成功
     */
    public boolean removeLocalStorageItem(String key) {
        try {
            jsExecutor.executeScript(
                "window.localStorage.removeItem(arguments[0]);", key);
            log.debug("删除 LocalStorage: key={}", key);
            return true;
        } catch (Exception e) {
            log.error("删除 LocalStorage 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 清空所有 LocalStorage
     *
     * @return int 清空的项目数量
     */
    public int clearLocalStorage() {
        try {
            int length = getAllLocalStorage().size();
            jsExecutor.executeScript("window.localStorage.clear();");
            log.info("清空 LocalStorage: count={}", length);
            return length;
        } catch (Exception e) {
            log.error("清空 LocalStorage 失败", e);
            return 0;
        }
    }

    /**
     * 获取 LocalStorage 大小
     *
     * @return int 项目数量
     */
    public int getLocalStorageLength() {
        try {
            return ((Long) jsExecutor.executeScript(
                "return window.localStorage.length;")).intValue();
        } catch (Exception e) {
            log.error("获取 LocalStorage 大小失败", e);
            return 0;
        }
    }

    // ==================== SessionStorage 操作 ====================

    /**
     * 获取所有 SessionStorage 项目
     *
     * @return List<StorageItem> SessionStorage 项目列表
     */
    @SuppressWarnings("unchecked")
    public List<StorageItem> getAllSessionStorage() {
        try {
            int length = ((Long) jsExecutor.executeScript(
                "return window.sessionStorage.length;")).intValue();

            List<StorageItem> items = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                String key = (String) jsExecutor.executeScript(
                    "return window.sessionStorage.key(" + i + ");");
                String value = (String) jsExecutor.executeScript(
                    "return window.sessionStorage.getItem(arguments[0]);", key);

                items.add(new StorageItem(key, value));
            }

            return items;
        } catch (Exception e) {
            log.error("获取 SessionStorage 失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取 SessionStorage 项目
     *
     * @param key 键
     * @return String 值，不存在返回 null
     */
    public String getSessionStorageItem(String key) {
        try {
            return (String) jsExecutor.executeScript(
                "return window.sessionStorage.getItem(arguments[0]);", key);
        } catch (Exception e) {
            log.error("获取 SessionStorage 失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 设置 SessionStorage 项目
     *
     * @param key 键
     * @param value 值
     * @return boolean 是否设置成功
     */
    public boolean setSessionStorageItem(String key, String value) {
        try {
            jsExecutor.executeScript(
                "window.sessionStorage.setItem(arguments[0], arguments[1]);", key, value);
            log.debug("设置 SessionStorage: key={}", key);
            return true;
        } catch (Exception e) {
            log.error("设置 SessionStorage 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 删除 SessionStorage 项目
     *
     * @param key 键
     * @return boolean 是否删除成功
     */
    public boolean removeSessionStorageItem(String key) {
        try {
            jsExecutor.executeScript(
                "window.sessionStorage.removeItem(arguments[0]);", key);
            log.debug("删除 SessionStorage: key={}", key);
            return true;
        } catch (Exception e) {
            log.error("删除 SessionStorage 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 清空所有 SessionStorage
     *
     * @return int 清空的项目数量
     */
    public int clearSessionStorage() {
        try {
            int length = getAllSessionStorage().size();
            jsExecutor.executeScript("window.sessionStorage.clear();");
            log.info("清空 SessionStorage: count={}", length);
            return length;
        } catch (Exception e) {
            log.error("清空 SessionStorage 失败", e);
            return 0;
        }
    }

    /**
     * 获取 SessionStorage 大小
     *
     * @return int 项目数量
     */
    public int getSessionStorageLength() {
        try {
            return ((Long) jsExecutor.executeScript(
                "return window.sessionStorage.length;")).intValue();
        } catch (Exception e) {
            log.error("获取 SessionStorage 大小失败", e);
            return 0;
        }
    }

    // ==================== 综合操作 ====================

    /**
     * 清空所有存储（Cookie、LocalStorage、SessionStorage）
     *
     * @return Map<String, Integer> 清理统计
     */
    public Map<String, Integer> clearAllStorage() {
        Map<String, Integer> stats = new HashMap<>();

        int cookies = deleteAllCookies();
        stats.put("cookies", cookies);

        int localStorage = clearLocalStorage();
        stats.put("localStorage", localStorage);

        int sessionStorage = clearSessionStorage();
        stats.put("sessionStorage", sessionStorage);

        log.info("清空所有存储: {}", stats);
        return stats;
    }

    /**
     * 获取存储统计信息
     *
     * @return Map<String, Object> 统计信息
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("cookies", getAllCookies().size());
        stats.put("localStorage", getLocalStorageLength());
        stats.put("sessionStorage", getSessionStorageLength());
        stats.put("url", driver.getCurrentUrl());
        stats.put("timestamp", Instant.now().toString());

        return stats;
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 Selenium Cookie 转换为 CookieInfo
     *
     * @param cookie Selenium Cookie
     * @return CookieInfo Cookie 信息
     */
    private CookieInfo toCookieInfo(Cookie cookie) {
        return new CookieInfo(
            cookie.getName(),
            cookie.getValue(),
            cookie.getDomain(),
            cookie.getPath(),
            cookie.getExpiry(),
            cookie.isSecure(),
            cookie.isHttpOnly()
        );
    }
}
