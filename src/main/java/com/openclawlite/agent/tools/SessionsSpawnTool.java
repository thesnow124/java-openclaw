package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话创建工具：创建一个新的会话。
 */
@Component
public class SessionsSpawnTool implements ToolHandler {

    @Override
    public String name() {
        return "sessions_spawn";
    }

    @Override
    public String description() {
        return "创建一个新的会话并返回会话 ID。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "sessions_spawn",
              "kind": "console"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("kind", Map.of(
            "type", "string",
            "description", "会话类型",
            "enum", java.util.List.of("console", "telegram", "discord", "slack"),
            "default", "console"
        ));
        properties.put("message", Map.of(
            "type", "string",
            "description", "可选的初始消息"
        ));

        return Map.of(
            "type", "object",
            "properties", properties
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
            String kind = String.valueOf(call.getArguments().getOrDefault("kind", "console"));
            String message = (String) call.getArguments().get("message");

            // 生成新的会话 ID
            String sessionId = UUID.randomUUID().toString().substring(0, 8);

            // TODO: 实际创建会话并保存到 SessionStore
            // SessionState newSession = new SessionState(sessionId);

            StringBuilder sb = new StringBuilder();
            sb.append("已创建新会话：\n\n");
            sb.append("**会话 ID**: ").append(sessionId).append("\n");
            sb.append("**类型**: ").append(kind).append("\n");
            sb.append("**创建时间**: ").append(System.currentTimeMillis()).append("\n");

            if (message != null && !message.trim().isEmpty()) {
                sb.append("**初始消息**: ").append(message).append("\n");
            }

            Map<String, Object> details = new HashMap<>();
            details.put("sessionId", sessionId);
            details.put("kind", kind);
            details.put("createdAt", System.currentTimeMillis());

            return ToolResult.success(sb.toString(), details);

        } catch (Exception e) {
            return ToolResult.error("创建会话失败：" + e.getMessage());
        }
    }
}
