package com.openclawlite.adapter.rest;

import com.openclawlite.openclaw.domain.agent.AgentService;
import com.openclawlite.openclaw.domain.channel.core.ChannelMessage;
import com.openclawlite.openclaw.domain.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 聊天控制器 - REST API
 *
 * <p>提供与 AI 智能体对话的 REST 接口，支持：</p>
 * <ul>
 *   <li>发送消息并获取响应</li>
 *   <li>查看聊天历史记录</li>
 *   <li>清除聊天历史</li>
 *   <li>获取可用智能体信息</li>
 * </ul>
 *
 * <p>API 基础路径: /api/chat</p>
 *
 * <p>请求示例：</p>
 * <pre>
 * POST /api/chat
 * {
 *   "message": "你好",
 *   "sessionKey": "web-1234567890"
 * }
 * </pre>
 *
 * <p>响应示例：</p>
 * <pre>
 * {
 *   "response": "你好！有什么可以帮助你的吗？",
 *   "sessionKey": "web-1234567890",
 *   "status": "success"
 * }
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /** 智能体服务，用于处理消息和生成响应 */
    private final AgentService agentService;

    /** 会话管理器，用于管理用户会话和历史记录 */
    private final SessionManager sessionManager;

    /**
     * 构造函数 - 依赖注入
     *
     * @param agentService 智能体服务
     * @param sessionManager 会话管理器
     */
    public ChatController(AgentService agentService, SessionManager sessionManager) {
        this.agentService = agentService;
        this.sessionManager = sessionManager;
        log.info("ChatController 初始化完成");
    }

    /**
     * 发送消息并获取智能体响应
     *
     * <p>处理用户发送的消息，通过智能体服务处理后返回响应。</p>
     * <p>如果未提供 sessionKey，系统会自动生成一个新会话。</p>
     *
     * <p>API 端点: POST /api/chat</p>
     *
     * <p>请求示例:</p>
     * <pre>
     * {
     *   "message": "请帮我写一个Java方法",
     *   "sessionKey": "web-1234567890"  // 可选，不提供则创建新会话
     * }
     * </pre>
     *
     * <p>响应示例:</p>
     * <pre>
     * {
     *   "response": "我可以帮你写一个Java方法...",
     *   "sessionKey": "web-1234567890",
     *   "status": "success"
     * }
     * </pre>
     *
     * @param request 聊天请求，包含消息内容和可选的会话密钥
     * @return Mono&lt;ChatResponse&gt; 异步返回智能体响应
     */
    @PostMapping
    public Mono<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("收到聊天消息: message='{}', sessionKey='{}'",
            request.message(), request.sessionKey());

        // 如果未提供 sessionKey，则生成新的会话密钥
        String sessionKey = request.sessionKey() != null
            ? request.sessionKey()
            : generateSessionKey();

        log.debug("使用会话密钥: {}", sessionKey);

        // 创建渠道消息对象
        ChannelMessage channelMessage = new ChannelMessage();
        channelMessage.setChannelId("web");  // 设置为 Web 渠道
        channelMessage.setText(request.message());  // 设置消息文本
        channelMessage.setSenderId("web-user");  // 设置发送者ID
        channelMessage.setChatId("web-chat");  // 设置聊天ID

        log.debug("构建渠道消息: channelId={}, senderId={}, chatId={}",
            channelMessage.getChannelId(),
            channelMessage.getSenderId(),
            channelMessage.getChatId());

        // 通过智能体服务处理消息
        return agentService.processMessage(sessionKey, channelMessage)
            .map(response -> {
                log.info("智能体响应成功: sessionKey={}, responseLength={}",
                    sessionKey, response.length());
                log.debug("响应内容: {}", response);
                return new ChatResponse(
                    response,
                    sessionKey,
                    "success"
                );
            })
            .onErrorResume(error -> {
                // 错误处理：记录错误并返回友好的错误消息
                log.error("处理消息时发生错误: sessionKey={}, error={}",
                    sessionKey, error.getMessage(), error);
                return Mono.just(new ChatResponse(
                    "抱歉，我遇到了一个错误: " + error.getMessage(),
                    sessionKey,
                    "error"
                ));
            });
    }

    /**
     * 获取会话历史记录
     *
     * <p>获取指定会话的历史消息记录，最多返回最近 50 条消息。</p>
     *
     * <p>API 端点: GET /api/chat/history/{sessionKey}</p>
     *
     * <p>响应示例:</p>
     * <pre>
     * {
     *   "sessionKey": "web-1234567890",
     *   "messages": [
     *     {
     *       "role": "user",
     *       "content": "你好",
     *       "timestamp": "2024-01-01T12:00:00"
     *     },
     *     {
     *       "role": "assistant",
     *       "content": "你好！有什么可以帮助你的吗？",
     *       "timestamp": "2024-01-01T12:00:01"
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param sessionKey 会话密钥
     * @return Mono&lt;ChatHistory&gt; 异步返回聊天历史记录
     */
    @GetMapping("/history/{sessionKey}")
    public Mono<ChatHistory> getHistory(@PathVariable String sessionKey) {
        log.info("获取会话历史: sessionKey={}", sessionKey);

        return sessionManager.getMessages(sessionKey, 50)
            .map(msg -> {
                // 将会话消息转换为消息条目
                log.debug("处理历史消息: role={}, contentLength={}",
                    msg.getRole(), msg.getContent().length());
                return new MessageEntry(
                    msg.getRole(),
                    msg.getContent(),
                    msg.getTimestamp().toString()
                );
            })
            .collectList()
            .map(messages -> {
                log.info("成功获取会话历史: sessionKey={}, messageCount={}",
                    sessionKey, messages.size());
                return new ChatHistory(
                    sessionKey,
                    messages
                );
            })
            .doOnError(error -> {
                log.error("获取会话历史失败: sessionKey={}, error={}",
                    sessionKey, error.getMessage(), error);
            });
    }

    /**
     * 清除会话历史记录
     *
     * <p>删除指定会话的所有消息记录，释放会话资源。</p>
     *
     * <p>API 端点: DELETE /api/chat/history/{sessionKey}</p>
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
    @DeleteMapping("/history/{sessionKey}")
    public Mono<Map<String, String>> clearHistory(@PathVariable String sessionKey) {
        log.info("清除会话历史: sessionKey={}", sessionKey);

        return sessionManager.deleteSession(sessionKey)
            .then(Mono.just(Map.of(
                "status", "cleared",
                "sessionKey", sessionKey
            )))
            .doOnSuccess(result -> {
                log.info("成功清除会话历史: sessionKey={}", sessionKey);
            })
            .doOnError(error -> {
                log.error("清除会话历史失败: sessionKey={}, error={}",
                    sessionKey, error.getMessage(), error);
            });
    }

    /**
     * 获取可用智能体信息
     *
     * <p>返回当前可用的智能体列表及其基本信息。</p>
     *
     * <p>API 端点: GET /api/chat/agents</p>
     *
     * <p>响应示例:</p>
     * <pre>
     * {
     *   "agentId": "default",
     *   "name": "OpenClaw AI Agent",
     *   "description": "Available for chat"
     * }
     * </pre>
     *
     * @return Mono&lt;AgentsInfo&gt; 异步返回智能体信息
     */
    @GetMapping("/agents")
    public Mono<AgentsInfo> getAgents() {
        log.debug("获取可用智能体信息");

        return Mono.just(new AgentsInfo(
            "default",
            "OpenClaw AI Agent",
            "Available for chat"
        ))
        .doOnSuccess(agents -> {
            log.debug("返回智能体信息: agentId={}, name={}",
                agents.agentId(), agents.name());
        });
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成新的会话密钥
     *
     * <p>使用时间戳生成唯一的会话标识符，格式为: web-{timestamp}</p>
     *
     * @return 会话密钥字符串
     */
    private String generateSessionKey() {
        String sessionKey = "web-" + System.currentTimeMillis();
        log.debug("生成新会话密钥: {}", sessionKey);
        return sessionKey;
    }

    // ==================== 数据类定义 ====================

    /**
     * 聊天请求记录
     *
     * @param message 用户消息内容
     * @param sessionKey 会话密钥（可选）
     */
    public record ChatRequest(
        String message,
        String sessionKey
    ) {}

    /**
     * 聊天响应记录
     *
     * @param response 智能体响应内容
     * @param sessionKey 会话密钥
     * @param status 响应状态（success/error）
     */
    public record ChatResponse(
        String response,
        String sessionKey,
        String status
    ) {}

    /**
     * 消息条目记录
     *
     * @param role 消息角色（user/assistant/system）
     * @param content 消息内容
     * @param timestamp 时间戳
     */
    public record MessageEntry(
        String role,
        String content,
        String timestamp
    ) {}

    /**
     * 聊天历史记录
     *
     * @param sessionKey 会话密钥
     * @param messages 消息列表
     */
    public record ChatHistory(
        String sessionKey,
        java.util.List<MessageEntry> messages
    ) {}

    /**
     * 智能体信息记录
     *
     * @param agentId 智能体ID
     * @param name 智能体名称
     * @param description 智能体描述
     */
    public record AgentsInfo(
        String agentId,
        String name,
        String description
    ) {}
}
