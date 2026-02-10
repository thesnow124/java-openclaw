package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 元素定位器
 *
 * <p>提供多种元素定位策略和智能回退机制。</p>
 *
 * <p>支持的定位策略：</p>
 * <ul>
 *   <li>CSS Selector</li>
 *   <li>XPath</li>
 *   <li>文本内容</li>
 *   <li>ARIA 属性</li>
 *   <li>智能混合定位</li>
 * </ul>
 */
public class ElementLocator {

    private static final Logger log = LoggerFactory.getLogger(ElementLocator.class);

    private final WebDriver driver;
    private final int defaultTimeout;

    /**
     * 定位策略枚举
     */
    public enum LocatorStrategy {
        CSS,           // CSS Selector
        XPATH,         // XPath
        TEXT,          // 文本内容
        ARIA,          // ARIA 属性
        ID,            // ID 属性
        NAME,          // Name 属性
        CLASS,         // Class 属性
        TAG,           // Tag 名称
        SMART          // 智能混合定位
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public ElementLocator(WebDriver driver) {
        this(driver, 10);
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param defaultTimeout 默认超时时间（秒）
     */
    public ElementLocator(WebDriver driver, int defaultTimeout) {
        this.driver = driver;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * 查找单个元素
     *
     * @param selector 选择器字符串
     * @return WebElement 元素
     * @throws NoSuchElementException 如果未找到元素
     */
    public WebElement findElement(String selector) {
        return findElement(selector, LocatorStrategy.SMART);
    }

    /**
     * 使用指定策略查找单个元素
     *
     * @param selector 选择器字符串
     * @param strategy 定位策略
     * @return WebElement 元素
     * @throws NoSuchElementException 如果未找到元素
     */
    public WebElement findElement(String selector, LocatorStrategy strategy) {
        log.debug("查找元素: selector={}, strategy={}", selector, strategy);

        try {
            By by = createBy(selector, strategy);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
            return wait.until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (TimeoutException e) {
            log.error("元素查找超时: selector={}, strategy={}", selector, strategy);
            throw new NoSuchElementException("未找到元素: " + selector + " (策略: " + strategy + ")");
        }
    }

    /**
     * 查找多个元素
     *
     * @param selector 选择器字符串
     * @return List<WebElement> 元素列表
     */
    public List<WebElement> findElements(String selector) {
        return findElements(selector, LocatorStrategy.SMART);
    }

    /**
     * 使用指定策略查找多个元素
     *
     * @param selector 选择器字符串
     * @param strategy 定位策略
     * @return List<WebElement> 元素列表
     */
    public List<WebElement> findElements(String selector, LocatorStrategy strategy) {
        log.debug("查找多个元素: selector={}, strategy={}", selector, strategy);

        try {
            By by = createBy(selector, strategy);
            return driver.findElements(by);
        } catch (Exception e) {
            log.warn("查找元素列表失败: selector={}, strategy={}", selector, strategy, e);
            return new ArrayList<>();
        }
    }

    /**
     * 等待元素可见并可交互
     *
     * @param selector 选择器字符串
     * @return WebElement 可交互元素
     * @throws TimeoutException 如果元素未在超时时间内变为可交互状态
     */
    public WebElement waitForInteractable(String selector) {
        return waitForInteractable(selector, LocatorStrategy.SMART);
    }

    /**
     * 使用指定策略等待元素可见并可交互
     *
     * @param selector 选择器字符串
     * @param strategy 定位策略
     * @return WebElement 可交互元素
     * @throws TimeoutException 如果元素未在超时时间内变为可交互状态
     */
    public WebElement waitForInteractable(String selector, LocatorStrategy strategy) {
        log.debug("等待元素可交互: selector={}, strategy={}", selector, strategy);

        By by = createBy(selector, strategy);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));

        return wait.until(driver -> {
            try {
                WebElement element = driver.findElement(by);

                // 检查元素是否存在
                if (element == null) {
                    return null;
                }

                // 检查元素是否显示
                if (!element.isDisplayed()) {
                    return null;
                }

                // 检查元素是否启用
                if (!element.isEnabled()) {
                    return null;
                }

                return element;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                return null;
            }
        });
    }

    /**
     * 智能查找元素（多策略回退）
     *
     * <p>按优先级尝试多种定位策略，直到找到元素。</p>
     *
     * @param selector 选择器字符串
     * @return WebElement 元素
     * @throws NoSuchElementException 如果所有策略都失败
     */
    public WebElement smartFindElement(String selector) {
        // 按优先级尝试的策略列表
        LocatorStrategy[] strategies = {
            LocatorStrategy.CSS,
            LocatorStrategy.XPATH,
            LocatorStrategy.TEXT,
            LocatorStrategy.ARIA,
            LocatorStrategy.ID,
            LocatorStrategy.NAME,
            LocatorStrategy.CLASS
        };

        List<Exception> exceptions = new ArrayList<>();

        for (LocatorStrategy strategy : strategies) {
            try {
                WebElement element = findElement(selector, strategy);
                log.info("成功使用 {} 策略找到元素: {}", strategy, selector);
                return element;
            } catch (NoSuchElementException e) {
                exceptions.add(e);
                log.debug("{} 策略失败，尝试下一个策略", strategy);
            }
        }

        // 所有策略都失败
        String errorMsg = String.format("所有定位策略都失败: %s", selector);
        log.error(errorMsg);
        throw new NoSuchElementException(errorMsg);
    }

    /**
     * 根据策略创建 By 对象
     *
     * @param selector 选择器字符串
     * @param strategy 定位策略
     * @return By Selenium By 对象
     */
    private By createBy(String selector, LocatorStrategy strategy) {
        return switch (strategy) {
            case CSS -> By.cssSelector(selector);
            case XPATH -> By.xpath(selector);
            case TEXT -> createTextLocator(selector);
            case ARIA -> createAriaLocator(selector);
            case ID -> By.id(selector);
            case NAME -> By.name(selector);
            case CLASS -> By.className(selector);
            case TAG -> By.tagName(selector);
            case SMART -> createSmartBy(selector);
        };
    }

    /**
     * 创建文本定位器
     *
     * @param text 文本内容
     * @return By XPath 定位器
     */
    private By createTextLocator(String text) {
        // 精确匹配文本
        return By.xpath(".//*[text()='" + text + "']");
    }

    /**
     * 创建 ARIA 属性定位器
     *
     * @param ariaSelector ARIA 选择器（格式：aria-label="xxx" 或 role="xxx"）
     * @return By XPath 定位器
     */
    private By createAriaLocator(String ariaSelector) {
        // 解析 ARIA 选择器
        if (ariaSelector.contains("=")) {
            String[] parts = ariaSelector.split("=", 2);
            String attribute = parts[0].trim();
            String value = parts[1].trim().replace("\"", "").replace("'", "");

            return By.xpath(".//*[@" + attribute + "='" + value + "']");
        } else {
            // 默认使用 aria-label
            return By.xpath(".//*[@aria-label='" + ariaSelector + "']");
        }
    }

    /**
     * 智能创建 By 对象（自动检测选择器类型）
     *
     * @param selector 选择器字符串
     * @return By Selenium By 对象
     */
    private By createSmartBy(String selector) {
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
     * 检查元素是否在视口中可见
     *
     * @param element WebElement 元素
     * @return boolean 是否可见
     */
    public boolean isInViewport(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (Boolean) js.executeScript(
            "var elem = arguments[0], " +
            "    box = elem.getBoundingClientRect(), " +
            "    cx = box.left + box.width / 2, " +
            "    cy = box.top + box.height / 2, " +
            "    e = document.elementFromPoint(cx, cy); " +
            "return e ? elem.contains(e) || e === elem : false;",
            element
        );
    }

    /**
     * 滚动到元素
     *
     * @param element WebElement 元素
     */
    public void scrollToElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /**
     * 获取元素的 ARIA 树信息
     *
     * @param element WebElement 元素
     * @return String ARIA 树信息
     */
    public String getAriaTree(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 获取元素的 ARIA 属性
        StringBuilder ariaInfo = new StringBuilder();
        ariaInfo.append("Tag: ").append(element.getTagName()).append("\n");

        // 获取常用 ARIA 属性
        String[] ariaAttrs = {
            "role", "aria-label", "aria-labelledby", "aria-describedby",
            "aria-hidden", "aria-expanded", "aria-checked", "aria-pressed",
            "aria-selected", "aria-disabled", "aria-required", "aria-readonly"
        };

        for (String attr : ariaAttrs) {
            String value = element.getAttribute(attr);
            if (value != null && !value.isEmpty()) {
                ariaInfo.append(attr).append(": ").append(value).append("\n");
            }
        }

        return ariaInfo.toString();
    }
}
