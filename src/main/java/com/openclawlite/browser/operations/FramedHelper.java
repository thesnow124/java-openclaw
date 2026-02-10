package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 框架辅助器
 *
 * <p>处理 iframe 和 Shadow DOM。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>iframe 切换和操作</li>
 *   <li>跨域 iframe 处理</li>
 *   <li>Shadow DOM 元素定位</li>
 *   <li>嵌套 Shadow DOM 遍历</li>
 * </ul>
 */
public class FramedHelper {

    private static final Logger log = LoggerFactory.getLogger(FramedHelper.class);

    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;
    private final int defaultTimeout;

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public FramedHelper(WebDriver driver) {
        this(driver, 10);
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param defaultTimeout 默认超时时间（秒）
     */
    public FramedHelper(WebDriver driver, int defaultTimeout) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
        this.defaultTimeout = defaultTimeout;
    }

    // ==================== iframe 操作 ====================

    /**
     * 切换到 iframe（通过索引）
     *
     * @param index iframe 索引（从 0 开始）
     * @return boolean 是否成功
     */
    public boolean switchToIframeByIndex(int index) {
        try {
            driver.switchTo().frame(index);
            log.debug("切换到 iframe: index={}", index);
            return true;
        } catch (Exception e) {
            log.error("切换到 iframe 失败: index={}", index, e);
            return false;
        }
    }

    /**
     * 切换到 iframe（通过名称或 ID）
     *
     * @param nameOrId iframe 名称或 ID
     * @return boolean 是否成功
     */
    public boolean switchToIframeByNameOrId(String nameOrId) {
        try {
            driver.switchTo().frame(nameOrId);
            log.debug("切换到 iframe: nameOrId={}", nameOrId);
            return true;
        } catch (Exception e) {
            log.error("切换到 iframe 失败: nameOrId={}", nameOrId, e);
            return false;
        }
    }

    /**
     * 切换到 iframe（通过 WebElement）
     *
     * @param iframeElement iframe 元素
     * @return boolean 是否成功
     */
    public boolean switchToIframeByElement(WebElement iframeElement) {
        try {
            driver.switchTo().frame(iframeElement);
            log.debug("切换到 iframe: element={}", iframeElement);
            return true;
        } catch (Exception e) {
            log.error("切换到 iframe 失败", e);
            return false;
        }
    }

    /**
     * 切换到 iframe（通过选择器）
     *
     * @param selector iframe 选择器
     * @return boolean 是否成功
     */
    public boolean switchToIframeBySelector(String selector) {
        try {
            WebElement iframe = driver.findElement(By.cssSelector(selector));
            return switchToIframeByElement(iframe);
        } catch (Exception e) {
            log.error("切换到 iframe 失败: selector={}", selector, e);
            return false;
        }
    }

