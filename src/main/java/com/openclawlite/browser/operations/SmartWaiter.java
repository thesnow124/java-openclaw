package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 智能等待管理器
 *
 * <p>提供多种等待策略和自定义等待条件。</p>
 *
 * <p>支持的等待类型：</p>
 * <ul>
 *   <li>显式等待（Explicit Wait）</li>
 *   <li>隐式等待（Implicit Wait）</li>
 *   <li>FluentWait（流式等待）</li>
 *   <li>自定义条件等待</li>
 * </ul>
 */
public class SmartWaiter {

    private static final Logger log = LoggerFactory.getLogger(SmartWaiter.class);

    private final WebDriver driver;
    private final ElementLocator elementLocator;
    private final int defaultTimeout;
    private final int defaultPollingInterval;

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public SmartWaiter(WebDriver driver) {
        this(driver, 10, 500);
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param defaultTimeout 默认超时时间（秒）
     * @param defaultPollingInterval 默认轮询间隔（毫秒）
     */
    public SmartWaiter(WebDriver driver, int defaultTimeout, int defaultPollingInterval) {
        this.driver = driver;
        this.elementLocator = new ElementLocator(driver);
        this.defaultTimeout = defaultTimeout;
        this.defaultPollingInterval = defaultPollingInterval;
    }

    /**
     * 等待元素出现（存在于 DOM 中）
     *
     * @param selector 选择器
     * @return WebElement 元素
     */
    public WebElement waitForPresence(String selector) {
        return waitForPresence(selector, defaultTimeout);
    }

    /**
     * 等待元素出现（存在于 DOM 中）
     *
     * @param selector 选择器
     * @param timeout 超时时间（秒）
     * @return WebElement 元素
     */
    public WebElement waitForPresence(String selector, int timeout) {
        log.debug("等待元素出现: selector={}, timeout={}s", selector, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.presenceOfElementLocated(
            createBy(selector)
        ));
    }

    /**
     * 等待元素可见
     *
     * @param selector 选择器
     * @return WebElement 元素
     */
    public WebElement waitForVisibility(String selector) {
        return waitForVisibility(selector, defaultTimeout);
    }

    /**
     * 等待元素可见
     *
     * @param selector 选择器
     * @param timeout 超时时间（秒）
     * @return WebElement 元素
     */
    public WebElement waitForVisibility(String selector, int timeout) {
        log.debug("等待元素可见: selector={}, timeout={}s", selector, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
            createBy(selector)
        ));
    }

    /**
     * 等待元素可点击
     *
     * @param selector 选择器
     * @return WebElement 元素
     */
    public WebElement waitForClickable(String selector) {
        return waitForClickable(selector, defaultTimeout);
    }

    /**
     * 等待元素可点击
     *
     * @param selector 选择器
     * @param timeout 超时时间（秒）
     * @return WebElement 元素
     */
    public WebElement waitForClickable(String selector, int timeout) {
        log.debug("等待元素可点击: selector={}, timeout={}s", selector, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.elementToBeClickable(
            createBy(selector)
        ));
    }

    /**
     * 创建 By 对象（智能检测）
     */
    private By createBy(String selector) {
        // 检测 CSS Selector
        if (selector.matches(".*[.#]\\w+.*")) {
            log.debug("检测为 CSS Selector");
            return By.cssSelector(selector);
        }

        // 检测 XPath
        if (selector.startsWith("/") || selector.startsWith("(")) {
            log.debug("检测为 XPath");
            return By.xpath(selector);
        }

        // 检测 ID
        if (selector.matches("^[a-zA-Z][\\w-]*$") &&
            !selector.matches(".*\\s.*") &&
            !selector.contains(">")) {
            log.debug("检测为 ID");
            return By.id(selector);
        }

        // 默认使用 CSS Selector
        log.debug("默认使用 CSS Selector");
        return By.cssSelector(selector);
    }

    /**
     * 等待元素文本包含指定内容
     *
     * @param selector 选择器
     * @param text 期望的文本内容
     * @return boolean 是否包含
     */
    public boolean waitForTextContains(String selector, String text) {
        return waitForTextContains(selector, text, defaultTimeout);
    }

