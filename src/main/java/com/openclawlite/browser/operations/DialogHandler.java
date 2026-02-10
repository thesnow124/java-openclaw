package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话框处理器
 *
 * <p>处理各种类型的对话框和弹窗。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>Alert/Confirm/Prompt 处理</li>
 *   <li>自定义对话框处理</li>
 *   <li>模态框检测和关闭</li>
 *   <li>Toast 消息处理</li>
 *   <li>弹窗自动响应策略</li>
 *   <li>弹窗内容验证</li>
 * </ul>
 */
public class DialogHandler {

    private static final Logger log = LoggerFactory.getLogger(DialogHandler.class);

    private final WebDriver driver;
    private final int defaultTimeout;

    /**
     * 对话框类型
     */
    public enum DialogType {
        ALERT,          // alert()
        CONFIRM,        // confirm()
        PROMPT,         // prompt()
        MODAL,          // 自定义模态框
        TOAST           // Toast 消息
    }

    /**
     * 对话框信息
     */
    public record DialogInfo(
        DialogType type,            // 对话框类型
        String message,             // 消息内容
        String text,                // 输入文本（仅 Prompt）
        boolean present,            // 是否存在
        String cssSelector          // CSS 选择器（自定义对话框）
    ) {
        public DialogInfo {
            if (text == null) {
                text = "";
            }
        }
    }

    /**
     * 弹窗处理策略
     */
    public static class PopupStrategy {
        private boolean acceptAlerts = true;           // 自动接受 Alert
        private boolean acceptConfirms = true;         // 自动接受 Confirm
        private boolean dismissConfirms = false;       // 自动拒绝 Confirm
        private String promptText = "";                // Prompt 输入文本
        private boolean autoHandle = false;            // 是否自动处理
        private int waitTimeout = 10;                  // 等待超时（秒）

        public PopupStrategy acceptAlerts(boolean accept) {
            this.acceptAlerts = accept;
            return this;
        }

        public PopupStrategy acceptConfirms(boolean accept) {
            this.acceptConfirms = accept;
            this.dismissConfirms = !accept;
            return this;
        }

        public PopupStrategy promptText(String text) {
            this.promptText = text;
            return this;
        }

        public PopupStrategy autoHandle(boolean auto) {
            this.autoHandle = auto;
            return this;
        }

        public PopupStrategy waitTimeout(int timeout) {
            this.waitTimeout = timeout;
            return this;
        }

