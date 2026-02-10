package com.openclawlite.adapter.rest;

import com.openclawlite.openclaw.domain.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 会话控制器 - REST API
 *
 * <p>提供会话管理的 REST 接口，支持：</p>
 * <ul>
 *   <li>查询会话信息</li>
 *   <li>获取渠道会话列表</li>
 *   <li>获取会话消息</li>
 *   <li>删除会话</li>
 *   <li>清除会话消息</li>
 *   <li>获取会话统计信息</li>
 * </ul>
 *
 * <p>API 基础路径: /api/sessions</p>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    /** 会话管理器，用于管理用户会话 */
    private final SessionManager sessionManager;

    /**
     * 构造函数 - 依赖注入
     *
     * @param sessionManager 会话管理器
     */
    public SessionController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        log.info("SessionController 初始化完成");
    }

    /**
     * 获取会话信息
     *
     * <p>根据会话密钥获取会话的详细信息。</p>
     *
     * <p>API 端点: GET /api/sessions/{sessionKey}</p>
     *
     * @param sessionKey 会话密钥
     * @return Mono&lt;Session&gt; 异步返回会话信息
     */
    @GetMapping("/{sessionKey}")
    public Mono<SessionManager.Session> getSession(@PathVariable String sessionKey) {
        log.info("获取会话信息: sessionKey={}", sessionKey);

        return sessionManager.getSession(sessionKey)
            .doOnSuccess(session -> {
                if (session != null) {
                    log.debug("成功获取会话: sessionKey={}, channelId={}",
                        sessionKey, session.getChannelId());
                } else {
                    log.warn("会话不存在: sessionKey={}", sessionKey);
                }
            })
            .doOnError(error -> {
                log.error("获取会话失败: sessionKey={}, error={}",
                    sessionKey, error.getMessage(), error);
            });
    }

    /**
     * 获取渠道会话列表
     *
     * <p>获取指定渠道的所有会话。</p>
     *
     * <p>API 端点: GET /api/sessions/channel/{channelId}</p>
     *
     * @param channelId 渠道ID
     * @return Flux&lt;Session&gt; 异步返回会话列表
     */
    @GetMapping("/channel/{channelId}")
    public Flux<SessionManager.Session> getChannelSessions(@PathVariable String channelId) {
        log.info("获取渠道会话列表: channelId={}", channelId);

        return sessionManager.getSessionsByChannel(channelId)
            .doOnComplete(() -> {
                log.debug("完成获取渠道会话列表: channelId={}", channelId);
            })
            .doOnError(error -> {
                log.error("获取渠道会话列表失败: channelId={}, error={}",
                    channelId, error.getMessage(), error);
            });
    }

    /**
     * 获取会话消息
     *
     * <p>获取指定会话的消息记录，可限制返回数量。</p>
     *
     * <p>API 端点: GET /api/sessions/{sessionKey}/messages?limit=50</p>
     *
     * @param sessionKey 会话密钥
     * @param limit 返回消息数量限制（默认50）
     * @return Flux&lt;SessionMessage&gt; 异步返回消息列表
     */
    @GetMapping("/{sessionKey}/messages")
    public Flux<SessionManager.SessionMessage> getSessionMessages(
            @PathVariable String sessionKey,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("获取会话消息: sessionKey={}, limit={}", sessionKey, limit);

        return sessionManager.getMessages(sessionKey, limit)
            .doOnComplete(() -> {
                log.debug("完成获取会话消息: sessionKey={}, limit={}", sessionKey, limit);
            })
            .doOnError(error -> {
                log.error("获取会话消息失败: sessionKey={}, error={}",
                    sessionKey, error.getMessage(), error);
            });
    }

    /**
     * 删除会话
     *
     * <p>删除指定的会话及其所有消息记录。</p>
     *
     * <p>API 端点: DELETE /api/sessions/{sessionKey}</p>
     *
     * <p>响应示例:</p>
     * <pre>
     * {
     *   "status": "deleted",
     *   "sessionKey": "web-1234567890"
     * }
     * </pre>
     *
     * @param sessionKey 会话密钥
     * @return Mono&lt;Map&lt;String, String&gt;&gt; 异步返回操作状态
     */
    @DeleteMapping("/{sessionKey}")
    public Mono<Map<String, String>> deleteSession(@PathVariable String sessionKey) {
        log.info("删除会话: sessionKey={}", sessionKey);

        return sessionManager.deleteSession(sessionKey)
            .then(Mono.just(Map.of(
                "status", "deleted",
                "sessionKey", sessionKey
            )))
            .doOnSuccess(result -> {
                log.info("成功删除会话: sessionKey={}", sessionKey);
            })
            .doOnError(error -> {
                log.error("删除会话失败: sessionKey={}, error={}",
                    sessionKey, error.getMessage(), error);
            });
    }

    /**
     * 清除会话消息
     *
     * <p>清除指定会话的所有消息，但保留会话本身。</p>
     *
     * <p>API 端点: DELETE /api/sessions/{sessionKey}/messages</p>
     *
     * <p>响应示例:</p>
     * <pre>
     * {
     *   "status": "cleared",
     *   "sessionKey": "web-1234567890"
     * }
     * </pre>
     *
     * @param sessionKey 会话密钥
     * @return Mono&lt;Map&lt;String, String&gt;&gt; 异步返回操作状态
     */
    @DeleteMapping("/{sessionKey}/messages")
    public Mono<Map<String, String>> clearSessionMessages(@PathVariable String sessionKey) {
        log.info("清除会话消息: sessionKey={}", sessionKey);

        // TODO: 实现清除消息功能
        log.warn("清除会话消息功能尚未实现: sessionKey={}", sessionKey);

        return Mono.just(Map.of(
            "status", "cleared",
            "sessionKey", sessionKey
        ));
    }

    /**
     * 获取会话统计信息
     *
     * <p>返回系统的会话统计数据，包括总会话数、总消息数和活跃会话数。</p>
     *
     * <p>API 端点: GET /api/sessions/stats</p>
     *
     * <p>响应示例:</p>
     * <pre>
     * {
     *   "totalSessions": 100,
     *   "totalMessages": 5000,
     *   "activeSessions": 25
     * }
     * </pre>
     *
     * @return Mono&lt;SessionStats&gt; 异步返回统计信息
     */
    @GetMapping("/stats")
    public Mono<SessionStats> getStats() {
        log.debug("获取会话统计信息");

        return sessionManager.getSessionsByChannel("all")  // 获取所有会话
            .count()
            .map(count -> {
                log.debug("会话统计: totalSessions={}", count);
                return new SessionStats(
                    (int) count.longValue(),
                    0,  // TODO: 实现总消息数统计
                    0   // TODO: 实现活跃会话数统计
                );
            })
            .doOnError(error -> {
                log.error("获取会话统计失败: error={}", error.getMessage(), error);
            });
    }

    // ==================== 数据类定义 ====================

    /**
     * 会话统计信息记录
     *
     * @param totalSessions 总会话数
     * @param totalMessages 总消息数
     * @param activeSessions 活跃会话数
     */
    public record SessionStats(
        int totalSessions,
        int totalMessages,
        int activeSessions
    ) {}
}
