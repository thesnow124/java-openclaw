package com.openclawlite.agent.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具执行结果：包含文本、详细数据和错误状态。
 */
public class ToolResult {
    private final String text;
    private final Map<String, Object> details;
    private final boolean isError;

    private ToolResult(String text, Map<String, Object> details, boolean isError) {
        this.text = text;
        this.details = details != null ? details : new HashMap<>();
        this.isError = isError;
    }

    /**
     * 创建成功结果。
     *
     * @param text 结果文本
     * @param details 详细数据（可选）
     * @return 成功的 ToolResult
     */
    public static ToolResult success(String text, Object details) {
        Map<String, Object> detailsMap = new HashMap<>();
        if (details != null) {
            detailsMap.put("result", details);
        }
        return new ToolResult(text, detailsMap, false);
    }

    /**
     * 创建成功结果（仅文本）。
     *
     * @param text 结果文本
     * @return 成功的 ToolResult
     */
    public static ToolResult success(String text) {
        return success(text, null);
    }

    /**
     * 创建错误结果。
     *
     * @param errorMessage 错误消息
     * @return 错误的 ToolResult
     */
    public static ToolResult error(String errorMessage) {
        return new ToolResult(errorMessage, null, true);
    }

    /**
     * 创建带详细错误的结果。
     *
     * @param errorMessage 错误消息
     * @param errorDetails 错误详情
     * @return 错误的 ToolResult
     */
    public static ToolResult error(String errorMessage, Map<String, Object> errorDetails) {
        return new ToolResult(errorMessage, errorDetails, true);
    }

    /**
     * 创建带自定义详细数据的结果。
     *
     * @param text 结果文本
     * @param details 详细数据
     * @return ToolResult
     */
    public static ToolResult of(String text, Map<String, Object> details) {
        return new ToolResult(text, details, false);
    }

    /**
     * 获取结果文本。
     */
    public String getText() {
        return text;
    }

    /**
     * 获取详细数据。
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * 是否为错误结果。
     */
    public boolean isError() {
        return isError;
    }

    /**
     * 是否为成功结果。
     */
    public boolean isSuccess() {
        return !isError;
    }

    @Override
    public String toString() {
        if (isError) {
            return "[ERROR] " + text;
        }
        return text;
    }
}
