package com.openclawlite.api.rest;

import com.openclawlite.browser.model.BrowserCommand;
import com.openclawlite.browser.model.BrowserParams;
import com.openclawlite.browser.model.BrowserResponse;
import com.openclawlite.browser.proxy.BrowserProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 浏览器 REST API 控制器
 *
 * <p>提供浏览器操作的 REST API 端点。</p>
 */
@RestController
@RequestMapping("/api/browser")
@CrossOrigin(origins = "*")
public class BrowserController {

    private static final Logger log = LoggerFactory.getLogger(BrowserController.class);

    private final BrowserProxy browserProxy;

    @Autowired
    public BrowserController(BrowserProxy browserProxy) {
        this.browserProxy = browserProxy;
    }

    /**
     * 获取浏览器状态
     *
     * @param profile 配置文件名
     * @return 浏览器状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam(defaultValue = "default") String profile) {
        log.info("GET /api/browser/status: profile={}", profile);

        BrowserResponse response = browserProxy.status(profile);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 启动浏览器
     *
     * @param profile 配置文件名
     * @return 启动结果
     */
    @PostMapping("/start")
    public ResponseEntity<?> startBrowser(@RequestParam(defaultValue = "default") String profile) {
        log.info("POST /api/browser/start: profile={}", profile);

        BrowserResponse response = browserProxy.start(profile);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 停止浏览器
     *
     * @param profile 配置文件名
     * @return 停止结果
     */
    @PostMapping("/stop")
    public ResponseEntity<?> stopBrowser(@RequestParam(defaultValue = "default") String profile) {
        log.info("POST /api/browser/stop: profile={}", profile);

        BrowserResponse response = browserProxy.stop(profile);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 导航到 URL
     *
     * @param profile 配置文件名
     * @param url 目标 URL
     * @return 导航结果
     */
    @PostMapping("/navigate")
    public ResponseEntity<?> navigate(
            @RequestParam(defaultValue = "default") String profile,
            @RequestParam String url) {
        log.info("POST /api/browser/navigate: profile={}, url={}", profile, url);

        BrowserResponse response = browserProxy.navigate(profile, url);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 获取页面快照
     *
     * @param profile 配置文件名
     * @param includeHtml 是否包含 HTML
     * @return 页面快照
     */
    @GetMapping("/snapshot")
    public ResponseEntity<?> getSnapshot(
            @RequestParam(defaultValue = "default") String profile,
            @RequestParam(defaultValue = "false") boolean includeHtml) {
        log.info("GET /api/browser/snapshot: profile={}, includeHtml={}", profile, includeHtml);

        BrowserProxy.SnapshotOptions options = new BrowserProxy.SnapshotOptions(
            true,   // aiFormat
            false,  // ariaFormat
            includeHtml
        );

        BrowserResponse response = browserProxy.snapshot(profile, options);

        if (response.success()) {
            return ResponseEntity.ok(Map.of("snapshot", response.data()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 截图
     *
     * @param profile 配置文件名
     * @param format 图片格式（png/jpeg）
     * @return 截图结果
     */
    @PostMapping("/screenshot")
    public ResponseEntity<?> takeScreenshot(
            @RequestParam(defaultValue = "default") String profile,
            @RequestParam(defaultValue = "png") String format) {
        log.info("POST /api/browser/screenshot: profile={}, format={}", profile, format);

        BrowserProxy.ScreenshotOptions options = new BrowserProxy.ScreenshotOptions(
            format,
            false,  // fullPage
            80      // quality
        );

        BrowserResponse response = browserProxy.screenshot(profile, options);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 执行元素操作
     *
     * @param profile 配置文件名
     * @param action 操作类型
     * @param selector CSS 选择器
     * @param text 输入文本
     * @param timeout 超时时间（毫秒）
     * @return 操作结果
     */
    @PostMapping("/act")
    public ResponseEntity<?> performAction(
            @RequestParam(defaultValue = "default") String profile,
            @RequestParam String action,
            @RequestParam(required = false) String selector,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "30000") int timeout) {
        log.info("POST /api/browser/act: profile={}, action={}, selector={}", profile, action, selector);

        BrowserParams params = new BrowserParams(
            null,   // url
            text,   // text
            selector,
            null,   // button
            null,   // keys
            null,   // filePath
            null,   // format
            null,   // fullPage
            timeout,
            null    // extra
        );

        BrowserCommand command = new BrowserCommand(action, profile, params);
        BrowserResponse response = browserProxy.act(profile, command);

        if (response.success()) {
            return ResponseEntity.ok(Map.of("result", response.data()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 获取标签页列表
     *
     * @param profile 配置文件名
     * @return 标签页列表
     */
    @GetMapping("/tabs")
    public ResponseEntity<?> getTabs(@RequestParam(defaultValue = "default") String profile) {
        log.info("GET /api/browser/tabs: profile={}", profile);

        BrowserResponse response = browserProxy.tabs(profile);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }

    /**
     * 关闭标签页
     *
     * @param targetId 标签页 ID
     * @return 关闭结果
     */
    @DeleteMapping("/tabs/{targetId}")
    public ResponseEntity<?> closeTab(@PathVariable String targetId) {
        log.info("DELETE /api/browser/tabs/{}", targetId);

        BrowserResponse response = browserProxy.closeTab(targetId);

        if (response.success()) {
            return ResponseEntity.ok(response.data());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", response.error()));
        }
    }
}