    /**
     * 等待元素文本包含指定内容
     *
     * @param selector 选择器
     * @param text 期望的文本内容
     * @param timeout 超时时间（秒）
     * @return boolean 是否包含
     */
    public boolean waitForTextContains(String selector, String text, int timeout) {
        log.debug("等待元素文本包含: selector={}, text={}, timeout={}s", selector, text, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(
            createBy(selector),
            text
        ));
    }

    /**
     * 等待元素属性包含指定值
     *
     * @param selector 选择器
     * @param attributeName 属性名
     * @param value 期望的属性值
     * @return boolean 是否包含
     */
    public boolean waitForAttributeContains(String selector, String attributeName, String value) {
        return waitForAttributeContains(selector, attributeName, value, defaultTimeout);
    }

    /**
     * 等待元素属性包含指定值
     *
     * @param selector 选择器
     * @param attributeName 属性名
     * @param value 期望的属性值
     * @param timeout 超时时间（秒）
     * @return boolean 是否包含
     */
    public boolean waitForAttributeContains(String selector, String attributeName, String value, int timeout) {
        log.debug("等待元素属性包含: selector={}, attr={}, value={}, timeout={}s",
                  selector, attributeName, value, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.attributeContains(
            createBy(selector),
            attributeName,
            value
        ));
    }

    /**
     * 等待元素消失
     *
     * @param selector 选择器
     * @return boolean 是否消失
     */
    public boolean waitForInvisibility(String selector) {
        return waitForInvisibility(selector, defaultTimeout);
    }

    /**
     * 等待元素消失
     *
     * @param selector 选择器
     * @param timeout 超时时间（秒）
     * @return boolean 是否消失
     */
    public boolean waitForInvisibility(String selector, int timeout) {
        log.debug("等待元素消失: selector={}, timeout={}s", selector, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(
            createBy(selector)
        ));
    }

    /**
     * 等待页面标题包含指定内容
     *
     * @param title 期望的标题内容
     * @return boolean 是否包含
     */
    public boolean waitForTitleContains(String title) {
        return waitForTitleContains(title, defaultTimeout);
    }

    /**
     * 等待页面标题包含指定内容
     *
     * @param title 期望的标题内容
     * @param timeout 超时时间（秒）
     * @return boolean 是否包含
     */
    public boolean waitForTitleContains(String title, int timeout) {
        log.debug("等待页面标题包含: title={}, timeout={}s", title, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.titleContains(title));
    }

    /**
     * 等待 URL 包含指定内容
     *
     * @param url 期望的 URL 内容
     * @return boolean 是否包含
     */
    public boolean waitForUrlContains(String url) {
        return waitForUrlContains(url, defaultTimeout);
    }

    /**
     * 等待 URL 包含指定内容
     *
     * @param url 期望的 URL 内容
     * @param timeout 超时时间（秒）
     * @return boolean 是否包含
     */
    public boolean waitForUrlContains(String url, int timeout) {
        log.debug("等待 URL 包含: url={}, timeout={}s", url, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.urlContains(url));
    }

    /**
     * 等待页面加载完成
     *
     * @return boolean 是否加载完成
     */
    public boolean waitForPageLoad() {
        return waitForPageLoad(defaultTimeout);
    }

