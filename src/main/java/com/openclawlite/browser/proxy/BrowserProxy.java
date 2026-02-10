package com.openclawlite.browser.proxy;

import com.openclawlite.browser.model.BrowserCommand;
import com.openclawlite.browser.model.BrowserResponse;

/**
 * 浏览器代理接口
 *
 * <p>定义浏览器操作的核心接口，所有浏览器实现都应实现此接口。</p>
 *
 * <p>支持的操作类型：</p>
 * <ul>
 *   <li>status - 获取浏览器状态</li>
 *   <li>start - 启动浏览器</li>
 *   <li>stop - 停止浏览器</li>
 *   *   <li>navigate - 导航到 URL</li>
 *   <li>snapshot - 获取页面快照</li>
   *   <li>screenshot - 截图</li>
 *   *   <li>act - 元素交互</li>
 *   *   *   <li>tabs - 标签页管理</li>
 * * </ul>
 */
public interface BrowserProxy {

    /**
     * 获取浏览器状态
     *
     * @param profile 配置文件名称
     * @return 浏览器状态响应
     */
    BrowserResponse status(String profile);

    /**
     * 启动浏览器
     *
     * @param profile 配置文件名称
     * @return 启动结果响应
     */
    BrowserResponse start(String profile);

    /**
     * 停止浏览器
     *
     * @param profile 配置文件名称
     * @return 停止结果响应
     */
    BrowserResponse stop(String profile);

    /**
     * 导航到指定URL
     *
     * @param targetId 目标标签页ID
     * @param url 要导航到的URL
     * @return 导航结果响应
     */
    BrowserResponse navigate(String targetId, String url);

    /**
     * 获取页面快照
     *
     * @param targetId 目标标签页ID
     * @param options 快照选项（AI格式、ARIA格式等）
     * @return 快照结果响应
     */
    BrowserResponse snapshot(String targetId, SnapshotOptions options);

    /**
     * 截图
     *
     * @param targetId 目标标签页ID
     * @param options 截图选项
     * @return 截图结果响应
     */
    BrowserResponse screenshot(String targetId, ScreenshotOptions options);

    /**
     * 元素交互
     *
     * @param targetId 目标标签页ID
     * @param command 浏览器命令
     * @return 交互结果响应
     */
    BrowserResponse act(String targetId, BrowserCommand command);

    /**
     * 获取所有标签页
     *
     * @param profile 配置文件名称
     * @return 标签页列表
     */
    BrowserResponse tabs(String profile);

    /**
     * 关闭标签页
     *
     * @param targetId 要关闭的标签页ID
     * @return 关闭结果响应
     */
    BrowserResponse closeTab(String targetId);

    /**
     * 快照选项
     */
    record SnapshotOptions(
        boolean aiFormat,          // AI友好的文本格式
        boolean ariaFormat,         // ARIA树格式
        boolean includeHtml        // 包含HTML源码
    ) {
        public static SnapshotOptions defaultOptions() {
            return new SnapshotOptions(true, false, false);
        }

        public static SnapshotOptions ariaOptions() {
            return new SnapshotOptions(false, true, false);
        }
    }

    /**
     * 截图选项
     */
    record ScreenshotOptions(
        String format,              // 图片格式：png, jpeg
        boolean fullPage,           // 是否全页截图
        int quality                 // JPEG质量（1-100）
    ) {
        public ScreenshotOptions {
            if (format == null) {
                format = "png";
            }
        }

        public static ScreenshotOptions defaultOptions() {
            return new ScreenshotOptions("png", false, 80);
        }

        public static ScreenshotOptions fullPageOptions() {
            return new ScreenshotOptions("png", true, 80);
        }
    }
}
