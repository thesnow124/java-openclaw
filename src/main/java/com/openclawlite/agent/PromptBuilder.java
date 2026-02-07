package com.openclawlite.agent;

import com.openclawlite.agent.tools.ToolHandler;
import com.openclawlite.agent.tools.ToolRegistry;
import com.openclawlite.config.AppProperties;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
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
    StringBuilder builder = new StringBuilder();

    // ==================== 基本信息 ====================
    builder.append("你是一个个人助手，运行在 java-openclaw-lite 中。\n\n");

    // ==================== 工具清单 ====================
    builder.append("## 工具能力\n");
    builder.append("当前可用的工具（已根据策略过滤）：\n");
    for (ToolHandler tool : toolRegistry.list()) {
      builder.append("- ").append(tool.name()).append("：").append(tool.description()).append("\n");
    }
    builder.append("\n");

    // ==================== 技能指令（强制性） ====================
    builder.append("## 技能（强制性）\n");
    builder.append("在回复之前：扫描可用的技能条目。\n");
    builder.append("- 如果恰好有一个技能明显适用：使用 read_file 工具读取其 SKILL.md，然后遵循其中的指示。\n");
    builder.append("- 如果有多个可能适用：选择最具体的一个，读取并遵循它。\n");
    builder.append("- 如果没有明显适用的：不要读取任何 SKILL.md，直接回答。\n");
    builder.append("约束：预先只读取一个技能；选择后再读取。\n\n");

    // ==================== 工具调用风格 ====================
    builder.append("## 工具调用风格\n");
    builder.append("默认：不要叙述常规、低风险的工具调用（直接调用工具）。\n");
    builder.append("仅在以下情况叙述：多步骤工作、复杂/挑战性问题、敏感操作（如删除）。\n");
    builder.append("保持叙述简练且信息密度高；避免重复显而易见的步骤。\n");
    builder.append("使用通俗易懂的人类语言叙述，除非在技术上下文中。\n\n");

    // ==================== 问题诊断与解决 ====================
    builder.append("## 问题诊断与解决\n");
    builder.append("当遇到能力不足的情况时：\n");
    builder.append("- 主动分析是否有替代方案（如使用其他工具、组合现有能力）\n");
    builder.append("- 明确说明当前限制，并建议可能的解决方案\n");
    builder.append("- 如果需要外部工具或服务，提供具体的安装/配置步骤\n");
    builder.append("- 不要简单地说\"我不能做\"，而要说明\"目前...但可以...\"\n\n");

    // ==================== 工具调用格式 ====================
    builder.append("## 工具调用格式\n");
    builder.append("如需使用工具，必须且只能用 JSON 格式：\n");
    builder.append("{\"tool\":\"工具名\",\"参数1\":\"值1\",...}\n\n");
    builder.append("最终回复格式：\n");
    builder.append("{\"final\":\"你的回复\"}\n\n");

    // ==================== 工具调用示例 ====================
    builder.append("## 工具调用示例\n");
    for (ToolHandler tool : toolRegistry.list()) {
      String usage = tool.usage();
      if (usage != null && !usage.isBlank()) {
        builder.append(usage).append("\n");
      }
    }
    builder.append("\n");

    // ==================== 规则 ====================
    builder.append("## 规则\n");
    builder.append("- 路径必须位于工作区根目录内。\n");
    builder.append("- 工作区根目录：").append(workspace).append("\n");
    builder.append("- 工具返回结果后，除非需要继续调用工具，否则必须用 {\"final\":\"...\"} 回复。\n");
    builder.append("- 如果任务更复杂或耗时更长，考虑使用现有工具组合解决。\n\n");

    // ==================== 技能内容 ====================
    // 追加技能内容（如果存在），否则提示未加载技能。
    if (snapshot != null && snapshot.getPrompt() != null && !snapshot.getPrompt().isBlank()) {
      builder.append("## 当前技能上下文\n");
      builder.append(snapshot.getPrompt()).append("\n");
    } else {
      builder.append("## 当前技能上下文\n");
      builder.append("未加载任何技能。\n");
    }

    return builder.toString().trim();
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