    /**
     * 等待页面加载完成
     *
     * @param timeout 超时时间（秒）
     * @return boolean 是否加载完成
     */
    public boolean waitForPageLoad(int timeout) {
        log.debug("等待页面加载完成: timeout={}s", timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(webDriver -> {
            String readyState = ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").toString();
            return readyState.equals("complete");
        });
    }

    /**
     * 等待 AJAX 请求完成
     *
     * @param timeout 超时时间（秒）
     * @return boolean 是否完成
     */
    public boolean waitForAjaxComplete(int timeout) {
        log.debug("等待 AJAX 请求完成: timeout={}s", timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(webDriver -> {
            return (Boolean) ((JavascriptExecutor) webDriver).executeScript(
                "return (typeof jQuery !== 'undefined') ? (jQuery.active === 0) : true"
            );
        });
    }

    /**
     * 等待自定义条件
     *
     * @param condition 自定义条件函数
     * @param <T> 返回类型
     * @return T 条件结果
     */
    public <T> T waitFor(Function<WebDriver, T> condition) {
        return waitFor(condition, defaultTimeout);
    }

    /**
     * 等待自定义条件
     *
     * @param condition 自定义条件函数
     * @param timeout 超时时间（秒）
     * @param <T> 返回类型
     * @return T 条件结果
     */
    public <T> T waitFor(Function<WebDriver, T> condition, int timeout) {
        log.debug("等待自定义条件: timeout={}s", timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(condition);
    }

    /**
     * FluentWait - 灵活等待
     *
     * @param condition 等待条件
     * @param timeout 超时时间（秒）
     * @param pollingInterval 轮询间隔（毫秒）
     * @param <T> 返回类型
     * @return T 条件结果
     */
    public <T> T fluentWait(Function<WebDriver, T> condition, int timeout, int pollingInterval) {
        log.debug("FluentWait: timeout={}s, polling={}ms", timeout, pollingInterval);

        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(timeout))
            .pollingEvery(Duration.ofMillis(pollingInterval))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .until(condition);
    }

    /**
     * 等待元素数量达到期望值
     *
     * @param selector 选择器
     * @param expectedCount 期望的元素数量
     * @return boolean 是否达到期望数量
     */
    public boolean waitForElementCount(String selector, int expectedCount) {
        return waitForElementCount(selector, expectedCount, defaultTimeout);
    }

    /**
     * 等待元素数量达到期望值
     *
     * @param selector 选择器
     * @param expectedCount 期望的元素数量
     * @param timeout 超时时间（秒）
     * @return boolean 是否达到期望数量
     */
    public boolean waitForElementCount(String selector, int expectedCount, int timeout) {
        log.debug("等待元素数量: selector={}, count={}, timeout={}s",
                  selector, expectedCount, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(webDriver -> {
            List<WebElement> elements = webDriver.findElements(
                createBy(selector)
            );
            return elements.size() >= expectedCount;
        });
    }

    /**
     * 等待元素可滚动
     *
     * @param selector 选择器
     * @return WebElement 可滚动元素
     */
    public WebElement waitForScrollable(String selector) {
        return waitForScrollable(selector, defaultTimeout);
    }

    /**
     * 等待元素可滚动
     *
     * @param selector 选择器
     * @param timeout 超时时间（秒）
     * @return WebElement 可滚动元素
     */
    public WebElement waitForScrollable(String selector, int timeout) {
        log.debug("等待元素可滚动: selector={}, timeout={}s", selector, timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(webDriver -> {
            WebElement element = webDriver.findElement(
                createBy(selector)
            );

            if (element.isDisplayed()) {
                // 检查元素是否可滚动
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                Boolean scrollable = (Boolean) js.executeScript(
                    "var elem = arguments[0];" +
                    "return elem.scrollHeight > elem.clientHeight || " +
                    "       elem.scrollWidth > elem.clientWidth;",
                    element
                );

                if (scrollable) {
                    return element;
                }
            }

            return null;
        });
    }

    /**
     * 硬性等待（不推荐，仅在必要时使用）
     *
     * @param milliseconds 等待时间（毫秒）
     */
    public void hardWait(int milliseconds) {
        try {
            log.debug("硬性等待: {}ms", milliseconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("硬性等待被中断", e);
        }
    }

    /**
     * 设置隐式等待
     *
     * @param timeout 超时时间（秒）
     */
    public void setImplicitWait(int timeout) {
        log.debug("设置隐式等待: {}s", timeout);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
    }

    /**
     * 设置页面加载超时
     *
     * @param timeout 超时时间（秒）
     */
    public void setPageLoadTimeout(int timeout) {
        log.debug("设置页面加载超时: {}s", timeout);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
    }

    /**
     * 设置脚本执行超时
     *
     * @param timeout 超时时间（秒）
     */
    public void setScriptTimeout(int timeout) {
        log.debug("设置脚本执行超时: {}s", timeout);
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeout));
    }
}
