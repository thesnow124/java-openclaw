package com.openclawlite.openclaw.domain.agent;

import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
// 负责在对话过长时对历史内容进行压缩。
public class CompactionService {
  private final AiClient aiClient;
  private final AppProperties properties;

  // 注入模型客户端与配置。
  public CompactionService(AiClient aiClient, AppProperties properties) {
    this.aiClient = aiClient;
    this.properties = properties;
  }

  // 尝试压缩会话历史，成功时更新 session 内的消息列表。
  public CompactionResult compact(SessionState session) {
    if (session == null || session.getMessages() == null) {
      return CompactionResult.fail("会话为空，无法压缩。");
    }
    List<MessageRecord> messages = session.getMessages();
    int keep = Math.max(1, properties.getCompactionKeepMessages());
    if (messages.size() <= keep) {
      return CompactionResult.noop();
    }
    int splitIndex = Math.max(0, messages.size() - keep);
    List<MessageRecord> head = new ArrayList<>(messages.subList(0, splitIndex));
    List<MessageRecord> tail = new ArrayList<>(messages.subList(splitIndex, messages.size()));
    if (head.isEmpty()) {
      return CompactionResult.noop();
    }
    String summary;
    try {
      summary = summarize(head);
    } catch (Exception e) {
      return CompactionResult.fail("压缩调用失败：" + e.getMessage());
    }
    if (summary == null || summary.isBlank()) {
      return CompactionResult.fail("压缩失败，摘要为空。");
    }
    List<MessageRecord> compacted = new ArrayList<>();
    compacted.add(new MessageRecord("assistant", "【对话摘要】\n" + summary.trim()));
    compacted.addAll(tail);
    session.setMessages(compacted);
    return CompactionResult.compacted(summary.trim());
  }

  // 使用模型把历史消息压缩成简洁中文摘要。
  private String summarize(List<MessageRecord> history) {
    String transcript = formatTranscript(history);
    int maxChars = Math.max(1000, properties.getCompactionInputMaxChars());
    if (transcript.length() > maxChars) {
      transcript = transcript.substring(transcript.length() - maxChars);
    }
    int targetTokens = Math.max(200, properties.getCompactionTargetTokens());
    int targetChars = targetTokens * 4;
    String promptText =
        "请将以下对话压缩成中文摘要，要求：\n"
            + "1) 只保留关键事实与决策\n"
            + "2) 控制在 "
            + targetChars
            + " 字以内\n"
            + "3) 输出为 5-10 条要点\n\n"
            + "对话内容：\n"
            + transcript;
    List<Message> messages = List.of(
        new SystemMessage("你是对话压缩器，只负责总结，不引入新信息。"),
        new UserMessage(promptText));
    return aiClient.chat(messages);
  }

  // 将消息列表格式化为可读的对话文本。
  private String formatTranscript(List<MessageRecord> history) {
    StringBuilder builder = new StringBuilder();
    for (MessageRecord record : history) {
      if (record == null || record.getContent() == null) {
        continue;
      }
      String role = record.getRole() == null ? "" : record.getRole().trim().toLowerCase();
      String label =
          switch (role) {
            case "user" -> "用户";
            case "assistant" -> "助手";
            case "tool" -> "工具";
            default -> "其他";
          };
      builder.append(label).append("：").append(record.getContent()).append("\n");
    }
    return builder.toString().trim();
  }
}
