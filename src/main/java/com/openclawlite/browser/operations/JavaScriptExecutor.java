package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaScript 执行器
 *
 * <p>提供 JavaScript 脚本执行能力。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>执行脚本片段</li>
 *   <li>执行带参数的脚本</li>
 *   <li>异步脚本执行</li>
 *   <li>获取脚本返回值</li>
 *   <li>错误处理和超时</li>
 * </ul>
 */
public class JavaScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptExecutor.class);

    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;
    private final int defaultTimeout;

    /**
     * 脚本执行结果
     */
    public record ScriptResult(
        boolean success,         // 是否执行成功
        Object result,           // 返回结果
        String error,            // 错误信息
        long executionTime       // 执行时间（毫秒）
    ) {
        public static ScriptResult success(Object result, long executionTime) {
            return new ScriptResult(true, result, null, executionTime);
        }

        public static ScriptResult failure(String error, long executionTime) {
            return new ScriptResult(false, null, error, executionTime);
        }
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public JavaScriptExecutor(WebDriver driver) {
        this(driver, 10);
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param defaultTimeout 默认超时时间（秒）
     */
    public JavaScriptExecutor(WebDriver driver, int defaultTimeout) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * 执行 JavaScript 脚本
     *
     * @param script JavaScript 脚本
     * @return ScriptResult 执行结果
     */
    public ScriptResult executeScript(String script) {
        return executeScript(script, new Object[0]);
    }

    /**
     * 执行 JavaScript 脚本（带参数）
     *
     * @param script JavaScript 脚本
     * @param args 参数列表
     * @return ScriptResult 执行结果
     */
    public ScriptResult executeScript(String script, Object... args) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("执行 JavaScript 脚本: script={}, args={}", script, args);

            Object result = jsExecutor.executeScript(script, args);
            long executionTime = System.currentTimeMillis() - startTime;

            log.debug("脚本执行成功: time={}ms", executionTime);
            return ScriptResult.success(result, executionTime);

        } catch (JavascriptException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("JavaScript 执行错误: script={}, error={}", script, e.getMessage());
            return ScriptResult.failure(e.getMessage(), executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("脚本执行失败: script={}", script, e);
            return ScriptResult.failure(e.getMessage(), executionTime);
        }
    }

    /**
     * 异步执行 JavaScript 脚本
     *
     * @param script JavaScript 脚本
     * @return ScriptResult 执行结果
     */
    public ScriptResult executeAsyncScript(String script) {
        return executeAsyncScript(script, new Object[0]);
    }

    /**
     * 异步执行 JavaScript 脚本（带参数）
     *
     * @param script JavaScript 脚本
     * @param args 参数列表
     * @return ScriptResult 执行结果
     */
    public ScriptResult executeAsyncScript(String script, Object... args) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("异步执行 JavaScript 脚本: script={}, args={}", script, args);

            Object result = jsExecutor.executeAsyncScript(script, args);
            long executionTime = System.currentTimeMillis() - startTime;

            log.debug("异步脚本执行成功: time={}ms", executionTime);
            return ScriptResult.success(result, executionTime);

        } catch (TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("异步脚本执行超时: script={}", script);
            return ScriptResult.failure("执行超时", executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("异步脚本执行失败: script={}", script, e);
            return ScriptResult.failure(e.getMessage(), executionTime);
        }
    }

    /**
     * 执行脚本并等待条件满足
     *
     * @param script JavaScript 脚本
     * @param timeout 超时时间（秒）
     * @return ScriptResult 执行结果
     */
    public ScriptResult executeScriptAndWait(String script, int timeout) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("执行脚本并等待: script={}, timeout={}s", script, timeout);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            Object result = wait.until(webDriver -> {
                return jsExecutor.executeScript(script);
            });

            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("脚本执行成功（带等待）: time={}ms", executionTime);
            return ScriptResult.success(result, executionTime);

        } catch (TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("脚本执行超时: script={}", script);
            return ScriptResult.failure("执行超时", executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("脚本执行失败: script={}", script, e);
            return ScriptResult.failure(e.getMessage(), executionTime);
        }
    }

    /**
     * 获取页面标题
     *
     * @return String 页面标题
     */
    public String getPageTitle() {
        ScriptResult result = executeScript("return document.title;");
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 获取页面 URL
     *
     * @return String 页面 URL
     */
    public String getPageUrl() {
        ScriptResult result = executeScript("return document.URL;");
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 获取页面 DOM 内容
     *
     * @return String DOM HTML
     */
    public String getPageHtml() {
        ScriptResult result = executeScript("return document.documentElement.outerHTML;");
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 获取页面文本内容
     *
     * @return String 页面文本
     */
    public String getPageText() {
        ScriptResult result = executeScript("return document.body.innerText;");
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 滚动到页面顶部
     *
     * @return ScriptResult 执行结果
     */
    public ScriptResult scrollToTop() {
        return executeScript("window.scrollTo(0, 0);");
    }

    /**
     * 滚动到页面底部
     *
     * @return ScriptResult 执行结果
     */
    public ScriptResult scrollToBottom() {
        return executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /**
     * 滚动到指定位置
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @return ScriptResult 执行结果
     */
    public ScriptResult scrollTo(int x, int y) {
        return executeScript(String.format("window.scrollTo(%d, %d);", x, y));
    }

    /**
     * 滚动元素到视图中
     *
     * @param element WebElement 元素
     * @return ScriptResult 执行结果
     */
    public ScriptResult scrollElementIntoView(WebElement element) {
        return executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /**
     * 获取元素属性
     *
     * @param element WebElement 元素
     * @param attributeName 属性名
     * @return String 属性值
     */
    public String getElementAttribute(WebElement element, String attributeName) {
        ScriptResult result = executeScript(
            "return arguments[0].getAttribute(arguments[1]);",
            element, attributeName
        );
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 设置元素属性
     *
     * @param element WebElement 元素
     * @param attributeName 属性名
     * @param value 属性值
     * @return ScriptResult 执行结果
     */
    public ScriptResult setElementAttribute(WebElement element, String attributeName, String value) {
        return executeScript(
            "arguments[0].setAttribute(arguments[1], arguments[2]);",
            element, attributeName, value
        );
    }

    /**
     * 移除元素属性
     *
     * @param element WebElement 元素
     * @param attributeName 属性名
     * @return ScriptResult 执行结果
     */
    public ScriptResult removeElementAttribute(WebElement element, String attributeName) {
        return executeScript(
            "arguments[0].removeAttribute(arguments[1]);",
            element, attributeName
        );
    }

    /**
     * 获取元素文本内容
     *
     * @param element WebElement 元素
     * @return String 文本内容
     */
    public String getElementText(WebElement element) {
        ScriptResult result = executeScript("return arguments[0].textContent;", element);
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 设置元素文本内容
     *
     * @param element WebElement 元素
     * @param text 文本内容
     * @return ScriptResult 执行结果
     */
    public ScriptResult setElementText(WebElement element, String text) {
        return executeScript("arguments[0].textContent = arguments[1];", element, text);
    }

    /**
     * 获取元素 HTML 内容
     *
     * @param element WebElement 元素
     * @return String HTML 内容
     */
    public String getElementHtml(WebElement element) {
        ScriptResult result = executeScript("return arguments[0].innerHTML;", element);
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 设置元素 HTML 内容
     *
     * @param element WebElement 元素
     * @param html HTML 内容
     * @return ScriptResult 执行结果
     */
    public ScriptResult setElementHtml(WebElement element, String html) {
        return executeScript("arguments[0].innerHTML = arguments[1];", element, html);
    }

    /**
     * 获取元素样式
     *
     * @param element WebElement 元素
     * @param propertyName 样式属性名
     * @return String 样式值
     */
    public String getElementStyle(WebElement element, String propertyName) {
        ScriptResult result = executeScript(
            "return window.getComputedStyle(arguments[0]).getPropertyValue(arguments[1]);",
            element, propertyName
        );
        return result.success() ? (String) result.result() : null;
    }

    /**
     * 高亮元素
     *
     * @param element WebElement 元素
     * @param duration 持续时间（毫秒）
     * @return ScriptResult 执行结果
     */
    public ScriptResult highlightElement(WebElement element, int duration) {
        String script = String.format(
            "var elem = arguments[0];" +
            "var originalStyle = elem.getAttribute('style');" +
            "elem.setAttribute('style', 'background: yellow; border: 2px solid red;');" +
            "setTimeout(function() {" +
            "  if (originalStyle) {" +
            "    elem.setAttribute('style', originalStyle);" +
            "  } else {" +
            "    elem.removeAttribute('style');" +
            "  }" +
            "}, %d);",
            duration
        );
        return executeScript(script, element);
    }

    /**
     * 触发元素事件
     *
     * @param element WebElement 元素
     * @param eventType 事件类型（click, focus, blur 等）
     * @return ScriptResult 执行结果
     */
    public ScriptResult triggerEvent(WebElement element, String eventType) {
        return executeScript(
            "var event = new Event(arguments[1], {bubbles: true});" +
            "arguments[0].dispatchEvent(event);",
            element, eventType
        );
    }

    /**
     * 获取页面性能指标
     *
     * @return Map<String, Object> 性能指标
     */
    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> getPerformanceMetrics() {
        ScriptResult result = executeScript(
            "var perfData = window.performance.timing;" +
            "return {" +
            "  navigationStart: perfData.navigationStart," +
            "  loadEventEnd: perfData.loadEventEnd," +
            "  domComplete: perfData.domComplete," +
            "  domContentLoaded: perfData.domContentLoadedEventEnd," +
            "  pageLoadTime: perfData.loadEventEnd - perfData.navigationStart," +
            "  domLoadTime: perfData.domComplete - perfData.navigationStart" +
            "};"
        );

        if (result.success() && result.result() != null) {
            return (java.util.Map<String, Object>) result.result();
        }
        return new java.util.HashMap<>();
    }

    /**
     * 获取控制台日志
     *
     * @return List<String> 控制台日志列表
     */
    public List<String> getConsoleLogs() {
        // 注意：这需要预先注入日志捕获脚本
        ScriptResult result = executeScript(
            "if (typeof window.consoleLogs === 'undefined') {" +
            "  return [];" +
            "}" +
            "return window.consoleLogs;"
        );

        if (result.success() && result.result() != null) {
            return (List<String>) result.result();
        }
        return new ArrayList<>();
    }

    /**
     * 注入日志捕获脚本
     *
     * @return ScriptResult 执行结果
     */
    public ScriptResult injectConsoleLogger() {
        return executeScript(
            "window.consoleLogs = [];" +
            "['log', 'warn', 'error', 'info'].forEach(function(method) {" +
            "  var original = console[method];" +
            "  console[method] = function() {" +
            "    var args = Array.prototype.slice.call(arguments);" +
            "    window.consoleLogs.push(method + ': ' + args.join(' '));" +
            "    original.apply(console, args);" +
            "  };" +
            "});"
        );
    }

    /**
     * 获取页面所有链接
     *
     * @return List<String> 链接 URL 列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAllLinks() {
        ScriptResult result = executeScript(
            "var links = [];" +
            "var elements = document.getElementsByTagName('a');" +
            "for (var i = 0; i < elements.length; i++) {" +
            "  var href = elements[i].href;" +
            "  if (href) {" +
            "    links.push(href);" +
            "  }" +
            "}" +
            "return links;"
        );

        if (result.success() && result.result() != null) {
            return (List<String>) result.result();
        }
        return new ArrayList<>();
    }

    /**
     * 获取页面所有图片
     *
     * @return List<String> 图片 URL 列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAllImages() {
        ScriptResult result = executeScript(
            "var images = [];" +
            "var elements = document.getElementsByTagName('img');" +
            "for (var i = 0; i < elements.length; i++) {" +
            "  var src = elements[i].src;" +
            "  if (src) {" +
            "    images.push(src);" +
            "  }" +
            "}" +
            "return images;"
        );

        if (result.success() && result.result() != null) {
            return (List<String>) result.result();
        }
        return new ArrayList<>();
    }

    /**
     * 获取页面所有表单
     *
     * @return List<java.util.Map<String, Object>> 表单信息列表
     */
    @SuppressWarnings("unchecked")
    public List<java.util.Map<String, Object>> getAllForms() {
        ScriptResult result = executeScript(
            "var forms = [];" +
            "var elements = document.getElementsByTagName('form');" +
            "for (var i = 0; i < elements.length; i++) {" +
            "  var form = elements[i];" +
            "  forms.push({" +
            "    id: form.id," +
            "    name: form.name," +
            "    action: form.action," +
            "    method: form.method," +
            "    inputs: form.elements.length" +
            "  });" +
            "}" +
            "return forms;"
        );

        if (result.success() && result.result() != null) {
            return (List<java.util.Map<String, Object>>) result.result();
        }
        return new ArrayList<>();
    }
}
