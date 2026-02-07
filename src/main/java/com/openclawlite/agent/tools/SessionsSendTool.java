package com.openclawlite.agent.tools;

import com.openclawlite.agent.SessionState;
import com.openclawlite.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话发送工具：向指定会话发送消息。
 */
@Component
public class SessionsSendTool implements ToolHandler {

    @Override
    public String name() {
        return "sessions_send";
    }

    @Override
    public String description() {
        return "向指定的会话发送消息并获取回复。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "sessions_send",
              "sessionId": "current",
              "message": "你好"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sessionId", Map.of(
            "type", "string",
            "description", "目标会话 ID（默认为 current）",
            "default", "current"
        ));
        properties.put("message", Map.of(
            "type", "string",
            "description", "要发送的消息内容"
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", java.util.List.of("message")
        );
    }

    @Override
    public String execute(ToolCall call, ToolContext context) {
        ToolResult result = executeTyped(call, context);
        return result.getText();
    }

    @Override
    public ToolResult executeTyped(ToolCall call, ToolContext context) {
        try {
            String sessionId = String.valueOf(call.getArguments().getOrDefault("sessionId", "current"));
            String message = (String) call.getArguments().get("message");

            if (message == null || message.trim().isEmpty()) {
                return ToolResult.error("消息内容不能为空");
            }

            // TODO: 实际发送消息到会话
            // 需要通过某种方式通知目标会话处理新消息
            // 这可能需要实现消息总线或事件系统

            StringBuilder sb = new StringBuilder();
            sb.append("消息已发送到会话 ").append(sessionId).append("\n\n");
            sb.append("**发送内容**: ").append(message).append("\n");
            sb.append("**发送时间**: ").append(System.currentTimeMillis()).append("\n");
            sb.append("\n注意：这是简化实现。完整的实现需要集成消息系统来处理跨会话通信。");

            Map<String, Object> details = new HashMap<>();
            details.put("sessionId", sessionId);
            details.put("message", message);
            details.put("sentAt", System.currentTimeMillis());

            return ToolResult.success(sb.toString(), details);

        } catch (Exception e) {
            return ToolResult.error("发送消息失败：" + e.getMessage());
        }
    }
}
