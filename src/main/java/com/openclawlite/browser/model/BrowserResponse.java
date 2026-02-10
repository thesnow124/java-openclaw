package com.openclawlite.browser.model;

/**
 * 浏览器响应
 *
 * <p>表示浏览器操作的结果，包含成功状态、数据和错误信息。</p>
 */
public record BrowserResponse(
    boolean success,             // 操作是否成功
    Object data,                 // 响应数据
    String error,                // 错误信息（如果失败）
    String action                 // 执行的操作类型
) {
    public BrowserResponse(boolean success, Object data, String error, String action) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.action = action;
    }

    public static BrowserResponse success(String action, Object data) {
        return new BrowserResponse(true, data, null, action);
    }

    public static BrowserResponse failure(String action, String error) {
        return new BrowserResponse(false, null, error, action);
    }

    public static BrowserResponse failure(String action, Exception e) {
        return new BrowserResponse(false, null, e.getMessage(), action);
    }
}
