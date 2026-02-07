package com.openclawlite.agent.tools;

import com.openclawlite.agent.SessionState;
import com.openclawlite.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话历史工具：获取指定会话的消息历史。
 */
@Component
public class SessionsHistoryTool implements ToolHandler {

    @Override
    public String name() {
        return "sessions_history";
    }

    @Override
    public String description() {
        return "获取指定会话的消息历史记录，包括用户消息和助手回复。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "sessions_history",
              "sessionId": "current",
              "limit": 20
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sessionId", Map.of(
            "type", "string",
            "description", "会话 ID（默认为 current）",
            "default", "current"
        ));
        properties.put("limit", Map.of(
            "type", "integer",
            "description", "返回的最大消息数",
            "default", 20,
            "minimum", 1,
            "maximum", 100
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of("sessionId")
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
            int limit = parseInt(call.getArguments().get("limit"), 20);

            // 获取会话历史
            SessionState session = context.getSession();
            if (session == null) {
                return ToolResult.error("会话不存在：" + sessionId);
            }

            var messages = session.getMessages();
            if (messages == null || messages.isEmpty()) {
                return ToolResult.success("会话 " + sessionId + " 暂无消息记录。");
            }

            int count = Math.min(limit, messages.size());
            int start = Math.max(0, messages.size() - count);

            StringBuilder sb = new StringBuilder();
            sb.append("会话 ").append(sessionId).append(" 的最近 ").append(count).append(" 条消息：\n\n");

            for (int i = start; i < messages.size(); i++) {
                var msg = messages.get(i);
                String role = msg.getRole();
                String content = msg.getContent();

                sb.append("**").append(role.equals("user") ? "用户" : "助手").append("**: ");
                sb.append(content).append("\n\n");
            }

            return ToolResult.success(sb.toString());

        } catch (Exception e) {
            return ToolResult.error("获取会话历史失败：" + e.getMessage());
        }
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
