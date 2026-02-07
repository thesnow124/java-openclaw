package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话列表工具：列出所有活跃的会话。
 */
@Component
public class SessionsListTool implements ToolHandler {

    @Override
    public String name() {
        return "sessions_list";
    }

    @Override
    public String description() {
        return "列出所有活跃的会话，包括会话 ID、创建时间、最后活动时间等信息。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "sessions_list",
              "limit": 10
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("limit", Map.of(
            "type", "integer",
            "description", "返回的最大会话数量",
            "default", 10,
            "minimum", 1,
            "maximum", 100
        ));
        properties.put("kind", Map.of(
            "type", "string",
            "description", "过滤会话类型（可选）",
            "enum", List.of("console", "telegram", "discord", "slack")
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
            int limit = parseInt(call.getArguments().get("limit"), 10);
            String kind = (String) call.getArguments().get("kind");

            // 获取会话列表（简化实现）
            List<Map<String, Object>> sessions = listSessions(limit, kind);

            if (sessions.isEmpty()) {
                return ToolResult.success("当前没有活跃的会话。");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("找到 ").append(sessions.size()).append(" 个活跃会话：\n\n");

            for (Map<String, Object> session : sessions) {
                String id = (String) session.get("id");
                String type = (String) session.get("type");
                long createdAt = ((Number) session.getOrDefault("createdAt", 0)).longValue();
                long lastActive = ((Number) session.getOrDefault("lastActive", 0)).longValue();

                sb.append("**会话 ID**: ").append(id).append("\n");
                sb.append("- 类型: ").append(type).append("\n");
                sb.append("- 创建时间: ").append(formatTimestamp(createdAt)).append("\n");
                sb.append("- 最后活动: ").append(formatTimestamp(lastActive)).append("\n");
                sb.append("\n");
            }

            return ToolResult.success(sb.toString(), Map.of("sessions", sessions));

        } catch (Exception e) {
            return ToolResult.error("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 列出会话（简化实现）。
     * TODO: 从 SessionStore 获取实际的会话列表
     */
    private List<Map<String, Object>> listSessions(int limit, String kind) {
        List<Map<String, Object>> sessions = new ArrayList<>();

        // 简化实现：返回当前会话
        Map<String, Object> currentSession = new HashMap<>();
        currentSession.put("id", "current");
        currentSession.put("type", kind != null ? kind : "console");
        currentSession.put("createdAt", System.currentTimeMillis() - 3600000L); // 1小时前
        currentSession.put("lastActive", System.currentTimeMillis());
        sessions.add(currentSession);

        return sessions;
    }

    /**
     * 格式化时间戳。
     */
    private String formatTimestamp(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " 天前";
        } else if (hours > 0) {
            return hours + " 小时前";
        } else if (minutes > 0) {
            return minutes + " 分钟前";
        } else {
            return "刚刚";
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
