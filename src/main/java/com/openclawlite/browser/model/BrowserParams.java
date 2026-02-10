package com.openclawlite.browser.model;

import java.util.Map;

/**
 * 浏览器操作参数
 *
 * <p>包含浏览器操作所需的各种参数。</p>
 */
public record BrowserParams(
    String url,                  // 页面URL
    String text,                 // 要输入的文本
    String selector,             // CSS选择器或XPath
    String button,               // 要点击的按钮
    String keys,                 // 要按的键
    String filePath,             // 文件路径（上传用）
    String format,               // 截图格式（png/jpeg）
    Boolean fullPage,            // 是否全页截图
    Integer timeout,             // 超时时间（毫秒）
    Map<String, Object> extra   // 额外参数
) {
    public BrowserParams {
        if (fullPage == null) {
            fullPage = false;
        }
        if (timeout == null) {
            timeout = 30000;
        }
        if (extra == null) {
            extra = Map.of();
        }
    }

    public BrowserParams() {
        this(null, null, null, null, null, null, null, false, 30000, Map.of());
    }

    // 便捷构建方法
    public static BrowserParams forUrl(String url) {
        return new BrowserParams(url, null, null, null, null, null, null, false, 30000, Map.of());
    }

    public static BrowserParams forClick(String selector) {
        return new BrowserParams(null, null, selector, null, null, null, null, false, 30000, Map.of());
    }

    public static BrowserParams forType(String selector, String text) {
        return new BrowserParams(null, text, selector, null, null, null, null, false, 30000, Map.of());
    }
}
