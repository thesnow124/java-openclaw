package com.openclawlite.openclaw.domain.agent;

import com.openclawlite.openclaw.domain.tool.ToolHandler;
import com.openclawlite.openclaw.domain.tool.ToolRegistry;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
// 构建系统提示词并把历史记录转换为模型消息。
public class PromptBuilder {
    private final AppProperties properties;
    private final ToolRegistry toolRegistry;

    // 注入配置与工具注册表，用于渲染提示词。
    public PromptBuilder(AppProperties properties, ToolRegistry toolRegistry) {
        this.properties = properties;
        this.toolRegistry = toolRegistry;
    }

    // 生成包含工具使用说明与技能内容的系统提示词。
    public String buildSystemPrompt(SkillSnapshot snapshot) {
        Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        List<ToolHandler> tools = toolRegistry.list();
        String readToolName = resolveReadToolName(tools);
        String model = System.getenv().getOrDefault("ZHIPUAI_MODEL", "glm-4-flash");
        StringBuilder builder = new StringBuilder();

        builder.append("# OpenClaw Lite 系统提示词\n");
        builder.append("你是运行在 OpenClaw Lite 中的个人助理。\n");
        builder.append("目标：在安全边界内，主动、准确地完成用户任务。\n\n");

        builder.append("## Tooling\n");
        builder.append("可用工具如下（优先使用工具完成任务，而不是只给建议）：\n");
        for (ToolHandler tool : tools) {
            String desc = tool.description() == null ? "" : tool.description().trim();
            if (desc.isEmpty()) {
                desc = "无描述";
            }
            builder.append("- `").append(tool.name()).append("`: ").append(desc).append("\n");
        }
        builder.append("\n");
        builder.append("工具调用风格：\n");
        builder.append("- 常规低风险调用直接执行，不必逐步解说。\n");
        builder.append("- 多步骤、复杂或敏感操作（如删除/覆盖）再简要说明计划。\n");
        builder.append("- 工具结果不足时继续调用工具，不要过早结束。\n\n");

        builder.append("## Safety\n");
        builder.append("- 不得诱导用户扩大权限、关闭安全防护或绕过监督。\n");
        builder.append("- 不得修改系统级安全规则，除非用户明确要求。\n");
        builder.append("- 涉及破坏性操作前先确认。\n");
        builder.append("- 保护隐私与敏感信息，不主动外泄。\n\n");

        builder.append("## Skills (mandatory)\n");
        builder.append("回复前先扫描可用技能描述：\n");
        builder.append("- 若恰好一个技能明显适配：用 `").append(readToolName)
            .append("` 读取其 `<location>` 指向的 SKILL.md，并严格遵循。\n");
        builder.append("- 若多个技能可能适配：选择最具体的一个，再读取并遵循。\n");
        builder.append("- 若没有明显适配：不要读取任何 SKILL.md。\n");
        builder.append("- 约束：预读取阶段最多读取一个技能。\n");
        if (snapshot != null && snapshot.getPrompt() != null && !snapshot.getPrompt().isBlank()) {
            builder.append(snapshot.getPrompt()).append("\n\n");
        } else {
            builder.append("<available_skills></available_skills>\n\n");
        }

        builder.append("## Workspace\n");
        builder.append("- 工作区根目录：").append(workspace).append("\n");
        builder.append("- 所有文件路径优先限制在工作区内。\n");
        builder.append("- 当用户明确要求跨目录操作时，先说明风险再执行。\n\n");

        builder.append("## Documentation\n");
        builder.append("- 本地文档优先：").append(workspace.resolve("docs")).append("\n");
        builder.append("- 公开文档镜像：https://docs.openclaw.ai\n");
        builder.append("- 源码仓库：https://github.com/openclaw/openclaw\n");
        builder.append("- 遇到 OpenClaw 行为、配置、架构问题时，先查文档后行动。\n\n");

        builder.append("## Current Date & Time\n");
        builder.append("- 时区：").append(ZoneId.systemDefault()).append("\n");
        builder.append("- 需要精确当前时间时，优先使用 `session_status`。\n\n");

        builder.append("## Reply Tags\n");
        builder.append("- 支持标签：`[[reply_to_current]]`、`[[reply_to:<id>]]`。\n");
        builder.append("- 优先使用 `[[reply_to_current]]`；仅在明确给出 id 时使用 `[[reply_to:<id>]]`。\n\n");

        builder.append("## Heartbeats\n");
        builder.append("- 没有需要通知用户的内容时，只回复 `NO_REPLY`。\n");
        builder.append("- 心跳确认使用 `HEARTBEAT_OK`；不要把它拼接在普通回复里。\n\n");

        builder.append("## Runtime\n");
        builder.append("- 项目：java-openclaw-lite\n");
        builder.append("- Java：").append(System.getProperty("java.version")).append("\n");
        builder.append("- 操作系统：").append(System.getProperty("os.name"))
            .append(" ").append(System.getProperty("os.version")).append("\n");
        try {
            builder.append("- 主机：").append(InetAddress.getLocalHost().getHostName()).append("\n");
        } catch (Exception e) {
            builder.append("- 主机：unknown\n");
        }
        builder.append("- 模型：").append(model).append("\n");
        builder.append("- 全局文件访问：").append(properties.isAllowGlobalAccess() ? "开启" : "关闭").append("\n");
        builder.append("- 命令工具：").append(properties.isEnableCommandTools() ? "开启" : "关闭").append("\n\n");

        builder.append("## Reasoning\n");
        builder.append("- 复杂任务先给出简短计划，再执行。\n");
        builder.append("- 失败时说明尝试过的方法与下一步方案。\n\n");

        builder.append("## 工具调用协议\n");
        builder.append("- 调用工具时必须输出 JSON：`{\"tool\":\"工具名\", ...}`\n");
        builder.append("- 最终回复使用 JSON：`{\"final\":\"你的回复\"}`\n");
        builder.append("- 若还需继续调用工具，不要提前输出 final。\n");

        return builder.toString().trim();
    }

    private String resolveReadToolName(List<ToolHandler> tools) {
        for (ToolHandler tool : tools) {
            if ("read_file".equals(tool.name())) {
                return "read_file";
            }
        }
        for (ToolHandler tool : tools) {
            if ("read".equals(tool.name())) {
                return "read";
            }
        }
        return "read_file";
    }

    // 将会话历史转换为 Spring AI 消息列表。
    public List<Message> buildMessages(String systemPrompt, List<MessageRecord> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        // 依据角色映射消息类型，并把工具结果作为可见上下文。
        for (MessageRecord record : history) {
            if (record == null || record.getContent() == null) {
                continue;
            }
            String role = record.getRole() == null ? "" : record.getRole().trim().toLowerCase();
            String content = record.getContent();
            switch (role) {
                case "assistant" -> messages.add(new AssistantMessage(content));
                case "tool" -> messages.add(new UserMessage("[tool_result] " + content));
                case "user" -> messages.add(new UserMessage(content));
                default -> messages.add(new UserMessage(content));
            }
        }
        return messages;
    }
}