        // Getters
        public boolean isAcceptAlerts() { return acceptAlerts; }
        public boolean isAcceptConfirms() { return acceptConfirms; }
        public boolean isDismissConfirms() { return dismissConfirms; }
        public String getPromptText() { return promptText; }
        public boolean isAutoHandle() { return autoHandle; }
        public int getWaitTimeout() { return waitTimeout; }
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public DialogHandler(WebDriver driver) {
        this(driver, 10);
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param defaultTimeout 默认超时时间（秒）
     */
    public DialogHandler(WebDriver driver, int defaultTimeout) {
        this.driver = driver;
        this.defaultTimeout = defaultTimeout;
    }

    // ==================== Alert/Confirm/Prompt 处理 ====================

    /**
     * 等待 Alert 出现
     *
     * @return Alert Alert 对象
     */
    public Alert waitForAlert() {
        return waitForAlert(defaultTimeout);
    }

    /**
     * 等待 Alert 出现
     *
     * @param timeout 超时时间（秒）
     * @return Alert Alert 对象
     */
    public Alert waitForAlert(int timeout) {
        log.debug("等待 Alert 出现: timeout={}s", timeout);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    /**
     * 接受 Alert
     *
     * @return boolean 是否成功
     */
    public boolean acceptAlert() {
        try {
            Alert alert = waitForAlert();
            String message = alert.getText();
            alert.accept();
            log.info("接受 Alert: message={}", message);
            return true;
        } catch (TimeoutException e) {
            log.warn("等待 Alert 超时");
            return false;
        } catch (Exception e) {
            log.error("接受 Alert 失败", e);
            return false;
        }
    }

    /**
     * 接受 Confirm
     *
     * @return boolean 是否成功
     */
    public boolean acceptConfirm() {
        try {
            Alert alert = waitForAlert();
            String message = alert.getText();
            alert.accept();
            log.info("接受 Confirm: message={}", message);
            return true;
        } catch (TimeoutException e) {
            log.warn("等待 Confirm 超时");
            return false;
        } catch (Exception e) {
            log.error("接受 Confirm 失败", e);
            return false;
        }
    }

    /**
     * 拒绝 Confirm
     *
     * @return boolean 是否成功
     */
    public boolean dismissConfirm() {
        try {
            Alert alert = waitForAlert();
            String message = alert.getText();
            alert.dismiss();
            log.info("拒绝 Confirm: message={}", message);
            return true;
        } catch (TimeoutException e) {
            log.warn("等待 Confirm 超时");
            return false;
        } catch (Exception e) {
            log.error("拒绝 Confirm 失败", e);
            return false;
        }
    }

    /**
     * 在 Prompt 中输入文本并接受
     *
     * @param text 输入文本
     * @return boolean 是否成功
     */
    public boolean acceptPrompt(String text) {
        try {
            Alert alert = waitForAlert();
            String message = alert.getText();
            alert.sendKeys(text);
            alert.accept();
            log.info("接受 Prompt: message={}, input={}", message, text);
            return true;
        } catch (TimeoutException e) {
            log.warn("等待 Prompt 超时");
            return false;
        } catch (Exception e) {
            log.error("接受 Prompt 失败", e);
            return false;
        }
    }

    /**
     * 拒绝 Prompt
     *
     * @return boolean 是否成功
     */
    public boolean dismissPrompt() {
        try {
            Alert alert = waitForAlert();
            String message = alert.getText();
            alert.dismiss();
            log.info("拒绝 Prompt: message={}", message);
            return true;
        } catch (TimeoutException e) {
            log.warn("等待 Prompt 超时");
            return false;
        } catch (Exception e) {
            log.error("拒绝 Prompt 失败", e);
            return false;
        }
    }

    /**
     * 获取对话框信息
     *
     * @return DialogInfo 对话框信息
     */
    public DialogInfo getDialogInfo() {
        try {
            Alert alert = driver.switchTo().alert();
            String message = alert.getText();

            // 判断类型（通过尝试操作）
            DialogType type = DialogType.ALERT;

            return new DialogInfo(type, message, "", true, null);

        } catch (NoAlertPresentException e) {
            return new DialogInfo(DialogType.ALERT, "", "", false, null);
        } catch (Exception e) {
            log.error("获取对话框信息失败", e);
            return new DialogInfo(DialogType.ALERT, "", "", false, null);
        }
    }

    /**
     * 检查是否存在 Alert
     *
     * @return boolean 是否存在
     */
    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    // ==================== 自定义对话框处理 ====================

    /**
     * 查找所有模态框
     *
     * @return List<WebElement> 模态框元素列表
     */
    public List<WebElement> findModals() {
        List<WebElement> modals = new ArrayList<>();

        try {
            // 常见的模态框选择器
            String[] modalSelectors = {
                ".modal",                // Bootstrap
                ".MuiModal-root",        // Material-UI
                "[role='dialog']",       // ARIA dialog
                ".ant-modal",            // Ant Design
                ".v-dialog",             // Vuetify
                ".swal2-container",      // SweetAlert2
                ".sweetalert",           // SweetAlert
                ".popup",                // 通用 popup
                ".overlay"               // 通用 overlay
            };

            for (String selector : modalSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            modals.add(element);
                        }
                    }
                } catch (Exception e) {
                    // 忽略选择器错误
                }
            }

        } catch (Exception e) {
            log.error("查找模态框失败", e);
        }

        return modals;
    }