    /**
     * 等待 iframe 可用并切换
     *
     * @param selector iframe 选择器
     * @return boolean 是否成功
     */
    public boolean waitForIframeAndSwitch(String selector) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
            WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(selector)
            ));

            return switchToIframeByElement(iframe);
        } catch (Exception e) {
            log.error("等待 iframe 并切换失败: selector={}", selector, e);
            return false;
        }
    }

    /**
     * 切换回主页面
     *
     * @return boolean 是否成功
     */
    public boolean switchToDefaultContent() {
        try {
            driver.switchTo().defaultContent();
            log.debug("切换到主页面");
            return true;
        } catch (Exception e) {
            log.error("切换到主页面失败", e);
            return false;
        }
    }

    /**
     * 切换到父 frame
     *
     * @return boolean 是否成功
     */
    public boolean switchToParentFrame() {
        try {
            driver.switchTo().parentFrame();
            log.debug("切换到父 frame");
            return true;
        } catch (Exception e) {
            log.error("切换到父 frame 失败", e);
            return false;
        }
    }

    /**
     * 获取所有 iframe
     *
     * @return List<WebElement> iframe 列表
     */
    public List<WebElement> getAllIframes() {
        try {
            return driver.findElements(By.tagName("iframe"));
        } catch (Exception e) {
            log.error("获取所有 iframe 失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取 iframe 信息
     *
     * @param iframe iframe 元素
     * @return String iframe 信息
     */
    public String getIframeInfo(WebElement iframe) {
        try {
            String id = iframe.getAttribute("id");
            String name = iframe.getAttribute("name");
            String src = iframe.getAttribute("src");
            String title = iframe.getAttribute("title");

            return String.format(
                "iframe[id=%s, name=%s, title=%s, src=%s]",
                id, name, title, src
            );
        } catch (Exception e) {
            return "iframe[info unavailable]";
        }
    }

    // ==================== Shadow DOM 操作 ====================

    /**
     * 在 Shadow DOM 中查找元素
     *
     * @param hostSelector Shadow Host 选择器
     * @param shadowSelector Shadow DOM 内部元素选择器
     * @return WebElement 元素
     */
    public WebElement findElementInShadowDom(String hostSelector, String shadowSelector) {
        try {
            // 使用 JavaScript 查找 Shadow DOM 中的元素
            WebElement element = (WebElement) jsExecutor.executeScript(
                "var host = document.querySelector(arguments[0]);" +
                "if (!host || !host.shadowRoot) {" +
                "  return null;" +
                "}" +
                "return host.shadowRoot.querySelector(arguments[1]);",
                hostSelector, shadowSelector
            );

            if (element != null) {
                log.debug("在 Shadow DOM 中找到元素: host={}, shadow={}", hostSelector, shadowSelector);
            }

            return element;

        } catch (Exception e) {
            log.error("在 Shadow DOM 中查找元素失败: host={}, shadow={}", hostSelector, shadowSelector, e);
            return null;
        }
    }

    /**
     * 在嵌套 Shadow DOM 中查找元素
     *
     * @param selectors 选择器数组（从外到内）
     * @return WebElement 元素
     */
    public WebElement findElementInNestedShadowDom(String... selectors) {
        if (selectors == null || selectors.length < 2) {
            log.error("选择器数组至少需要 2 个元素");
            return null;
        }

        try {
            // 构建查找脚本
            StringBuilder script = new StringBuilder();
            script.append("var element = document;");

            for (int i = 0; i < selectors.length; i++) {
                if (i == 0) {
                    // 第一个选择器是普通 DOM 查询
                    script.append("element = element.querySelector('").append(selectors[i]).append("');");
                } else if (i < selectors.length - 1) {
                    // 中间的选择器是 Shadow Host
                    script.append("if (element) element = element.shadowRoot;");
                    script.append("if (element) element = element.querySelector('").append(selectors[i]).append("');");
                } else {
                    // 最后一个选择器是目标元素
                    script.append("if (element && element.shadowRoot) element = element.shadowRoot;");
                    script.append("if (element) element = element.querySelector('").append(selectors[i]).append("');");
                }
            }

            script.append("return element;");

            WebElement element = (WebElement) jsExecutor.executeScript(script.toString());

            if (element != null) {
                log.debug("在嵌套 Shadow DOM 中找到元素: selectors={}", String.join(", ", selectors));
            }

            return element;

        } catch (Exception e) {
            log.error("在嵌套 Shadow DOM 中查找元素失败", e);
            return null;
        }
    }

    /**
     * 在 Shadow DOM 中查找多个元素
     *
     * @param hostSelector Shadow Host 选择器
     * @param shadowSelector Shadow DOM 内部元素选择器
     * @return List<WebElement> 元素列表
     */
    @SuppressWarnings("unchecked")
    public List<WebElement> findElementsInShadowDom(String hostSelector, String shadowSelector) {
        try {
            List<WebElement> elements = (List<WebElement>) jsExecutor.executeScript(
                "var host = document.querySelector(arguments[0]);" +
                "if (!host || !host.shadowRoot) {" +
                "  return [];" +
                "}" +
                "return Array.from(host.shadowRoot.querySelectorAll(arguments[1]));",
                hostSelector, shadowSelector
            );

            log.debug("在 Shadow DOM 中找到 {} 个元素: host={}, shadow={}",
                     elements.size(), hostSelector, shadowSelector);

            return elements;

        } catch (Exception e) {
            log.error("在 Shadow DOM 中查找多个元素失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取 Shadow DOM 内容
     *
     * @param hostSelector Shadow Host 选择器
     * @return String Shadow DOM HTML 内容
     */
    public String getShadowDomContent(String hostSelector) {
        try {
            String content = (String) jsExecutor.executeScript(
                "var host = document.querySelector(arguments[0]);" +
                "if (!host || !host.shadowRoot) {" +
                "  return null;" +
                "}" +
                "return host.shadowRoot.innerHTML;",
                hostSelector
            );

            if (content != null) {
                log.debug("获取 Shadow DOM 内容: host={}, length={}", hostSelector, content.length());
            }

            return content;

        } catch (Exception e) {
            log.error("获取 Shadow DOM 内容失败: host={}", hostSelector, e);
            return null;
        }
    }

    /**
     * 检查元素是否是 Shadow Host
     *
     * @param element WebElement 元素
     * @return boolean 是否是 Shadow Host
     */
    public boolean isShadowHost(WebElement element) {
        try {
            Boolean result = (Boolean) jsExecutor.executeScript(
                "return arguments[0].shadowRoot !== undefined;",
                element
            );
            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查元素是否在 Shadow DOM 中
     *
     * @param element WebElement 元素
     * @return boolean 是否在 Shadow DOM 中
     */
    public boolean isInShadowDom(WebElement element) {
        try {
            Boolean result = (Boolean) jsExecutor.executeScript(
                "return arguments[0].getRootNode() !== document;",
                element
            );
            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Shadow Root
     *
     * @param hostSelector Shadow Host 选择器
     * @return WebElement Shadow Root（作为返回的 Object）
     */
    public Object getShadowRoot(String hostSelector) {
        try {
            return jsExecutor.executeScript(
                "var host = document.querySelector(arguments[0]);" +
                "return host ? host.shadowRoot : null;",
                hostSelector
            );
        } catch (Exception e) {
            log.error("获取 Shadow Root 失败: host={}", hostSelector, e);
            return null;
        }
    }

    /**
     * 在 Shadow DOM 中执行 JavaScript
     *
     * @param hostSelector Shadow Host 选择器
     * @param script JavaScript 脚本
     * @param Object 返回值
     */
    public Object executeScriptInShadowDom(String hostSelector, String script) {
        try {
            return jsExecutor.executeScript(
                "var host = document.querySelector(arguments[0]);" +
                "if (!host || !host.shadowRoot) {" +
                "  return null;" +
                "}" +
                "return (function() {" +
                "  " + script +
                "}).call(host.shadowRoot);",
                hostSelector
            );
        } catch (Exception e) {
            log.error("在 Shadow DOM 中执行脚本失败: host={}", hostSelector, e);
            return null;
        }
    }

    /**
     * 遍历所有 Shadow DOM
     *
     * @return List<String> Shadow DOM 信息列表
     */
    @SuppressWarnings("unchecked")
    public List<String> traverseAllShadowDoms() {
        try {
            List<Object> results = (List<Object>) jsExecutor.executeScript(
                "var results = [];" +
                "" +
                "function traverseShadowDoms(element, depth) {" +
                "  if (!element) return;" +
                "" +
                "  // 检查是否是 Shadow Host" +
                "  if (element.shadowRoot) {" +
                "    var info = {" +
                "      tagName: element.tagName," +
                "      id: element.id || ''," +
                "      className: element.className || ''," +
                "      depth: depth," +
                "      childCount: element.shadowRoot.children.length" +
                "    };" +
                "    results.push(info);" +
                "" +
                "    // 遍历 Shadow DOM 中的子元素" +
                "    Array.from(element.shadowRoot.children).forEach(function(child) {" +
                "      traverseShadowDoms(child, depth + 1);" +
                "    });" +
                "  }" +
                "" +
                "  // 遍历普通子元素" +
                "  Array.from(element.children).forEach(function(child) {" +
                "    traverseShadowDoms(child, depth);" +
                "  });" +
                "}" +
                "" +
                "traverseShadowDoms(document.body, 0);" +
                "return results;"
            );

            List<String> infoList = new ArrayList<>();

            for (Object obj : results) {
                if (obj instanceof Map) {
                    @SuppressWarnings("rawtypes")
                    Map map = (Map) obj;
                    String info = String.format(
                        "ShadowDOM{tag=%s, id=%s, class=%s, depth=%s, children=%s}",
                        map.get("tagName"),
                        map.get("id"),
                        map.get("className"),
                        map.get("depth"),
                        map.get("childCount")
                    );
                    infoList.add(info);
                }
            }

            log.info("遍历 Shadow DOM: count={}", infoList.size());

            return infoList;

        } catch (Exception e) {
            log.error("遍历所有 Shadow DOM 失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 混合操作 ====================

    /**
     * 在 iframe 中的 Shadow DOM 查找元素
     *
     * @param iframeSelector iframe 选择器
     * @param hostSelector Shadow Host 选择器（iframe 内部）
     * @param shadowSelector Shadow DOM 内部元素选择器
     * @return WebElement 元素
     */
    public WebElement findElementInIframeShadowDom(
            String iframeSelector,
            String hostSelector,
            String shadowSelector) {
        try {
            // 切换到 iframe
            if (!switchToIframeBySelector(iframeSelector)) {
                log.error("切换到 iframe 失败: {}", iframeSelector);
                return null;
            }

            // 在 Shadow DOM 中查找元素
            WebElement element = findElementInShadowDom(hostSelector, shadowSelector);

            // 切换回主页面
            switchToDefaultContent();

            return element;

        } catch (Exception e) {
            log.error("在 iframe 的 Shadow DOM 中查找元素失败", e);
            switchToDefaultContent();
            return null;
        }
    }
}
