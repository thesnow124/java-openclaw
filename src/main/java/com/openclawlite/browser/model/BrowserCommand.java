package com.openclawlite.browser.model;

/**
 * 浏览器命令
 *
 * <p>表示对浏览器的操作命令，包含操作类型和参数。</p>
 */
public record BrowserCommand(
    String action,                // 操作类型：start, stop, navigate, click, type, etc.
    String targetId,              // 目标元素或标签页ID
    BrowserParams params         // 命令参数
) {
    public BrowserCommand(String action, String targetId, BrowserParams params) {
        this.action = action;
        this.targetId = targetId;
        this.params = params != null ? params : new BrowserParams();
    }

    public BrowserCommand(String action) {
        this(action, null, new BrowserParams());
    }
}