    /**
     * 关闭所有模态框
     *
     * @return int 关闭的模态框数量
     */
    public int closeAllModals() {
        int closedCount = 0;
        List<WebElement> modals = findModals();

        for (WebElement modal : modals) {
            try {
                // 尝试多种关闭方式
                // 1. 查找关闭按钮
                String[] closeButtonSelectors = {
                    ".close", ".modal-close", "[data-dismiss='modal']",
                    ".ant-modal-close", ".v-btn__content", ".swal2-close",
                    "button[aria-label='Close']", "button[aria-label='close']"
                };

                boolean closed = false;
                for (String selector : closeButtonSelectors) {
                    try {
                        WebElement closeButton = modal.findElement(By.cssSelector(selector));
                        if (closeButton.isDisplayed()) {
                            closeButton.click();
                            closed = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个选择器
                    }
                }

                // 2. 按 ESC 键
                if (!closed) {
                    modal.sendKeys(Keys.ESCAPE);
                    closed = true;
                }

                if (closed) {
                    closedCount++;
                    log.info("关闭模态框: {}", modal.getTagName());
                }

            } catch (Exception e) {
                log.warn("关闭模态框失败", e);
            }
        }

        return closedCount;
    }

    /**
     * 等待模态框消失
     *
     * @param modalSelector 模态框选择器
     * @return boolean 是否消失
     */
    public boolean waitForModalToDisappear(String modalSelector) {
        return waitForModalToDisappear(modalSelector, defaultTimeout);
    }

    /**
     * 等待模态框消失
     *
     * @param modalSelector 模态框选择器
     * @param timeout 超时时间（秒）
     * @return boolean 是否消失
     */
    public boolean waitForModalToDisappear(String modalSelector, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(modalSelector)
            ));
        } catch (Exception e) {
            log.error("等待模态框消失失败: selector={}", modalSelector, e);
            return false;
        }
    }

    // ==================== Toast 消息处理 ====================

    /**
     * 查找所有 Toast 消息
     *
     * @return List<WebElement> Toast 元素列表
     */
    public List<WebElement> findToasts() {
        List<WebElement> toasts = new ArrayList<>();

        try {
            // 常见的 Toast 选择器
            String[] toastSelectors = {
                ".toast",                    // Bootstrap
                ".Toastify",                // Toastify
                ".ant-message",              // Ant Design
                ".v-snack__wrapper",        // Vuetify
                ".swal2-toast",              // SweetAlert2
                ".notification",             // 通用 notification
                ".alert",                    // 通用 alert
                "[role='alert']",            // ARIA alert
                ".toaster"                   // Angular Toaster
            };

            for (String selector : toastSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            toasts.add(element);
                        }
                    }
                } catch (Exception e) {
                    // 忽略选择器错误
                }
            }

        } catch (Exception e) {
            log.error("查找 Toast 失败", e);
        }

        return toasts;
    }

    /**
     * 等待 Toast 出现
     *
     * @param toastSelector Toast 选择器
     * @return WebElement Toast 元素
     */
    public WebElement waitForToast(String toastSelector) {
        return waitForToast(toastSelector, defaultTimeout);
    }

    /**
     * 等待 Toast 出现
     *
     * @param toastSelector Toast 选择器
     * @param timeout 超时时间（秒）
     * @return WebElement Toast 元素
     */
    public WebElement waitForToast(String toastSelector, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(toastSelector)
            ));
        } catch (Exception e) {
            log.error("等待 Toast 出现失败: selector={}", toastSelector, e);
            return null;
        }
    }

    /**
     * 等待 Toast 消失
     *
     * @param toastSelector Toast 选择器
     * @return boolean 是否消失
     */
    public boolean waitForToastToDisappear(String toastSelector) {
        return waitForToastToDisappear(toastSelector, defaultTimeout);
    }

    /**
     * 等待 Toast 消失
     *
     * @param toastSelector Toast 选择器
     * @param timeout 超时时间（秒）
     * @return boolean 是否消失
     */
    public boolean waitForToastToDisappear(String toastSelector, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(toastSelector)
            ));
        } catch (Exception e) {
            log.error("等待 Toast 消失失败: selector={}", toastSelector, e);
            return false;
        }
    }

    /**
     * 获取所有 Toast 消息文本
     *
     * @return List<String> Toast 消息列表
     */
    public List<String> getToastMessages() {
        List<String> messages = new ArrayList<>();
        List<WebElement> toasts = findToasts();

        for (WebElement toast : toasts) {
            try {
                messages.add(toast.getText());
            } catch (Exception e) {
                // 忽略错误
            }
        }

        return messages;
    }

    // ==================== 自动处理策略 ====================

    /**
     * 使用策略自动处理弹窗
     *
     * @param strategy 处理策略
     * @return boolean 是否成功处理
     */
    public boolean handleWithStrategy(PopupStrategy strategy) {
        if (!strategy.isAutoHandle()) {
            log.debug("自动处理未启用");
            return false;
        }

        try {
            Alert alert = waitForAlert(strategy.getWaitTimeout());

            // 获取消息（类型判断）
            String message = alert.getText();

            // 根据策略处理
            // 注意：无法直接判断对话框类型，需要根据上下文
            // 这里假设所有对话框都按照策略处理

            if (strategy.isAcceptAlerts() || strategy.isAcceptConfirms()) {
                if (!strategy.getPromptText().isEmpty()) {
                    alert.sendKeys(strategy.getPromptText());
                }
                alert.accept();
                log.info("自动接受对话框: message={}", message);
                return true;
            } else if (strategy.isDismissConfirms()) {
                alert.dismiss();
                log.info("自动拒绝对话框: message={}", message);
                return true;
            }

        } catch (TimeoutException e) {
            log.debug("没有检测到对话框");
            return false;
        } catch (Exception e) {
            log.error("自动处理对话框失败", e);
            return false;
        }

        return false;
    }

    // ==================== 工具方法 ====================

    /**
     * 切换到对话框
     *
     * @return Alert 对话框对象
     */
    public Alert switchToAlert() {
        return driver.switchTo().alert();
    }

    /**
     * 切换回默认内容
     *
     * @return DialogHandler 当前对象
     */
    public DialogHandler switchToDefaultContent() {
        driver.switchTo().defaultContent();
        return this;
    }

    /**
     * 切换到指定 iframe
     *
     * @param iframeSelector iframe 选择器
     * @return DialogHandler 当前对象
     */
    public DialogHandler switchToIframe(String iframeSelector) {
        WebElement iframe = driver.findElement(By.cssSelector(iframeSelector));
        driver.switchTo().frame(iframe);
        return this;
    }

    /**
     * 切换回父 frame
     *
     * @return DialogHandler 当前对象
     */
    public DialogHandler switchToParentFrame() {
        driver.switchTo().parentFrame();
        return this;
    }
}
