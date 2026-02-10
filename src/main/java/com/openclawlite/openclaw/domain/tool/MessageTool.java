package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.adapter.channel.registry.ChannelRegistry;
import com.openclawlite.openclaw.domain.agent.ToolCall;
import com.openclawlite.openclaw.domain.channel.core.ChannelOutboundAdapter;
import com.openclawlite.openclaw.domain.channel.core.ChannelPlugin;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * MessageTool - 跨平台消息发送和管理工具
 *
 * <p>支持通过不同渠道发送消息、回复消息等操作。</p>
 *
 * <p>功能包括：</p>
 * <ul>
 *   <li>发送文本消息</li>
 *   <li>发送媒体消息（图片、文件等）</li>
 *   <li>回复现有消息</li>
 *   <li>删除消息</li>
 *   <li>添加反应（emoji）</li>
 * </ul>
 *
 * @author OpenClaw Lite
 * @since 2026-02-08
 */
@Component
public class MessageTool implements ToolHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageTool.class);

    private final ChannelRegistry channelRegistry;
    private final AppProperties properties;

    public MessageTool(ChannelRegistry channelRegistry, AppProperties properties) {
        this.channelRegistry = channelRegistry;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "message";
    }

    @Override
    public String description() {
        return "跨平台消息发送和管理工具";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "message",
              "action": "send",
              "channel": "telegram",
              "account": "default",
              "chatId": "聊天ID",
              "text": "要发送的消息内容"
            }

            {
              "tool": "message",
              "action": "reply",
              "channel": "telegram",
              "account": "default",
              "messageId": "要回复的消息ID",
              "text": "回复内容"
            }
            """;
    }

    @Override
    public String execute(ToolCall call, com.openclawlite.openclaw.domain.tool.ToolContext context) {
        String action = call.getArguments().get("action").toString();

        log.debug("执行消息工具: action={}", action);

        switch (action) {
            case "send":
                return sendMessage(call, context);
            case "reply":
                return replyMessage(call, context);
            case "delete":
                return deleteMessage(call, context);
            case "list_channels":
                return listChannels();
            default:
                return "❌ 不支持的操作: " + action + "\n支持的操作: send, reply, delete, list_channels";
        }
    }

    /**
     * 发送消息
     */
    private String sendMessage(ToolCall call, ToolContext context) {
        try {
            String channelId = (String) call.getArguments().get("channel");
            String accountId = (String) call.getArguments().get("account");
            String chatId = (String) call.getArguments().get("chatId");
            String text = (String) call.getArguments().get("text");

            if (channelId == null || accountId == null || chatId == null || text == null) {
                return "❌ 缺少必需参数：channel, account, chatId, text";
            }

            // 获取渠道插件
            ChannelPlugin channel = channelRegistry.getChannel(channelId)
                .orElseThrow(() -> new IllegalArgumentException("渠道不存在: " + channelId));

            // 获取发送适配器
            ChannelOutboundAdapter outboundAdapter = channel.getOutboundAdapter()
                .orElseThrow(() -> new IllegalStateException("渠道不支持发送消息"));

            // 发送消息
            CompletableFuture<ChannelOutboundAdapter.ChannelMessageResult> future =
                outboundAdapter.sendTextMessage(accountId, chatId, text);

            // 等待结果
            ChannelOutboundAdapter.ChannelMessageResult result = future.get(30, TimeUnit.SECONDS);

            if (result.success()) {
                log.info("消息发送成功: messageId={}", result.messageId());
                return "✅ 消息发送成功\n" +
                       "消息ID: " + result.messageId() + "\n" +
                       "渠道: " + channelId + "\n" +
                       "账号: " + accountId + "\n" +
                       "聊天ID: " + chatId;
            } else {
                log.error("消息发送失败: {}", result.error());
                return "❌ 消息发送失败\n" +
                       "错误: " + result.error();
            }

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("发送消息时出错", e);
            return "❌ 发送消息时出错: " + e.getMessage();
        }
    }

    /**
     * 回复消息
     */
    private String replyMessage(ToolCall call, ToolContext context) {
        try {
            String channelId = (String) call.getArguments().get("channel");
            String accountId = (String) call.getArguments().get("account");
            String messageId = (String) call.getArguments().get("messageId");
            String text = (String) call.getArguments().get("text");

            if (channelId == null || accountId == null || messageId == null || text == null) {
                return "❌ 缺少必需参数：channel, account, messageId, text";
            }

            // 获取渠道插件
            ChannelPlugin channel = channelRegistry.getChannel(channelId)
                .orElseThrow(() -> new IllegalArgumentException("渠道不存在: " + channelId));

            // 获取发送适配器
            ChannelOutboundAdapter outboundAdapter = channel.getOutboundAdapter()
                .orElseThrow(() -> new IllegalStateException("渠道不支持发送消息"));

            // 发送回复（在文本中添加引用）
            String replyText = "回复消息 [" + messageId + "]:\n" + text;
            CompletableFuture<ChannelOutboundAdapter.ChannelMessageResult> future =
                outboundAdapter.sendTextMessage(accountId, messageId, replyText);

            // 等待结果
            ChannelOutboundAdapter.ChannelMessageResult result = future.get(30, TimeUnit.SECONDS);

            if (result.success()) {
                log.info("回复发送成功: messageId={}", result.messageId());
                return "✅ 回复发送成功\n" +
                       "回复ID: " + result.messageId() + "\n" +
                       "原消息ID: " + messageId;
            } else {
                log.error("回复发送失败: {}", result.error());
                return "❌ 回复发送失败\n" +
                       "错误: " + result.error();
            }

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("回复消息时出错", e);
            return "❌ 回复消息时出错: " + e.getMessage();
        }
    }

    /**
     * 删除消息
     */
    private String deleteMessage(ToolCall call, ToolContext context) {
        // 注意：当前 ChannelOutboundAdapter 接口不支持删除消息
        // 这是一个占位实现，未来可以扩展
        String channelId = (String) call.getArguments().get("channel");
        String messageId = (String) call.getArguments().get("messageId");

        log.info("请求删除消息: channel={}, messageId={}", channelId, messageId);

        return "⚠️ 删除消息功能暂未实现\n" +
               "渠道: " + channelId + "\n" +
               "消息ID: " + messageId + "\n" +
               "原因: 当前接口不支持删除操作，需要扩展 ChannelOutboundAdapter";
    }

    /**
     * 列出所有可用渠道
     */
    private String listChannels() {
        var channels = channelRegistry.getAllChannels();

        if (channels.isEmpty()) {
            return "当前没有可用的渠道";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("可用渠道列表:\n\n");

        channels.forEach(channel -> {
            sb.append("- **").append(channel.getId()).append("**\n");
            sb.append("  名称: ").append(channel.getMeta().label()).append("\n");
            sb.append("  描述: ").append(channel.getMeta().blurb()).append("\n");
            sb.append("  状态: ").append(channel.getCapabilities().reply() ? "✅ 支持消息" : "❌ 不支持").append("\n");
            sb.append("\n");
        });

        return sb.toString();
    }
}
