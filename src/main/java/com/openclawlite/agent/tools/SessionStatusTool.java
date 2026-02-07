package com.openclawlite.agent.tools;

import com.openclawlite.agent.SessionState;
import com.openclawlite.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话状态工具：获取当前会话的详细状态信息。
 */
@Component
public class SessionStatusTool implements ToolHandler {

    @Override
    public String name() {
        return "session_status";
    }

    @Override
    public String description() {
        return "获取当前会话的详细状态信息，包括模型使用情况、上下文大小、工具调用统计等。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "session_status"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of()
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
            SessionState session = context.getSession();
            if (session == null) {
                return ToolResult.error("当前没有活跃会话。");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("## 当前会话状态\n\n");

            // 会话基本信息
            sb.append("**会话 ID**: ").append(session.getSessionId()).append("\n");
            sb.append("**创建时间**: ").append(formatTimestamp(session.getCreatedAt())).append("\n");
            sb.append("**消息数量**: ").append(session.getMessages() != null ? session.getMessages().size() : 0).append("\n");
            sb.append("**模型**: ").append(session.getModel() != null ? session.getModel() : "未指定").append("\n");

            // 上下文信息
            sb.append("\n### 上下文信息\n");
            sb.append("**当前大小**: ").append(session.getMessages() != null ? session.getMessages().size() : 0).append(" 条消息\n");

            // 技能信息
            if (session.getSkillSnapshot() != null) {
                sb.append("**已加载技能**: ").append(session.getSkillSnapshot().getSkillRefs().size()).append(" 个\n");
            }

            // 工具调用统计
            sb.append("\n### 工具调用统计\n");
            sb.append("**总调用次数**: ").append(session.getTotalToolCalls()).append("\n");
            sb.append("**成功次数**: ").append(session.getSuccessfulToolCalls()).append("\n");
            sb.append("**失败次数**: ").append(session.getFailedToolCalls()).append("\n");

            // Token 使用统计
            sb.append("\n### Token 使用\n");
            sb.append("**输入 Token**: ").append(session.getTotalInputTokens()).append("\n");
            sb.append("**输出 Token**: ").append(session.getTotalOutputTokens()).append("\n");
            sb.append("**总计**: ").append(session.getTotalInputTokens() + session.getTotalOutputTokens()).append("\n");

            // 成本估算
            sb.append("\n### 成本估算\n");
            double estimatedCost = estimateCost(session.getTotalInputTokens(), session.getTotalOutputTokens());
            sb.append("**预估成本**: $").append(String.format("%.6f", estimatedCost)).append("\n");

            Map<String, Object> details = new HashMap<>();
            details.put("sessionId", session.getSessionId());
            details.put("messageCount", session.getMessages() != null ? session.getMessages().size() : 0);
            details.put("totalToolCalls", session.getTotalToolCalls());
            details.put("totalTokens", session.getTotalInputTokens() + session.getTotalOutputTokens());
            details.put("estimatedCost", estimatedCost);

            return ToolResult.success(sb.toString(), details);

        } catch (Exception e) {
            return ToolResult.error("获取会话状态失败：" + e.getMessage());
        }
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

    /**
     * 估算成本（简化版）。
     * 假设输入 $0.002/1K tokens，输出 $0.01/1K tokens
     */
    private double estimateCost(long inputTokens, long outputTokens) {
        return (inputTokens * 0.002 / 1000.0) + (outputTokens * 0.01 / 1000.0);
    }
}
