package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;

/**
 * 交互处理器
 *
 * <p>提供增强的鼠标和键盘操作。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>鼠标操作（点击、双击、右键、悬停、拖拽）</li>
 *   <li>键盘操作（组合键、功能键、快捷键）</li>
 *   <li>操作链（Actions Chain）</li>
 *   <li>触摸操作（缩放、滑动）</li>
 * </ul>
 */
public class InteractionHandler {

    private static final Logger log = LoggerFactory.getLogger(InteractionHandler.class);

    private final WebDriver driver;
    private final Actions actions;

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public InteractionHandler(WebDriver driver) {
        this.driver = driver;
        this.actions = new Actions(driver);
    }

    // ==================== 鼠标操作 ====================

    /**
     * 点击元素
     *
     * @param element WebElement 元素
     * @return boolean 是否成功
     */
    public boolean click(WebElement element) {
        try {
            actions.click(element).perform();
            log.debug("点击元素: {}", element);
            return true;
        } catch (Exception e) {
            log.error("点击元素失败", e);
            return false;
        }
    }

    /**
     * 点击元素（使用选择器）
     *
     * @param selector CSS 选择器
     * @return boolean 是否成功
     */
    public boolean click(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return click(element);
        } catch (Exception e) {
            log.error("点击元素失败: selector={}", selector, e);
            return false;
        }
    }

    /**
     * 双击元素
     *
     * @param element WebElement 元素
     * @return boolean 是否成功
     */
    public boolean doubleClick(WebElement element) {
        try {
            actions.doubleClick(element).perform();
            log.debug("双击元素: {}", element);
            return true;
        } catch (Exception e) {
            log.error("双击元素失败", e);
            return false;
        }
    }

    /**
     * 双击元素（使用选择器）
     *
     * @param selector CSS 选择器
     * @return boolean 是否成功
     */
    public boolean doubleClick(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return doubleClick(element);
        } catch (Exception e) {
            log.error("双击元素失败: selector={}", selector, e);
            return false;
        }
    }

    /**
     * 右键点击元素
     *
     * @param element WebElement 元素
     * @return boolean 是否成功
     */
    public boolean rightClick(WebElement element) {
        try {
            actions.contextClick(element).perform();
            log.debug("右键点击元素: {}", element);
            return true;
        } catch (Exception e) {
            log.error("右键点击元素失败", e);
            return false;
        }
    }

    /**
     * 右键点击元素（使用选择器）
     *
     * @param selector CSS 选择器
     * @return boolean 是否成功
     */
    public boolean rightClick(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return rightClick(element);
        } catch (Exception e) {
            log.error("右键点击元素失败: selector={}", selector, e);
            return false;
        }
    }

    /**
     * 鼠标悬停在元素上
     *
     * @param element WebElement 元素
     * @return boolean 是否成功
     */
    public boolean hover(WebElement element) {
        try {
            actions.moveToElement(element).perform();
            log.debug("鼠标悬停: {}", element);
            return true;
        } catch (Exception e) {
            log.error("鼠标悬停失败", e);
            return false;
        }
    }

    /**
     * 鼠标悬停在元素上（使用选择器）
     *
     * @param selector CSS 选择器
     * @return boolean 是否成功
     */
    public boolean hover(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return hover(element);
        } catch (Exception e) {
            log.error("鼠标悬停失败: selector={}", selector, e);
            return false;
        }
    }

    /**
     * 拖拽元素到目标位置
     *
     * @param source 源元素
     * @param target 目标元素
     * @return boolean 是否成功
     */
    public boolean dragAndDrop(WebElement source, WebElement target) {
        try {
            actions.dragAndDrop(source, target).perform();
            log.debug("拖拽元素: {} -> {}", source, target);
            return true;
        } catch (Exception e) {
            log.error("拖拽元素失败", e);
            return false;
        }
    }

    /**
     * 拖拽元素到目标位置（使用选择器）
     *
     * @param sourceSelector 源元素选择器
     * @param targetSelector 目标元素选择器
     * @return boolean 是否成功
     */
    public boolean dragAndDrop(String sourceSelector, String targetSelector) {
        try {
            WebElement source = driver.findElement(By.cssSelector(sourceSelector));
            WebElement target = driver.findElement(By.cssSelector(targetSelector));
            return dragAndDrop(source, target);
        } catch (Exception e) {
            log.error("拖拽元素失败: {} -> {}", sourceSelector, targetSelector, e);
            return false;
        }
    }

    /**
     * 拖拽元素到指定偏移量
     *
     * @param element 元素
     * @param xOffset X 偏移量
     * @param yOffset Y 偏移量
     * @return boolean 是否成功
     */
    public boolean dragAndDropBy(WebElement element, int xOffset, int yOffset) {
        try {
            actions.dragAndDropBy(element, xOffset, yOffset).perform();
            log.debug("拖拽元素到偏移: {} -> ({}, {})", element, xOffset, yOffset);
            return true;
        } catch (Exception e) {
            log.error("拖拽元素到偏移失败", e);
            return false;
        }
    }

    /**
     * 移动鼠标到指定坐标
     *
     * @param xOffset X 偏移量
     * @param yOffset Y 偏移量
     * @return boolean 是否成功
     */
    public boolean moveByOffset(int xOffset, int yOffset) {
        try {
            actions.moveByOffset(xOffset, yOffset).perform();
            log.debug("移动鼠标到偏移: ({}, {})", xOffset, yOffset);
            return true;
        } catch (Exception e) {
            log.error("移动鼠标失败", e);
            return false;
        }
    }

    /**
     * 点击并按住元素
     *
     * @param element WebElement 元素
     * @return boolean 是否成功
     */
    public boolean clickAndHold(WebElement element) {
        try {
            actions.clickAndHold(element).perform();
            log.debug("点击并按住: {}", element);
            return true;
        } catch (Exception e) {
            log.error("点击并按住失败", e);
            return false;
        }
    }

    /**
     * 释放鼠标
     *
     * @return boolean 是否成功
     */
    public boolean release() {
        try {
            actions.release().perform();
            log.debug("释放鼠标");
            return true;
        } catch (Exception e) {
            log.error("释放鼠标失败", e);
            return false;
        }
    }

    // ==================== 键盘操作 ====================

    /**
     * 发送按键
     *
     * @param keys 按键
     * @return boolean 是否成功
     */
    public boolean sendKeys(CharSequence... keys) {
        try {
            actions.sendKeys(keys).perform();
            log.debug("发送按键: {}", Arrays.toString(keys));
            return true;
        } catch (Exception e) {
            log.error("发送按键失败", e);
            return false;
        }
    }

    /**
     * 发送按键到元素
     *
     * @param element WebElement 元素
     * @param keys 按键
     * @return boolean 是否成功
     */
    public boolean sendKeys(WebElement element, CharSequence... keys) {
        try {
            actions.sendKeys(element, keys).perform();
            log.debug("发送按键到元素: {} <- {}", element, Arrays.toString(keys));
            return true;
        } catch (Exception e) {
            log.error("发送按键到元素失败", e);
            return false;
        }
    }

    /**
     * 按下按键
     *
     * @param key 按键
     * @return boolean 是否成功
     */
    public boolean keyDown(Keys key) {
        try {
            actions.keyDown(key).perform();
            log.debug("按下按键: {}", key);
            return true;
        } catch (Exception e) {
            log.error("按下按键失败: {}", key, e);
            return false;
        }
    }

    /**
     * 释放按键
     *
     * @param key 按键
     * @return boolean 是否成功
     */
    public boolean keyUp(Keys key) {
        try {
            actions.keyUp(key).perform();
            log.debug("释放按键: {}", key);
            return true;
        } catch (Exception e) {
            log.error("释放按键失败: {}", key, e);
            return false;
        }
    }

    /**
     * 按下组合键
     *
     * @param keys 按键数组
     * @return boolean 是否成功
     */
    public boolean sendKeyCombo(CharSequence... keys) {
        try {
            // 先按下所有键
            for (CharSequence key : keys) {
                if (key instanceof Keys) {
                    actions.keyDown((Keys) key);
                }
            }
            // 再释放所有键
            for (CharSequence key : keys) {
                if (key instanceof Keys) {
                    actions.keyUp((Keys) key);
                }
            }
            actions.perform();

            log.debug("发送组合键: {}", Arrays.toString(keys));
            return true;
        } catch (Exception e) {
            log.error("发送组合键失败", e);
            return false;
        }
    }

    /**
     * 复制（Ctrl+C / Cmd+C）
     *
     * @return boolean 是否成功
     */
    public boolean copy() {
        // 根据操作系统选择组合键
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            actions.keyDown(Keys.COMMAND).sendKeys("c").keyUp(Keys.COMMAND).perform();
        } else {
            actions.keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();
        }
        return true;
    }

    /**
     * 粘贴（Ctrl+V / Cmd+V）
     *
     * @return boolean 是否成功
     */
    public boolean paste() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            actions.keyDown(Keys.COMMAND).sendKeys("v").keyUp(Keys.COMMAND).perform();
        } else {
            actions.keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL).perform();
        }
        return true;
    }

    /**
     * 全选（Ctrl+A / Cmd+A）
     *
     * @return boolean 是否成功
     */
    public boolean selectAll() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            actions.keyDown(Keys.COMMAND).sendKeys("a").keyUp(Keys.COMMAND).perform();
        } else {
            actions.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
        }
        return true;
    }

    /**
     * 剪切（Ctrl+X / Cmd+X）
     *
     * @return boolean 是否成功
     */
    public boolean cut() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            actions.keyDown(Keys.COMMAND).sendKeys("x").keyUp(Keys.COMMAND).perform();
        } else {
            actions.keyDown(Keys.CONTROL).sendKeys("x").keyUp(Keys.CONTROL).perform();
        }
        return true;
    }

    // ==================== 操作链 ====================

    /**
     * 开始构建操作链
     *
     * @return Actions Actions 对象
     */
    public Actions beginActions() {
        return new Actions(driver);
    }

    /**
     * 执行操作链
     *
     * @param actions Actions 对象
     * @return boolean 是否成功
     */
    public boolean performActions(Actions actions) {
        try {
            actions.perform();
            log.debug("执行操作链");
            return true;
        } catch (Exception e) {
            log.error("执行操作链失败", e);
            return false;
        }
    }

    /**
     * 批量操作示例
     *
     * @param selectors 元素选择器数组
     * @return boolean 是否成功
     */
    public boolean clickMultiple(String... selectors) {
        try {
            Actions actions = new Actions(driver);

            for (String selector : selectors) {
                WebElement element = driver.findElement(By.cssSelector(selector));
                actions.click(element);
                actions.pause(Duration.ofMillis(100)); // 短暂暂停
            }

            actions.perform();
            log.debug("批量点击: {}", Arrays.toString(selectors));
            return true;
        } catch (Exception e) {
            log.error("批量点击失败", e);
            return false;
        }
    }

    // ==================== 高级操作 ====================

    /**
     * 滚动鼠标滚轮
     *
     * @param element WebElement 元素
     * @param deltaX X 滚动量
     * @param deltaY Y 滚动量
     * @return boolean 是否成功
     */
    public boolean scrollWheel(WebElement element, int deltaX, int deltaY) {
        try {
            // 使用 JavaScript 模拟滚轮事件
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "arguments[0].dispatchEvent(new WheelEvent('wheel', {" +
                "  deltaX: arguments[1]," +
                "  deltaY: arguments[2]," +
                "  bubbles: true" +
                "});",
                element, deltaX, deltaY
            );

            log.debug("滚动鼠标滚轮: {} -> ({}, {})", element, deltaX, deltaY);
            return true;
        } catch (Exception e) {
            log.error("滚动鼠标滚轮失败", e);
            return false;
        }
    }

    /**
     * 缩放页面（Ctrl + 滚轮）
     *
     * @param zoomFactor 缩放因子（1.0 = 100%）
     * @return boolean 是否成功
     */
    public boolean zoom(double zoomFactor) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "document.body.style.zoom = arguments[0];",
                zoomFactor
            );

            log.debug("缩放页面: {}", zoomFactor);
            return true;
        } catch (Exception e) {
            log.error("缩放页面失败", e);
            return false;
        }
    }

    /**
     * 触摸操作 - 点击（移动设备）
     *
     * @param element WebElement 元素
     * @return boolean 是否成功
     */
    public boolean tap(WebElement element) {
        try {
            // 直接使用点击
            element.click();
            log.debug("触摸点击: {}", element);
            return true;
        } catch (Exception e) {
            log.error("触摸点击失败", e);
            return false;
        }
    }

    /**
     * 触摸操作 - 滑动
     *
     * @param startX 起始 X 坐标
     * @param startY 起始 Y 坐标
     * @param endX 结束 X 坐标
     * @param endY 结束 Y 坐标
     * @return boolean 是否成功
     */
    public boolean swipe(int startX, int startY, int endX, int endY) {
        try {
            // 使用 Actions 模拟滑动
            actions.moveByOffset(startX, startY)
                   .clickAndHold()
                   .moveByOffset(endX - startX, endY - startY)
                   .release()
                   .perform();

            log.debug("滑动: ({}, {}) -> ({}, {})", startX, startY, endX, endY);
            return true;
        } catch (Exception e) {
            log.error("滑动失败", e);
            return false;
        }
    }

    /**
     * 触摸操作 - 双指缩放
     *
     * @param element WebElement 元素
     * @param scale 缩放比例
     * @return boolean 是否成功
     */
    public boolean pinchZoom(WebElement element, double scale) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "var elem = arguments[0];" +
                "var rect = elem.getBoundingClientRect();" +
                "var centerX = rect.left + rect.width / 2;" +
                "var centerY = rect.top + rect.height / 2;" +
                "" +
                "// 模拟双指缩放（简化版）" +
                "elem.style.transform = 'scale(' + arguments[1] + ')';",
                element, scale
            );

            log.debug("双指缩放: {} -> {}", element, scale);
            return true;
        } catch (Exception e) {
            log.error("双指缩放失败", e);
            return false;
        }
    }
}
